package com.clansocket.tools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

final class DupReport
{
    private DupReport()
    {
    }

    static String format(final List<DupMatch> identical, final List<DupMatch> logical,
        final List<DupMatch> signature, final List<DupMatch> callPattern, final int fileCount)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append('\n').append("── DuplicationCheck ─────────────────────────────").append('\n');
        sb.append("Scanned: ").append(fileCount).append(" files").append('\n');
        sb.append("Identical dup groups:    ").append(identical.size()).append(" (blocking)").append('\n');
        sb.append("Logical dup groups:      ").append(logical.size()).append(" (blocking)").append('\n');
        sb.append("Signature dup groups:    ").append(signature.size()).append(" (informational)").append('\n');
        sb.append("Call-pattern dup groups: ").append(callPattern.size()).append(" (informational)").append('\n');
        if (identical.isEmpty() && logical.isEmpty() && signature.isEmpty() && callPattern.isEmpty())
        {
            sb.append('\n').append("CLEAN — no duplication detected.").append('\n');
            return sb.toString();
        }
        final Path cwd = Paths.get("").toAbsolutePath();
        int n = 1;
        n = renderRangeGroups(sb, cwd, "Identical", identical, n);
        n = renderLabelGroups(sb, cwd, "Logical", logical, n);
        n = renderLabelGroups(sb, cwd, "Signature", signature, n);
        renderLabelGroups(sb, cwd, "Call-pattern", callPattern, n);
        sb.append('\n').append("DRY strategy: extract shared logic into a named function, constant, or shared contract.").append('\n');
        return sb.toString();
    }

    private static int renderRangeGroups(final StringBuilder sb, final Path cwd, final String label,
        final List<DupMatch> groups, final int startNum)
    {
        int n = startNum;
        for (final DupMatch m : groups)
        {
            sb.append('\n').append('[').append(n++).append("] ").append(label).append(" — ").append(m.summary).append('\n');
            for (final DupMatch.Location loc : m.locations)
            {
                sb.append("    ").append(relativize(cwd, loc.file)).append(':')
                    .append(loc.startLine).append('-').append(loc.endLine).append('\n');
            }
        }
        return n;
    }

    private static int renderLabelGroups(final StringBuilder sb, final Path cwd, final String label,
        final List<DupMatch> groups, final int startNum)
    {
        int n = startNum;
        for (final DupMatch m : groups)
        {
            sb.append('\n').append('[').append(n++).append("] ").append(label).append(" — ").append(m.summary).append('\n');
            for (final DupMatch.Location loc : m.locations)
            {
                final String suffix = loc.label.isEmpty() ? "" : " — " + loc.label;
                sb.append("    ").append(relativize(cwd, loc.file)).append(':')
                    .append(loc.startLine).append(suffix).append('\n');
            }
        }
        return n;
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
