package com.clansocket.tools;

import java.nio.file.Path;
import java.util.List;

public final class DupMatch
{
    public enum Kind { IDENTICAL, LOGICAL, SIGNATURE, CALL_PATTERN }

    public final Kind kind;
    public final int size;
    public final String summary;
    public final List<Location> locations;

    public DupMatch(final Kind kind, final int size, final String summary, final List<Location> locations)
    {
        this.kind = kind;
        this.size = size;
        this.summary = summary;
        this.locations = locations;
    }

    public static final class Location
    {
        public final Path file;
        public final int startLine;
        public final int endLine;
        public final String label;

        public Location(final Path file, final int startLine, final int endLine, final String label)
        {
            this.file = file;
            this.startLine = startLine;
            this.endLine = endLine;
            this.label = label;
        }
    }
}
