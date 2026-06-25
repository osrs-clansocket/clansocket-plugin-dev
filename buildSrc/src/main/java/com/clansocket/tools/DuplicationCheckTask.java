package com.clansocket.tools;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class DuplicationCheckTask extends DefaultTask
{
    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @Input
    public abstract Property<Integer> getMinTokens();

    @Input
    public abstract Property<Integer> getMinAstNodes();

    @Input
    public abstract Property<Integer> getMinSignatureBodyNodes();

    @Input
    public abstract Property<Integer> getMinCallees();

    @Input
    public abstract Property<Boolean> getFailOnViolation();

    @InputFile
    @Optional
    public abstract RegularFileProperty getSignatureExclusionsFile();

    @OutputDirectory
    public abstract DirectoryProperty getReportDir();

    public DuplicationCheckTask()
    {
        getMinTokens().convention(50);
        getMinAstNodes().convention(20);
        getMinSignatureBodyNodes().convention(25);
        getMinCallees().convention(5);
        getFailOnViolation().convention(Boolean.TRUE);
        getReportDir().convention(getProject().getLayout().getBuildDirectory().dir("reports/duplicationCheck"));
    }

    @TaskAction
    public void execute() throws IOException
    {
        final List<ParsedSource> parsed = SourceLoader.load(getSourceRoots());
        final Path exclusionsPath = getSignatureExclusionsFile().isPresent()
                ? getSignatureExclusionsFile().getAsFile().get().toPath()
                : null;
        final SignatureExclusionMap exclusions = SignatureExclusionMap.load(exclusionsPath);
        final List<DupMatch> identical = new TokenScanner(getMinTokens().get()).scan(parsed);
        final List<DupMatch> logical = new AstNormalizer(getMinAstNodes().get()).scan(parsed);
        final List<DupMatch> signature = new SignatureShapeScanner(getMinSignatureBodyNodes().get(), exclusions).scan(parsed);
        final List<DupMatch> callPattern = new CallPatternScanner(getMinCallees().get()).scan(parsed);

        final String report = DupReport.format(identical, logical, signature, callPattern, parsed.size());
        getLogger().lifecycle(report);
        if (exclusions.size() > 0)
        {
            getLogger().lifecycle("Signature exclusions loaded: " + exclusions.size() + " methods (see config/duplication/signature-exclusions.txt).");
        }
        ReportWriter.writeRawText(getReportDir().get().getAsFile().toPath(), "DuplicationCheck", report);

        final int blocking = identical.size() + logical.size();
        if (blocking > 0 && getFailOnViolation().get())
        {
            throw new GradleException("DuplicationCheck: " + blocking + " blocking dup group(s) found. Signature + call-pattern findings are informational only.");
        }
    }
}
