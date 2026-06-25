package com.clansocket.tools;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.gradle.api.file.ConfigurableFileCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class SourceLoader
{
    private SourceLoader()
    {
    }

    public static List<ParsedSource> load(final ConfigurableFileCollection roots) throws IOException
    {
        final List<Path> files = collectFiles(roots);
        final List<ParsedSource> out = new ArrayList<>();
        for (final Path file : files)
        {
            final String src = new String(Files.readAllBytes(file));
            final CompilationUnit cu = StaticJavaParser.parse(src);
            out.add(new ParsedSource(file, src, cu));
        }
        return out;
    }

    private static List<Path> collectFiles(final ConfigurableFileCollection roots) throws IOException
    {
        final List<Path> all = new ArrayList<>();
        for (final java.io.File root : roots)
        {
            if (!root.exists())
            {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root.toPath()))
            {
                stream.filter(p -> p.toString().endsWith(".java")).forEach(all::add);
            }
        }
        return all;
    }
}
