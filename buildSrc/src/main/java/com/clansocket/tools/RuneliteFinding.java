package com.clansocket.tools;

import java.nio.file.Path;

public final class RuneliteFinding
{
    public enum Severity { ERROR, WARN }

    public final String ruleId;
    public final Severity severity;
    public final Path file;
    public final int line;
    public final String message;

    public RuneliteFinding(final String ruleId, final Severity severity, final Path file, final int line,
            final String message)
    {
        this.ruleId = ruleId;
        this.severity = severity;
        this.file = file;
        this.line = line;
        this.message = message;
    }

    public static RuneliteFinding error(final String ruleId, final Path file, final int line, final String message)
    {
        return new RuneliteFinding(ruleId, Severity.ERROR, file, line, message);
    }

    public static RuneliteFinding warn(final String ruleId, final Path file, final int line, final String message)
    {
        return new RuneliteFinding(ruleId, Severity.WARN, file, line, message);
    }
}
