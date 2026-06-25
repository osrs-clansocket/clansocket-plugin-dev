package com.clansocket.tools;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class StripCommentsTask extends DefaultTask
{
    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @TaskAction
    public void execute() throws IOException
    {
        int processed = 0;
        int changed = 0;
        int preserved = 0;
        for (java.io.File root : getSourceRoots())
        {
            if (!root.exists())
            {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root.toPath()))
            {
                List<Path> files = new ArrayList<>();
                stream.filter(p -> p.toString().endsWith(".java")).forEach(files::add);
                for (Path file : files)
                {
                    processed++;
                    String before = new String(Files.readAllBytes(file));
                    CompilationUnit cu = StaticJavaParser.parse(before);
                    LexicalPreservingPrinter.setup(cu);
                    Optional<Comment> leading = cu.getComment();
                    List<Comment> comments = new ArrayList<>(cu.getAllContainedComments());
                    if (leading.isPresent())
                    {
                        comments.add(leading.get());
                    }
                    if (comments.isEmpty())
                    {
                        continue;
                    }
                    for (Comment c : comments)
                    {
                        if (leading.isPresent() && c == leading.get() && c.getContent().contains("Copyright"))
                        {
                            preserved++;
                            continue;
                        }
                        c.remove();
                    }
                    String after = LexicalPreservingPrinter.print(cu);
                    if (!after.equals(before))
                    {
                        Files.write(file, after.getBytes());
                        changed++;
                    }
                }
            }
        }
        getLogger().lifecycle("StripComments: processed={} changed={} preserved={}", processed, changed, preserved);
    }
}
