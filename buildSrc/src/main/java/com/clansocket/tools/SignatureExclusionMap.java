package com.clansocket.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SignatureExclusionMap
{
    private final Set<String> excluded;

    private SignatureExclusionMap(final Set<String> excluded)
    {
        this.excluded = excluded;
    }

    public static SignatureExclusionMap load(final Path file) throws IOException
    {
        if (file == null || !Files.exists(file))
        {
            return new SignatureExclusionMap(Collections.emptySet());
        }
        final Set<String> set = new HashSet<>();
        final Map<String, Integer> dupReasons = new HashMap<>();
        final List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (final String raw : lines)
        {
            final String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            final int tab = line.indexOf('\t');
            if (tab <= 0)
            {
                throw new IOException("signature-exclusions: line missing tab-separated reason: " + line);
            }
            final String fqn = line.substring(0, tab).trim();
            final String reason = line.substring(tab + 1).trim();
            if (fqn.isEmpty() || reason.isEmpty())
            {
                throw new IOException("signature-exclusions: blank fqn or reason: " + line);
            }
            if (!set.add(fqn))
            {
                dupReasons.merge(fqn, 1, Integer::sum);
            }
        }
        if (!dupReasons.isEmpty())
        {
            throw new IOException("signature-exclusions: duplicate entries: " + dupReasons.keySet());
        }
        return new SignatureExclusionMap(Collections.unmodifiableSet(set));
    }

    public boolean isExcluded(final String fqn)
    {
        return excluded.contains(fqn);
    }

    public int size()
    {
        return excluded.size();
    }
}
