package com.clansocket.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class PrivacyTraceScannerTaskTest
{
    @Test
    void flagsForbiddenDtoFieldName()
    {
        final String src = "class Bar { public final String killerName; }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanDtoFields(wrap(src), findings);
        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).ruleId).isEqualTo("privacy.forbidden-dto-field");
        assertThat(findings.get(0).message).contains("killerName");
    }

    @Test
    void doesNotFlagSafeDtoFieldName()
    {
        final String src = "class Bar { public final String skillName; public final int level; }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanDtoFields(wrap(src), findings);
        assertThat(findings).isEmpty();
    }

    @Test
    void flagsGetNameOnThirdPartyScope()
    {
        final String src = "class Tracker { String steal() { return killer.getName(); } }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanActorGetNameCalls(wrap(src), findings);
        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).ruleId).isEqualTo("privacy.third-party-getname");
    }

    @Test
    void doesNotFlagGetNameOnLocalPlayerScope()
    {
        final String src = "class Tracker { String mine() { return localPlayer.getName(); } }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanActorGetNameCalls(wrap(src), findings);
        assertThat(findings).isEmpty();
    }

    @Test
    void doesNotFlagGetNameOnNpcScope()
    {
        final String src = "class Tracker { String npcName() { return npc.getName(); } }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanActorGetNameCalls(wrap(src), findings);
        assertThat(findings).isEmpty();
    }

    @Test
    void flagsGetNameOnVariableAssignedFromGetKiller()
    {
        final String src = "class Tracker {"
                + " void leak() {"
                + "   Player p = event.getKiller();"
                + "   String n = p.getName();"
                + " } }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanTaintedAssignments(wrap(src), findings);
        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).ruleId).isEqualTo("privacy.tainted-getname");
        assertThat(findings.get(0).message).contains("'p'").contains("forbidden source method");
    }

    @Test
    void flagsGetNameOnVariableAssignedFromGetOpponent()
    {
        final String src = "class Tracker {"
                + " void leak() {"
                + "   Actor opp = event.getOpponent();"
                + "   String n = opp.getName();"
                + " } }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanTaintedAssignments(wrap(src), findings);
        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).message).contains("'opp'");
    }

    @Test
    void doesNotFlagGetNameOnUntaintedVariable()
    {
        final String src = "class Tracker {"
                + " void safe() {"
                + "   Player p = client.getLocalPlayer();"
                + "   String n = p.getName();"
                + " } }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanTaintedAssignments(wrap(src), findings);
        assertThat(findings).isEmpty();
    }

    @Test
    void doesNotFlagWhenNoTaintedAssignmentsExist()
    {
        final String src = "class Tracker { String safe() { return localPlayer.getName(); } }";
        final List<RuneliteFinding> findings = new ArrayList<>();
        PrivacyTraceScannerTask.scanTaintedAssignments(wrap(src), findings);
        assertThat(findings).isEmpty();
    }

    private static ParsedSource wrap(final String code)
    {
        final CompilationUnit unit = StaticJavaParser.parse(code);
        return new ParsedSource(Paths.get("Foo.java"), code, unit);
    }
}
