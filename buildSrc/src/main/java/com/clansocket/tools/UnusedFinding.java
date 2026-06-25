package com.clansocket.tools;

import java.nio.file.Path;

public final class UnusedFinding
{
    public enum Kind { CLASS, METHOD, FIELD, ENUM_CONST }

    public final Kind kind;
    public final String name;
    public final Path file;
    public final int line;

    public UnusedFinding(final Kind kind, final String name, final Path file, final int line)
    {
        this.kind = kind;
        this.name = name;
        this.file = file;
        this.line = line;
    }
}
