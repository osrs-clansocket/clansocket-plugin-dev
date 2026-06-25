package com.clansocket.tools;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class UnusedCodeCheckTask extends DefaultTask
{
    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @Input
    public abstract Property<Boolean> getFailOnViolation();

    @OutputDirectory
    public abstract DirectoryProperty getReportDir();

    public UnusedCodeCheckTask()
    {
        getFailOnViolation().convention(Boolean.TRUE);
        getReportDir().convention(getProject().getLayout().getBuildDirectory().dir("reports/unusedCodeCheck"));
    }

    @TaskAction
    public void execute() throws IOException
    {
        final List<ParsedSource> sources = SourceLoader.load(getSourceRoots());
        final ReferenceIndex refs = new ReferenceIndex(sources);
        final List<UnusedFinding> findings = new DeadFinder(refs).scan(sources);

        final String report = formatReport(findings, sources.size());
        getLogger().lifecycle(report);
        ReportWriter.writeRawText(getReportDir().get().getAsFile().toPath(), "UnusedCodeCheck", report);

        if (!findings.isEmpty() && getFailOnViolation().get())
        {
            throw new GradleException("UnusedCodeCheck: " + findings.size() + " unused declaration(s). See report above.");
        }
    }

    private String formatReport(final List<UnusedFinding> findings, final int fileCount)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append('\n').append("── UnusedCodeCheck ──────────────────────────────").append('\n');
        sb.append("Scanned: ").append(fileCount).append(" files").append('\n');
        sb.append("Unused declarations: ").append(findings.size()).append('\n');
        if (findings.isEmpty())
        {
            sb.append('\n').append("CLEAN — no orphans detected.").append('\n');
            return sb.toString();
        }
        final Path cwd = Paths.get("").toAbsolutePath();
        int n = 1;
        for (final UnusedFinding f : findings)
        {
            sb.append('\n').append('[').append(n++).append("] ").append(f.kind.name())
                .append(" — ").append(f.name).append('\n');
            sb.append("    ").append(relativize(cwd, f.file)).append(':').append(f.line).append('\n');
        }
        sb.append('\n').append("Delete if truly dead. If called via reflection/DI, mark with the matching entry-point annotation.").append('\n');
        return sb.toString();
    }

    private static String relativize(final Path cwd, final Path file)
    {
        try
        {
            return cwd.relativize(file).toString().replace('\\', '/');
        }
        catch (final IllegalArgumentException ex)
        {
            return file.toString().replace('\\', '/');
        }
    }
}
