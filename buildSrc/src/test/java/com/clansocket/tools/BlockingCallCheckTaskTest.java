package com.clansocket.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

final class BlockingCallCheckTaskTest
{
    @Test
    void flagsThreadSleepInsideSubscribeHandler()
    {
        final String src = "class Foo {"
                + " @Subscribe void onTick(Object e) throws Exception { Thread.sleep(100); } }";
        final List<RuneliteFinding> findings = BlockingCallCheckTask.scanSource(wrap(src));
        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).ruleId).isEqualTo("blocking.in-subscribe");
        assertThat(findings.get(0).message).contains("Thread.sleep").contains("onTick");
    }

    @Test
    void flagsFilesReadAllBytesInsideSubscribeHandler()
    {
        final String src = "class Foo {"
                + " @Subscribe void onTick(Object e) throws Exception {"
                + "   Files.readAllBytes(java.nio.file.Paths.get(\"/x\")); } }";
        final List<RuneliteFinding> findings = BlockingCallCheckTask.scanSource(wrap(src));
        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).message).contains("Files.readAllBytes");
    }

    @Test
    void doesNotFlagThreadSleepInNonSubscribeMethod()
    {
        final String src = "class Foo { void helper() throws Exception { Thread.sleep(100); } }";
        final List<RuneliteFinding> findings = BlockingCallCheckTask.scanSource(wrap(src));
        assertThat(findings).isEmpty();
    }

    @Test
    void doesNotFlagSubscribeHandlerWithoutBlockingCall()
    {
        final String src = "class Foo {"
                + " @Subscribe void onTick(Object e) { String s = \"safe\"; } }";
        final List<RuneliteFinding> findings = BlockingCallCheckTask.scanSource(wrap(src));
        assertThat(findings).isEmpty();
    }

    @Test
    void flagsTransitiveBlockingCallThroughHelper()
    {
        final String src = "class Foo {"
                + " @Subscribe void onTick(Object e) throws Exception { doWork(); }"
                + " void doWork() throws Exception { Thread.sleep(100); } }";
        final List<RuneliteFinding> findings = BlockingCallCheckTask.scanSource(wrap(src));
        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).ruleId).isEqualTo("blocking.transitive-from-subscribe");
        assertThat(findings.get(0).message).contains("Thread.sleep")
                .contains("onTick").contains("doWork");
    }

    @Test
    void flagsDeeperTransitiveChain()
    {
        final String src = "class Foo {"
                + " @Subscribe void onTick(Object e) throws Exception { layerA(); }"
                + " void layerA() throws Exception { layerB(); }"
                + " void layerB() throws Exception { Files.readAllBytes(null); } }";
        final List<RuneliteFinding> findings = BlockingCallCheckTask.scanSource(wrap(src));
        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).ruleId).isEqualTo("blocking.transitive-from-subscribe");
        assertThat(findings.get(0).message).contains("Files.readAllBytes")
                .contains("layerA").contains("layerB");
    }

    @Test
    void doesNotFollowRecursionInfinitely()
    {
        final String src = "class Foo {"
                + " @Subscribe void onTick(Object e) { recurse(); }"
                + " void recurse() { recurse(); } }";
        final List<RuneliteFinding> findings = BlockingCallCheckTask.scanSource(wrap(src));
        assertThat(findings).isEmpty();
    }

    private static ParsedSource wrap(final String code)
    {
        final CompilationUnit unit = StaticJavaParser.parse(code);
        return new ParsedSource(Paths.get("Foo.java"), code, unit);
    }
}
