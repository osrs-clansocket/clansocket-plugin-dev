package com.clansocket.tools;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PrivacyTraceScannerTask extends DefaultTask
{
    private static final Set<String> FORBIDDEN_FIELD_NAMES = Set.of(
            "playerName", "playerRsn", "killerName", "opponentName", "attackerName",
            "victimName", "observedRsn", "observedName", "otherPlayer", "otherPlayerName");

    private static final Set<String> THIRD_PARTY_SCOPE_HINTS = Set.of(
            "killer", "opponent", "attacker", "victim", "observed", "otherPlayer", "otherActor");

    private static final Set<String> LOCAL_PLAYER_SCOPE_HINTS = Set.of(
            "localPlayer", "local", "self", "me");

    private static final Set<String> FORBIDDEN_SOURCE_METHODS = Set.of(
            "getKiller", "getOpponent", "getAttacker", "getVictim", "getOtherActor", "getOtherPlayer");

    private static final String GET_NAME = "getName";
    private static final String PROTOCOL_PATH_HINT = "protocol";

    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @Input
    public abstract Property<Boolean> getFailOnViolation();

    @OutputDirectory
    public abstract DirectoryProperty getReportDir();

    public PrivacyTraceScannerTask()
    {
        getFailOnViolation().convention(Boolean.TRUE);
        getReportDir().convention(getProject().getLayout().getBuildDirectory().dir("reports/privacyTraceScan"));
    }

    @TaskAction
    public void execute() throws IOException
    {
        final List<ParsedSource> sources = SourceLoader.load(getSourceRoots());
        final List<RuneliteFinding> findings = new ArrayList<>();
        for (final ParsedSource src : sources)
        {
            if (src.path.toString().contains(PROTOCOL_PATH_HINT))
            {
                scanDtoFields(src, findings);
            }
            else
            {
                scanActorGetNameCalls(src, findings);
                scanTaintedAssignments(src, findings);
            }
        }
        final String report = RuneliteRulesReport.format(findings, sources.size(), "PrivacyTraceScanner");
        getLogger().lifecycle(report);
        ReportWriter.writeTextAndHtml(getReportDir().get().getAsFile().toPath(), "PrivacyTraceScanner",
                findings, sources.size());
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
                    "PrivacyTraceScanner: " + errors + " potential third-party data leak(s).");
        }
    }

    static void scanDtoFields(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final FieldDeclaration field : src.unit.findAll(FieldDeclaration.class))
        {
            for (final VariableDeclarator var : field.getVariables())
            {
                final String name = var.getNameAsString();
                if (FORBIDDEN_FIELD_NAMES.contains(name))
                {
                    final int line = field.getBegin().map(p -> p.line).orElse(0);
                    out.add(RuneliteFinding.error("privacy.forbidden-dto-field", src.path, line,
                            "DTO field '" + name + "' implies third-party player data — arch rule 7 forbids observed-player names"));
                }
            }
        }
    }

    static void scanActorGetNameCalls(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final MethodCallExpr call : src.unit.findAll(MethodCallExpr.class))
        {
            if (!GET_NAME.equals(call.getNameAsString()) || call.getScope().isEmpty())
            {
                continue;
            }
            final Expression scope = call.getScope().get();
            final String scopeText = scope.toString();
            if (matchesLocalHint(scopeText) || matchesNpcHint(scopeText))
            {
                continue;
            }
            if (matchesThirdPartyHint(scopeText))
            {
                final int line = call.getBegin().map(p -> p.line).orElse(0);
                out.add(RuneliteFinding.error("privacy.third-party-getname", src.path, line,
                        "getName() called on scope '" + scopeText
                                + "' suggesting non-local actor; arch rule 7 forbids observed-player names in payloads"));
            }
        }
    }

    static void scanTaintedAssignments(final ParsedSource src, final List<RuneliteFinding> out)
    {
        final Set<String> tainted = collectTaintedVariables(src);
        if (tainted.isEmpty())
        {
            return;
        }
        for (final MethodCallExpr call : src.unit.findAll(MethodCallExpr.class))
        {
            if (!GET_NAME.equals(call.getNameAsString()) || call.getScope().isEmpty())
            {
                continue;
            }
            final Expression scope = call.getScope().get();
            if (!scope.isNameExpr())
            {
                continue;
            }
            final String scopeName = scope.asNameExpr().getNameAsString();
            if (tainted.contains(scopeName))
            {
                final int line = call.getBegin().map(p -> p.line).orElse(0);
                out.add(RuneliteFinding.error("privacy.tainted-getname", src.path, line,
                        "getName() called on variable '" + scopeName
                                + "' that was assigned from a forbidden source method (event.getKiller / getOpponent / etc); arch rule 7 forbids observed-player names in payloads"));
            }
        }
    }

    private static Set<String> collectTaintedVariables(final ParsedSource src)
    {
        final Set<String> tainted = new HashSet<>();
        for (final VariableDeclarator var : src.unit.findAll(VariableDeclarator.class))
        {
            if (var.getInitializer().isEmpty())
            {
                continue;
            }
            final Expression init = var.getInitializer().get();
            if (!init.isMethodCallExpr())
            {
                continue;
            }
            final String calleeName = init.asMethodCallExpr().getNameAsString();
            if (FORBIDDEN_SOURCE_METHODS.contains(calleeName))
            {
                tainted.add(var.getNameAsString());
            }
        }
        return tainted;
    }

    private static boolean matchesLocalHint(final String scope)
    {
        for (final String hint : LOCAL_PLAYER_SCOPE_HINTS)
        {
            if (scope.contains(hint))
            {
                return true;
            }
        }
        return scope.contains("LocalPlayer") || scope.contains("getLocalPlayer");
    }

    private static boolean matchesNpcHint(final String scope)
    {
        return scope.contains("npc") || scope.contains("Npc") || scope.contains("NPC")
                || scope.contains("composition") || scope.contains("Composition");
    }

    private static boolean matchesThirdPartyHint(final String scope)
    {
        for (final String hint : THIRD_PARTY_SCOPE_HINTS)
        {
            if (scope.contains(hint))
            {
                return true;
            }
        }
        return false;
    }
}
