package com.clansocket.tools;

import com.github.javaparser.ast.CompilationUnit;

import java.nio.file.Path;

public final class ParsedSource
{
    public final Path path;
    public final String source;
    public final CompilationUnit unit;

    public ParsedSource(final Path path, final String source, final CompilationUnit unit)
    {
        this.path = path;
        this.source = source;
        this.unit = unit;
    }
}
