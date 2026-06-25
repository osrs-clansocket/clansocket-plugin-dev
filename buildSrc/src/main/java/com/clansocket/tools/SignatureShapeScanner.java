package com.clansocket.tools;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class SignatureShapeScanner
{
    private static final Set<String> SKIP_ANNOTATIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "Override", "Subscribe", "Provides", "Inject", "PostConstruct", "Test", "BeforeEach", "AfterEach")));

    private static final Set<String> SKIP_NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("startUp",
            "shutDown", "configure", "provide", "get", "set", "refresh", "reset", "resetForReconnect", "emitFreshSnapshot",
            "onGameTick", "onConfigChanged", "onLoginScreen", "onLoggedIn", "onActorDeath", "onChatMessage",
            "onWidgetLoaded", "onWidgetClosed", "onItemContainerChanged", "onVarbitChanged", "onGameStateChanged",
            "onStatChanged", "onScriptPreFired", "onLootReceived", "onAnimationChanged", "onInteractingChanged",
            "onHitsplatApplied", "onClientTick")));

    private static final Set<String> EVENT_PARAM_SUFFIXES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("Changed", "Event", "Tick", "Fired", "Applied", "Loaded", "Closed", "Received")));

    private final int minBodyNodes;
    private final SignatureExclusionMap exclusions;

    SignatureShapeScanner(final int minBodyNodes, final SignatureExclusionMap exclusions)
    {
        this.minBodyNodes = minBodyNodes;
        this.exclusions = exclusions;
    }

    List<DupMatch> scan(final List<ParsedSource> files)
    {
        final Map<String, List<MethodEntry>> index = new HashMap<>();
        for (final ParsedSource f : files)
        {
            final String pkg = f.unit.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse("");
            for (final MethodDeclaration m : f.unit.findAll(MethodDeclaration.class))
            {
                if (shouldSkip(m))
                {
                    continue;
                }
                if (exclusions.isExcluded(methodFqn(pkg, m)))
                {
                    continue;
                }
                final String key = shapeKey(pkg, m);
                index.computeIfAbsent(key, k -> new ArrayList<>()).add(new MethodEntry(f, m));
            }
        }

        final List<DupMatch> out = new ArrayList<>();
        for (final Map.Entry<String, List<MethodEntry>> e : index.entrySet())
        {
            final List<MethodEntry> group = e.getValue();
            if (group.size() < 2 || !hasCrossClass(group))
            {
                continue;
            }
            out.add(toMatch(e.getKey(), group));
        }
        return out;
    }

    private boolean shouldSkip(final MethodDeclaration m)
    {
        if (!m.getBody().isPresent())
        {
            return true;
        }
        if (hasSkippedAnnotation(m) || SKIP_NAMES.contains(m.getNameAsString()))
        {
            return true;
        }
        if (isEventHandler(m))
        {
            return true;
        }
        if (m.getType().asString().equals("void") && m.getParameters().size() <= 1)
        {
            return true;
        }
        return countNodes(m.getBody().get()) < minBodyNodes;
    }

    private static boolean hasSkippedAnnotation(final MethodDeclaration m)
    {
        for (final String anno : SKIP_ANNOTATIONS)
        {
            if (m.isAnnotationPresent(anno))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isEventHandler(final MethodDeclaration m)
    {
        if (m.getParameters().size() != 1)
        {
            return false;
        }
        final String paramType = m.getParameters().get(0).getType().asString();
        for (final String suffix : EVENT_PARAM_SUFFIXES)
        {
            if (paramType.endsWith(suffix))
            {
                return true;
            }
        }
        return false;
    }

    private static String shapeKey(final String pkg, final MethodDeclaration m)
    {
        final StringBuilder sb = new StringBuilder(pkg).append('|').append(m.getType().asString()).append('(');
        final List<Parameter> params = m.getParameters();
        for (int i = 0; i < params.size(); i++)
        {
            if (i > 0)
            {
                sb.append(',');
            }
            sb.append(params.get(i).getType().asString());
        }
        return sb.append(')').toString();
    }

    private static String methodFqn(final String pkg, final MethodDeclaration m)
    {
        final String cls = enclosingClassName(m);
        final StringBuilder sb = new StringBuilder();
        if (!pkg.isEmpty())
        {
            sb.append(pkg).append('.');
        }
        sb.append(cls).append('#').append(m.getNameAsString()).append('(');
        final List<Parameter> params = m.getParameters();
        for (int i = 0; i < params.size(); i++)
        {
            if (i > 0)
            {
                sb.append(',');
            }
            sb.append(params.get(i).getType().asString());
        }
        return sb.append(')').toString();
    }

    private static String enclosingClassName(final MethodDeclaration m)
    {
        Node n = m.getParentNode().orElse(null);
        while (n != null)
        {
            if (n instanceof ClassOrInterfaceDeclaration)
            {
                return ((ClassOrInterfaceDeclaration) n).getNameAsString();
            }
            n = n.getParentNode().orElse(null);
        }
        return "";
    }

    private static boolean hasCrossClass(final List<MethodEntry> group)
    {
        final String first = group.get(0).file.path.toString();
        for (final MethodEntry e : group)
        {
            if (!e.file.path.toString().equals(first))
            {
                return true;
            }
        }
        return false;
    }

    private static DupMatch toMatch(final String shape, final List<MethodEntry> group)
    {
        final List<DupMatch.Location> locs = new ArrayList<>();
        for (final MethodEntry e : group)
        {
            final int start = e.method.getBegin().map(p -> p.line).orElse(0);
            final int end = e.method.getEnd().map(p -> p.line).orElse(0);
            locs.add(new DupMatch.Location(e.file.path, start, end, e.method.getNameAsString() + "()"));
        }
        return new DupMatch(DupMatch.Kind.SIGNATURE, group.size(), "shape " + shape, locs);
    }

    private int countNodes(final Node n)
    {
        int count = 1;
        for (final Node child : n.getChildNodes())
        {
            count += countNodes(child);
        }
        return count;
    }

    private static final class MethodEntry
    {
        final ParsedSource file;
        final MethodDeclaration method;

        MethodEntry(final ParsedSource file, final MethodDeclaration method)
        {
            this.file = file;
            this.method = method;
        }
    }
}
