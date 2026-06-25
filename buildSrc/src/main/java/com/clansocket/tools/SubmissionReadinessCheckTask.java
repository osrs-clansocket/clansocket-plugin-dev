package com.clansocket.tools;

import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public abstract class SubmissionReadinessCheckTask extends DefaultTask
{
    private static final Path PLACEHOLDER = Paths.get("(project root)");
    private static final int MAX_ICON_WIDTH = 48;
    private static final int MAX_ICON_HEIGHT = 72;
    private static final int DEFAULT_MAX_LOC = 10_000;
    private static final int MIN_README_BYTES = 100;
    private static final int PNG_IHDR_OFFSET = 16;
    private static final int PNG_MIN_BYTES = 24;
    private static final Set<String> REQUIRED_PROPS = new LinkedHashSet<>(Arrays.asList(
            "displayName", "author", "description", "tags", "plugins"));
    private static final Set<String> NON_JAVA_EXTS = new LinkedHashSet<>(Arrays.asList(".kt", ".scala", ".groovy"));
    private static final String BUILD_GRADLE = "build.gradle";
    private static final String LAUNCHER_REL_PATH = "src/test/java/com/clansocket/ClanSocketPluginTest.java";
    private static final String TEST_JAVA_ROOT = "src/test/java";
    private static final String RUNELITE_GROUP = "net.runelite";
    private static final String TEST_IMPL_PREFIX = "testImplementation";

    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @InputDirectory
    public abstract DirectoryProperty getProjectRoot();

    @Input
    public abstract Property<Integer> getMaxLoc();

    @Input
    public abstract Property<Boolean> getStrictHygiene();

    @Input
    public abstract Property<Boolean> getFailOnViolation();

    public SubmissionReadinessCheckTask()
    {
        getMaxLoc().convention(DEFAULT_MAX_LOC);
        getStrictHygiene().convention(Boolean.TRUE);
        getFailOnViolation().convention(Boolean.TRUE);
    }

    @TaskAction
    public void execute() throws IOException
    {
        final File root = getProjectRoot().get().getAsFile();
        final List<RuneliteFinding> findings = new ArrayList<>();
        final Properties manifest = new Properties();

        findings.addAll(checkManifestFile(root, manifest));
        findings.addAll(checkLicenseFile(root));
        findings.addAll(checkReadme(root));
        findings.addAll(checkIcon(root));
        findings.addAll(checkBuildGradle(root));
        findings.addAll(checkNonJavaSources(root));
        findings.addAll(checkSubmissionStripAware(root));

        final List<ParsedSource> sources = SourceLoader.load(getSourceRoots());
        findings.addAll(checkLoc(sources, getMaxLoc().get()));
        findings.addAll(checkPluginDescriptor(sources, manifest));

        if (getStrictHygiene().get())
        {
            for (final ParsedSource src : sources)
            {
                findings.addAll(SubmissionHygieneRules.scan(src));
            }
        }

        final String report = RuneliteRulesReport.format(findings, sources.size(), "SubmissionReadinessCheck");
        getLogger().lifecycle(report);

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
                    "SubmissionReadinessCheck: " + errors + " blocking violation(s). See report above.");
        }
    }

    private List<RuneliteFinding> checkManifestFile(final File root, final Properties manifest) throws IOException
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final File f = new File(root, "runelite-plugin.properties");
        if (!f.exists())
        {
            out.add(RuneliteFinding.error("submission.manifest-missing", PLACEHOLDER, 0,
                    "runelite-plugin.properties missing at repo root"));
            return out;
        }
        try (FileInputStream in = new FileInputStream(f))
        {
            manifest.load(in);
        }
        for (final String key : REQUIRED_PROPS)
        {
            final String value = manifest.getProperty(key);
            if (value == null || value.trim().isEmpty())
            {
                out.add(RuneliteFinding.error("submission.manifest-required-field-missing", f.toPath(), 0,
                        "runelite-plugin.properties: '" + key + "' missing or blank"));
            }
        }
        if (manifest.getProperty("build") == null)
        {
            out.add(RuneliteFinding.warn("submission.manifest-build-not-set", f.toPath(), 0,
                    "runelite-plugin.properties: 'build=' not set (defaults to 'standard')"));
        }
        return out;
    }

    private List<RuneliteFinding> checkLicenseFile(final File root) throws IOException
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final File f = new File(root, "LICENSE");
        if (!f.exists())
        {
            out.add(RuneliteFinding.error("submission.license-missing", PLACEHOLDER, 0,
                    "LICENSE missing at repo root"));
            return out;
        }
        final String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        if (!content.contains("BSD 2-Clause") && !content.contains("Redistribution and use in source and binary"))
        {
            out.add(RuneliteFinding.error("submission.license-not-bsd2", f.toPath(), 0,
                    "LICENSE does not appear to be BSD 2-Clause"));
        }
        return out;
    }

    private List<RuneliteFinding> checkReadme(final File root)
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final File f = new File(root, "README.md");
        if (!f.exists())
        {
            out.add(RuneliteFinding.error("submission.readme-missing", PLACEHOLDER, 0,
                    "README.md missing at repo root"));
            return out;
        }
        if (f.length() < MIN_README_BYTES)
        {
            out.add(RuneliteFinding.warn("submission.readme-trivial", f.toPath(), 0,
                    "README.md is under " + MIN_README_BYTES + " bytes — provide end-user docs"));
        }
        return out;
    }

    private List<RuneliteFinding> checkIcon(final File root) throws IOException
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final File f = new File(root, "icon.png");
        if (!f.exists())
        {
            out.add(RuneliteFinding.warn("submission.icon-missing", PLACEHOLDER, 0,
                    "icon.png missing at repo root (optional)"));
            return out;
        }
        try (RandomAccessFile raf = new RandomAccessFile(f, "r"))
        {
            if (raf.length() < PNG_MIN_BYTES)
            {
                out.add(RuneliteFinding.error("submission.icon-invalid", f.toPath(), 0,
                        "icon.png is too small to be a valid PNG"));
                return out;
            }
            raf.seek(PNG_IHDR_OFFSET);
            final int width = raf.readInt();
            final int height = raf.readInt();
            if (width > MAX_ICON_WIDTH || height > MAX_ICON_HEIGHT)
            {
                out.add(RuneliteFinding.error("submission.icon-too-large", f.toPath(), 0,
                        "icon.png is " + width + "x" + height + " — max is "
                                + MAX_ICON_WIDTH + "x" + MAX_ICON_HEIGHT));
            }
        }
        return out;
    }

    private List<RuneliteFinding> checkBuildGradle(final File root) throws IOException
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final File f = new File(root, BUILD_GRADLE);
        if (!f.exists())
        {
            out.add(RuneliteFinding.error("submission.build-gradle-missing", PLACEHOLDER, 0,
                    "build.gradle missing at repo root"));
            return out;
        }
        final String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        if (!content.contains("'latest.release'"))
        {
            out.add(RuneliteFinding.warn("submission.runelite-version-not-latest", f.toPath(), 0,
                    "build.gradle: runeLiteVersion should be 'latest.release'"));
        }
        if (!content.contains("release.set(11)"))
        {
            out.add(RuneliteFinding.warn("submission.java-release-not-11", f.toPath(), 0,
                    "build.gradle: java release should be 11 (runelite constraint)"));
        }
        return out;
    }

    private List<RuneliteFinding> checkNonJavaSources(final File root) throws IOException
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final Path srcMain = root.toPath().resolve("src").resolve("main");
        if (!Files.exists(srcMain))
        {
            return out;
        }
        try (Stream<Path> stream = Files.walk(srcMain))
        {
            stream.filter(Files::isRegularFile).forEach(p ->
            {
                final String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                for (final String ext : NON_JAVA_EXTS)
                {
                    if (name.endsWith(ext))
                    {
                        out.add(RuneliteFinding.error("submission.non-java-source", p, 0,
                                "non-java source under src/main — only java allowed"));
                        return;
                    }
                }
            });
        }
        return out;
    }

    private List<RuneliteFinding> checkLoc(final List<ParsedSource> sources, final int maxLoc)
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        int total = 0;
        for (final ParsedSource src : sources)
        {
            total += countLines(src.source);
        }
        if (total > maxLoc)
        {
            out.add(RuneliteFinding.error("submission.loc-over-limit", PLACEHOLDER, 0,
                    "total LOC " + total + " exceeds " + maxLoc + " (initial submissions should stay <10k)"));
        }
        return out;
    }

    private static int countLines(final String src)
    {
        int count = 1;
        for (int i = 0; i < src.length(); i++)
        {
            if (src.charAt(i) == '\n')
            {
                count++;
            }
        }
        return count;
    }

    private List<RuneliteFinding> checkPluginDescriptor(final List<ParsedSource> sources, final Properties manifest)
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final String manifestName = manifest.getProperty("displayName", "");
        final String manifestDesc = manifest.getProperty("description", "");
        boolean found = false;
        for (final ParsedSource src : sources)
        {
            for (final NormalAnnotationExpr ann : src.unit.findAll(NormalAnnotationExpr.class))
            {
                if (!"PluginDescriptor".equals(ann.getNameAsString()))
                {
                    continue;
                }
                found = true;
                checkDescriptorPair(src, ann, "name", manifestName, "displayName", out);
                checkDescriptorPair(src, ann, "description", manifestDesc, "description", out);
            }
        }
        if (!found)
        {
            out.add(RuneliteFinding.error("submission.plugin-descriptor-missing", PLACEHOLDER, 0,
                    "no @PluginDescriptor found in any source — required for plugin recognition"));
        }
        return out;
    }

    private static void checkDescriptorPair(final ParsedSource src, final NormalAnnotationExpr ann,
            final String descriptorField, final String manifestValue, final String manifestField,
            final List<RuneliteFinding> out)
    {
        if (manifestValue == null || manifestValue.isEmpty())
        {
            return;
        }
        for (final MemberValuePair pair : ann.getPairs())
        {
            if (!descriptorField.equals(pair.getNameAsString()))
            {
                continue;
            }
            if (!(pair.getValue() instanceof StringLiteralExpr))
            {
                continue;
            }
            final String descriptorValue = ((StringLiteralExpr) pair.getValue()).getValue();
            if (!descriptorValue.equals(manifestValue))
            {
                final int line = ann.getBegin().map(p -> p.line).orElse(0);
                out.add(RuneliteFinding.warn("submission.plugin-descriptor-mismatch", src.path, line,
                        "@PluginDescriptor " + descriptorField + " differs from runelite-plugin.properties "
                                + manifestField));
            }
        }
    }

    private List<RuneliteFinding> checkSubmissionStripAware(final File root) throws IOException
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        out.addAll(checkTestTreeStrip(root));
        final File gradleFile = new File(root, BUILD_GRADLE);
        if (gradleFile.exists())
        {
            out.addAll(checkTestImplStrip(gradleFile));
        }
        return out;
    }

    private List<RuneliteFinding> checkTestTreeStrip(final File root) throws IOException
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final Path testRoot = root.toPath().resolve(TEST_JAVA_ROOT);
        final Path launcher = root.toPath().resolve(LAUNCHER_REL_PATH);
        if (!Files.exists(testRoot))
        {
            return out;
        }
        if (!Files.exists(launcher))
        {
            out.add(RuneliteFinding.error("submission.test-launcher-missing", launcher, 0,
                    LAUNCHER_REL_PATH + " missing — runelite mandates this file at submission"));
        }
        try (Stream<Path> stream = Files.walk(testRoot))
        {
            stream.filter(Files::isRegularFile).forEach(p ->
            {
                if (!p.equals(launcher))
                {
                    out.add(RuneliteFinding.error("submission.test-tree-leakage", p, 0,
                            "stray test file under src/test/java — strip before submission"));
                }
            });
        }
        return out;
    }

    private List<RuneliteFinding> checkTestImplStrip(final File gradleFile) throws IOException
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        final String content = new String(Files.readAllBytes(gradleFile.toPath()), StandardCharsets.UTF_8);
        int lineNum = 1;
        int start = 0;
        for (int i = 0; i < content.length(); i++)
        {
            if (content.charAt(i) == '\n')
            {
                final String line = content.substring(start, i).trim();
                if (line.startsWith(TEST_IMPL_PREFIX) && !line.contains(RUNELITE_GROUP))
                {
                    out.add(RuneliteFinding.error("submission.test-impl-leakage", gradleFile.toPath(), lineNum,
                            "non-runelite testImplementation line — strip before submission"));
                }
                start = i + 1;
                lineNum++;
            }
        }
        return out;
    }
}
