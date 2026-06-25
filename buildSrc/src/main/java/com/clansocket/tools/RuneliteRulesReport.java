package com.clansocket.tools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RuneliteRulesReport
{
    private RuneliteRulesReport()
    {
    }

    static String format(final List<RuneliteFinding> findings, final int fileCount)
    {
        return format(findings, fileCount, "RuneliteRulesCheck");
    }

    static String format(final List<RuneliteFinding> findings, final int fileCount, final String taskName)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append('\n').append("── ").append(taskName).append(" ──────────────────────────").append('\n');
        sb.append("Scanned: ").append(fileCount).append(" files").append('\n');

        int errors = 0;
        int warns = 0;
        for (final RuneliteFinding f : findings)
        {
            if (f.severity == RuneliteFinding.Severity.ERROR)
            {
                errors++;
            }
            else
            {
                warns++;
            }
        }

        sb.append("Errors:   ").append(errors).append(" (blocking)").append('\n');
        sb.append("Warnings: ").append(warns).append(" (informational)").append('\n');

        if (findings.isEmpty())
        {
            sb.append('\n').append("CLEAN — no runelite rule violations detected.").append('\n');
            return sb.toString();
        }

        final Path cwd = Paths.get("").toAbsolutePath();
        final Map<String, List<RuneliteFinding>> byRule = groupByRule(findings);
        int n = 1;
        for (final Map.Entry<String, List<RuneliteFinding>> e : byRule.entrySet())
        {
            sb.append('\n').append('[').append(n++).append("] ").append(e.getKey()).append('\n');
            for (final RuneliteFinding f : e.getValue())
            {
                final String severityTag = f.severity == RuneliteFinding.Severity.ERROR ? "ERROR" : "warn";
                sb.append("    [").append(severityTag).append("] ")
                    .append(relativize(cwd, f.file)).append(':').append(f.line)
                    .append(" — ").append(f.message).append('\n');
            }
        }
        sb.append('\n').append("See D:/BanesLab/ClanSocket/runelite-rules/ for each rule's source.").append('\n');
        return sb.toString();
    }

    private static Map<String, List<RuneliteFinding>> groupByRule(final List<RuneliteFinding> findings)
    {
        final Map<String, List<RuneliteFinding>> map = new LinkedHashMap<>();
        for (final RuneliteFinding f : findings)
        {
            map.computeIfAbsent(f.ruleId, k -> new java.util.ArrayList<>()).add(f);
        }
        return map;
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
