package com.clansocket.tools;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ConventionRules
{
    private static final Set<String> HOT_PATH_HANDLERS = new HashSet<>(Arrays.asList("onGameTick", "onClientTick",
            "onHitsplatApplied", "onItemContainerChanged", "onAnimationChanged", "onVarbitChanged", "onStatChanged",
            "onInteractingChanged", "onChatMessage", "onScriptPreFired", "onScriptPostFired"));

    private ConventionRules()
    {
    }

    static List<RuneliteFinding> scan(final ParsedSource src)
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        scanWildcardImports(src, out);
        scanCopyrightHeader(src, out);
        scanInfoInHotPath(src, out);
        scanSleepInSubscribe(src, out);
        scanConfigItemDescriptions(src, out);
        return out;
    }

    private static void scanWildcardImports(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final ImportDeclaration imp : src.unit.getImports())
        {
            if (!imp.isAsterisk())
            {
                continue;
            }
            final int line = imp.getBegin().map(p -> p.line).orElse(0);
            out.add(RuneliteFinding.error("code-conv.wildcard-import", src.path, line,
                    "wildcard import '" + imp.getNameAsString() + ".*' banned; list each type explicitly"));
        }
    }

    private static void scanCopyrightHeader(final ParsedSource src, final List<RuneliteFinding> out)
    {
        final boolean hasCopyright = src.source.length() >= 200
                && src.source.substring(0, 200).toLowerCase(java.util.Locale.ROOT).contains("copyright");
        if (!hasCopyright)
        {
            out.add(RuneliteFinding.warn("code-conv.copyright-header", src.path, 1,
                    "missing BSD 2-Clause copyright header (runelite convention)"));
        }
    }

    private static void scanInfoInHotPath(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final MethodDeclaration m : src.unit.findAll(MethodDeclaration.class))
        {
            if (!hasSubscribe(m) && !HOT_PATH_HANDLERS.contains(m.getNameAsString()))
            {
                continue;
            }
            for (final MethodCallExpr call : m.findAll(MethodCallExpr.class))
            {
                if (!"info".equals(call.getNameAsString()))
                {
                    continue;
                }
                if (!call.getScope().filter(s -> "log".equals(s.toString())).isPresent())
                {
                    continue;
                }
                final int line = call.getBegin().map(p -> p.line).orElse(0);
                out.add(RuneliteFinding.warn("logging.info-in-hot-path", src.path, line,
                        "log.info(...) in " + m.getNameAsString() + " — demote to log.debug(...) for per-event handlers"));
            }
        }
    }

    private static void scanSleepInSubscribe(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final MethodDeclaration m : src.unit.findAll(MethodDeclaration.class))
        {
            if (!hasSubscribe(m))
            {
                continue;
            }
            for (final MethodCallExpr call : m.findAll(MethodCallExpr.class))
            {
                if (!"sleep".equals(call.getNameAsString()))
                {
                    continue;
                }
                if (!call.getScope().filter(s -> "Thread".equals(s.toString())).isPresent())
                {
                    continue;
                }
                final int line = call.getBegin().map(p -> p.line).orElse(0);
                out.add(RuneliteFinding.error("threading.sleep-in-subscribe", src.path, line,
                        "Thread.sleep(...) in @Subscribe " + m.getNameAsString() + " — blocks the client thread"));
            }
        }
    }

    private static boolean hasSubscribe(final MethodDeclaration m)
    {
        return m.isAnnotationPresent("Subscribe");
    }

    private static void scanConfigItemDescriptions(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final NormalAnnotationExpr ann : src.unit.findAll(NormalAnnotationExpr.class))
        {
            if (!"ConfigItem".equals(ann.getNameAsString()))
            {
                continue;
            }
            inspectConfigItem(src, ann, out);
        }
    }

    private static void inspectConfigItem(final ParsedSource src, final NormalAnnotationExpr ann,
            final List<RuneliteFinding> out)
    {
        final int line = ann.getBegin().map(p -> p.line).orElse(0);
        for (final MemberValuePair pair : ann.getPairs())
        {
            final String fieldName = pair.getNameAsString();
            if (!"description".equals(fieldName) && !"name".equals(fieldName))
            {
                continue;
            }
            if (!(pair.getValue() instanceof StringLiteralExpr))
            {
                continue;
            }
            if (!((StringLiteralExpr) pair.getValue()).getValue().isEmpty())
            {
                continue;
            }
            if ("description".equals(fieldName))
            {
                out.add(RuneliteFinding.error("plugin-config.empty-description", src.path, line,
                        "@ConfigItem description=\"\" — empty descriptions hurt UX, reviewers flag"));
            }
            else
            {
                out.add(RuneliteFinding.error("plugin-config.empty-name", src.path, line,
                        "@ConfigItem name=\"\" — empty display name is broken UX"));
            }
        }
    }

    @SuppressWarnings("unused")
    static CompilationUnit nop(final ParsedSource src)
    {
        return src.unit;
    }

    @SuppressWarnings("unused")
    static String annoName(final AnnotationExpr a)
    {
        return a.getNameAsString();
    }
}
