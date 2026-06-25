package com.clansocket.tools;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
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
import java.util.stream.Stream;

public abstract class AddBracesTask extends DefaultTask
{
    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @TaskAction
    public void execute() throws IOException
    {
        int processed = 0;
        int changed = 0;
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

                    cu.findAll(IfStmt.class).forEach(s -> {
                        wrap(s.getThenStmt(), s::setThenStmt);
                        s.getElseStmt().ifPresent(e -> {
                            if (!(e instanceof BlockStmt) && !(e instanceof IfStmt))
                            {
                                wrap(e, s::setElseStmt);
                            }
                        });
                    });
                    cu.findAll(ForStmt.class).forEach(s -> wrap(s.getBody(), s::setBody));
                    cu.findAll(ForEachStmt.class).forEach(s -> wrap(s.getBody(), s::setBody));
                    cu.findAll(WhileStmt.class).forEach(s -> wrap(s.getBody(), s::setBody));
                    cu.findAll(DoStmt.class).forEach(s -> wrap(s.getBody(), s::setBody));

                    String after = LexicalPreservingPrinter.print(cu);
                    if (!after.equals(before))
                    {
                        Files.write(file, after.getBytes());
                        changed++;
                    }
                }
            }
        }
        getLogger().lifecycle("AddBraces: processed={} changed={}", processed, changed);
    }

    private static void wrap(final Statement body, final java.util.function.Consumer<BlockStmt> setter)
    {
        if (body instanceof BlockStmt)
        {
            return;
        }
        final BlockStmt block = new BlockStmt(new NodeList<>(body.clone()));
        setter.accept(block);
    }
}
