package com.clansocket.tools;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BlockingCallCheckTask extends DefaultTask
{
    private static final String SUBSCRIBE = "Subscribe";
    private static final int MAX_CLOSURE_DEPTH = 8;
    private static final Map<String, Set<String>> BLOCKING_CALLS = Map.of(
            "Thread", Set.of("sleep", "join"),
            "Object", Set.of("wait"),
            "Files", Set.of("read", "readAllBytes", "readAllLines", "readString",
                    "write", "writeString", "copy", "delete", "move"));

    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @Input
    public abstract Property<Boolean> getFailOnViolation();

    @OutputDirectory
    public abstract DirectoryProperty getReportDir();

    public BlockingCallCheckTask()
    {
        getFailOnViolation().convention(Boolean.TRUE);
        getReportDir().convention(getProject().getLayout().getBuildDirectory().dir("reports/blockingCallCheck"));
    }

    @TaskAction
    public void execute() throws IOException
    {
        final List<ParsedSource> sources = SourceLoader.load(getSourceRoots());
        final List<RuneliteFinding> findings = scanSources(sources);
        final String report = RuneliteRulesReport.format(findings, sources.size(), "BlockingCallCheck");
        getLogger().lifecycle(report);
        ReportWriter.writeTextAndHtml(getReportDir().get().getAsFile().toPath(), "BlockingCallCheck", findings,
                sources.size());

        int errors = 0;
        for (final RuneliteFinding f : findings)
        {
            if (f.severity == RuneliteFinding.Severity.ERROR)
            {
                errors++;
            }
        }
        if (errors > 0 && getFailOnViolation().get())
        {
            throw new GradleException(
                    "BlockingCallCheck: " + errors + " blocking call(s) reachable from @Subscribe handlers.");
        }
    }

    static List<RuneliteFinding> scanSource(final ParsedSource src)
    {
        return scanSources(List.of(src));
    }

    static List<RuneliteFinding> scanSources(final List<ParsedSource> sources)
    {
        final Map<String, List<MethodEntry>> methodIndex = buildMethodIndex(sources);
        final List<RuneliteFinding> out = new ArrayList<>();
        for (final ParsedSource src : sources)
        {
            for (final MethodDeclaration method : src.unit.findAll(MethodDeclaration.class))
            {
                if (isSubscribeMethod(method))
                {
                    walkClosure(src, method, methodIndex, new HashSet<>(),
                            new ArrayList<>(), 0, out);
                }
            }
        }
        return out;
    }

    private static Map<String, List<MethodEntry>> buildMethodIndex(final List<ParsedSource> sources)
    {
        final Map<String, List<MethodEntry>> index = new HashMap<>();
        for (final ParsedSource src : sources)
        {
            for (final MethodDeclaration method : src.unit.findAll(MethodDeclaration.class))
            {
                index.computeIfAbsent(method.getNameAsString(), k -> new ArrayList<>())
                        .add(new MethodEntry(src, method));
            }
        }
        return index;
    }

    private static boolean isSubscribeMethod(final MethodDeclaration method)
    {
        for (final AnnotationExpr ann : method.getAnnotations())
        {
            if (SUBSCRIBE.equals(ann.getNameAsString()))
            {
                return true;
            }
        }
        return false;
    }

    private static void walkClosure(final ParsedSource origin, final MethodDeclaration current,
            final Map<String, List<MethodEntry>> index, final Set<String> visited,
            final List<String> chain, final int depth, final List<RuneliteFinding> out)
    {
        final String key = current.getNameAsString() + "/" + current.getParameters().size();
        if (visited.contains(key) || depth > MAX_CLOSURE_DEPTH)
        {
            return;
        }
        visited.add(key);
        chain.add(current.getNameAsString());
        for (final MethodCallExpr call : current.findAll(MethodCallExpr.class))
        {
            checkCallInChain(origin, call, chain, out);
            recurseIntoCallee(origin, call, index, visited, chain, depth, out);
        }
        chain.remove(chain.size() - 1);
    }

    private static void recurseIntoCallee(final ParsedSource origin, final MethodCallExpr call,
            final Map<String, List<MethodEntry>> index, final Set<String> visited,
            final List<String> chain, final int depth, final List<RuneliteFinding> out)
    {
        final List<MethodEntry> candidates = index.get(call.getNameAsString());
        if (candidates == null)
        {
            return;
        }
        for (final MethodEntry candidate : candidates)
        {
            walkClosure(origin, candidate.method, index, visited, chain, depth + 1, out);
        }
    }

    private static void checkCallInChain(final ParsedSource origin, final MethodCallExpr call,
            final List<String> chain, final List<RuneliteFinding> out)
    {
        if (call.getScope().isEmpty())
        {
            return;
        }
        final Expression scope = call.getScope().get();
        if (!scope.isNameExpr())
        {
            return;
        }
        final String scopeName = scope.asNameExpr().getNameAsString();
        final Set<String> blockedNames = BLOCKING_CALLS.get(scopeName);
        if (blockedNames == null || !blockedNames.contains(call.getNameAsString()))
        {
            return;
        }
        final int line = call.getBegin().map(p -> p.line).orElse(0);
        final String ruleId = chain.size() > 1
                ? "blocking.transitive-from-subscribe"
                : "blocking.in-subscribe";
        final String via = chain.size() > 1 ? " via " + String.join(" → ", chain) : "";
        out.add(RuneliteFinding.error(ruleId, origin.path, line,
                "blocking call " + scopeName + "." + call.getNameAsString()
                        + " reachable from @Subscribe " + chain.get(0) + via
                        + " — blocks runelite client thread"));
    }

    private static final class MethodEntry
    {
        final ParsedSource source;
        final MethodDeclaration method;

        MethodEntry(final ParsedSource source, final MethodDeclaration method)
        {
            this.source = source;
            this.method = method;
        }
    }
}
