package com.clansocket.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReportWriter
{
    private static final String HTML_HEAD_TEMPLATE = "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
            + "<title>%s</title>"
            + "<style>"
            + "body{font-family:'JetBrains Mono','Consolas',monospace;background:#1e1e2e;color:#cdd6f4;"
            + "padding:1.5rem;line-height:1.5;}"
            + "h1{color:#fab387;border-bottom:1px solid #45475a;padding-bottom:0.5rem;margin-bottom:1rem;}"
            + ".header{margin-bottom:1.5rem;}"
            + ".stat{display:inline-block;margin-right:2rem;}"
            + ".errors{color:#f38ba8;font-weight:bold;}"
            + ".warns{color:#f9e2af;}"
            + ".clean{color:#a6e3a1;font-size:1.1rem;font-weight:bold;}"
            + ".rule{margin-top:1.5rem;padding:0.5rem 1rem;background:#181825;border-left:3px solid #cba6f7;}"
            + ".rule-id{color:#cba6f7;font-weight:bold;}"
            + ".finding{padding:0.25rem 0 0.25rem 1rem;}"
            + ".sev-error{color:#f38ba8;}"
            + ".sev-warn{color:#f9e2af;}"
            + ".path{color:#89b4fa;}"
            + ".message{color:#cdd6f4;}"
            + "</style></head><body>";

    private ReportWriter()
    {
    }

    public static void writeTextAndHtml(final Path reportDir, final String taskName,
            final List<RuneliteFinding> findings, final int fileCount) throws IOException
    {
        Files.createDirectories(reportDir);
        final String text = RuneliteRulesReport.format(findings, fileCount, taskName);
        Files.write(reportDir.resolve("findings.txt"), text.getBytes(StandardCharsets.UTF_8));
        final String html = renderHtml(taskName, findings, fileCount);
        Files.write(reportDir.resolve("index.html"), html.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeRawText(final Path reportDir, final String taskName, final String textReport)
            throws IOException
    {
        Files.createDirectories(reportDir);
        Files.write(reportDir.resolve("findings.txt"), textReport.getBytes(StandardCharsets.UTF_8));
        final String html = wrapRawTextHtml(taskName, textReport);
        Files.write(reportDir.resolve("index.html"), html.getBytes(StandardCharsets.UTF_8));
    }

    private static String wrapRawTextHtml(final String taskName, final String textReport)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format(HTML_HEAD_TEMPLATE, escape(taskName)));
        sb.append("<h1>").append(escape(taskName)).append("</h1>");
        sb.append("<pre style=\"background:#11111b;padding:1rem;border-radius:4px;overflow-x:auto;white-space:pre;\">");
        sb.append(escape(textReport));
        sb.append("</pre></body></html>");
        return sb.toString();
    }

    private static String renderHtml(final String taskName, final List<RuneliteFinding> findings,
            final int fileCount)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format(HTML_HEAD_TEMPLATE, escape(taskName)));
        sb.append("<h1>").append(escape(taskName)).append("</h1>");
        sb.append("<div class=\"header\">");
        sb.append("<span class=\"stat\">Scanned: <strong>").append(fileCount).append("</strong> files</span>");
        final int[] counts = countSeverities(findings);
        sb.append("<span class=\"stat errors\">Errors: ").append(counts[0]).append(" (blocking)</span>");
        sb.append("<span class=\"stat warns\">Warnings: ").append(counts[1])
                .append(" (informational)</span>");
        sb.append("</div>");
        if (findings.isEmpty())
        {
            sb.append("<p class=\"clean\">✓ CLEAN — no violations detected.</p>");
        }
        else
        {
            renderFindings(sb, findings);
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static int[] countSeverities(final List<RuneliteFinding> findings)
    {
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
        return new int[]{errors, warns};
    }

    private static void renderFindings(final StringBuilder sb, final List<RuneliteFinding> findings)
    {
        final Path cwd = Paths.get("").toAbsolutePath();
        final Map<String, List<RuneliteFinding>> byRule = groupByRule(findings);
        int n = 1;
        for (final Map.Entry<String, List<RuneliteFinding>> entry : byRule.entrySet())
        {
            sb.append("<div class=\"rule\"><span class=\"rule-id\">[").append(n++).append("] ")
                    .append(escape(entry.getKey())).append("</span>");
            for (final RuneliteFinding f : entry.getValue())
            {
                final String sevClass = f.severity == RuneliteFinding.Severity.ERROR ? "sev-error" : "sev-warn";
                final String sevTag = f.severity == RuneliteFinding.Severity.ERROR ? "ERROR" : "warn";
                sb.append("<div class=\"finding\">[<span class=\"").append(sevClass).append("\">")
                        .append(sevTag).append("</span>] <span class=\"path\">")
                        .append(escape(relativize(cwd, f.file))).append(':').append(f.line)
                        .append("</span> — <span class=\"message\">").append(escape(f.message))
                        .append("</span></div>");
            }
            sb.append("</div>");
        }
    }

    private static Map<String, List<RuneliteFinding>> groupByRule(final List<RuneliteFinding> findings)
    {
        final Map<String, List<RuneliteFinding>> map = new LinkedHashMap<>();
        for (final RuneliteFinding f : findings)
        {
            map.computeIfAbsent(f.ruleId, k -> new ArrayList<>()).add(f);
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

    private static String escape(final String raw)
    {
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
