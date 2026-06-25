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
import java.util.ArrayList;
import java.util.List;

public abstract class RuneliteRulesCheckTask extends DefaultTask
{
    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @Input
    public abstract Property<Boolean> getFailOnViolation();

    @OutputDirectory
    public abstract DirectoryProperty getReportDir();

    public RuneliteRulesCheckTask()
    {
        getFailOnViolation().convention(Boolean.TRUE);
        getReportDir().convention(getProject().getLayout().getBuildDirectory().dir("reports/runeliteRulesCheck"));
    }

    @TaskAction
    public void execute() throws IOException
    {
        final List<ParsedSource> parsed = SourceLoader.load(getSourceRoots());
        final List<RuneliteFinding> findings = new ArrayList<>();
        for (final ParsedSource src : parsed)
        {
            findings.addAll(ForbiddenApiRules.scan(src));
            findings.addAll(ForbiddenLanguageRules.scan(src));
            findings.addAll(ResourceFsRules.scan(src));
            findings.addAll(ConventionRules.scan(src));
        }
        findings.addAll(ConfigKeyRule.scan(parsed));

        final String report = RuneliteRulesReport.format(findings, parsed.size());
        getLogger().lifecycle(report);
        ReportWriter.writeTextAndHtml(getReportDir().get().getAsFile().toPath(), "RuneliteRulesCheck",
                findings, parsed.size());

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
                    "RuneliteRulesCheck: " + errors + " blocking rule violation(s) found. See report above.");
        }
    }
}
