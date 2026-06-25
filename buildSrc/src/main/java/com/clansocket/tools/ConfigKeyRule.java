package com.clansocket.tools;

import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ConfigKeyRule
{
    private ConfigKeyRule()
    {
    }

    static List<RuneliteFinding> scan(final List<ParsedSource> files)
    {
        final Map<String, List<KeyOccurrence>> byKey = new HashMap<>();
        for (final ParsedSource src : files)
        {
            for (final NormalAnnotationExpr ann : src.unit.findAll(NormalAnnotationExpr.class))
            {
                if (!"ConfigItem".equals(ann.getNameAsString()))
                {
                    continue;
                }
                recordKey(byKey, src, ann);
            }
        }
        final List<RuneliteFinding> out = new ArrayList<>();
        for (final Map.Entry<String, List<KeyOccurrence>> e : byKey.entrySet())
        {
            if (e.getValue().size() <= 1)
            {
                continue;
            }
            for (final KeyOccurrence occ : e.getValue())
            {
                out.add(RuneliteFinding.error("plugin-config.duplicate-key-name", occ.src.path, occ.line,
                        "@ConfigItem keyName=\"" + e.getKey() + "\" duplicated; collisions silently overwrite stored values"));
            }
        }
        return out;
    }

    private static void recordKey(final Map<String, List<KeyOccurrence>> byKey, final ParsedSource src,
            final NormalAnnotationExpr ann)
    {
        for (final MemberValuePair pair : ann.getPairs())
        {
            if (!"keyName".equals(pair.getNameAsString()))
            {
                continue;
            }
            if (!(pair.getValue() instanceof StringLiteralExpr))
            {
                continue;
            }
            final String key = ((StringLiteralExpr) pair.getValue()).getValue();
            final int line = ann.getBegin().map(p -> p.line).orElse(0);
            byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(new KeyOccurrence(src, line));
        }
    }

    private static final class KeyOccurrence
    {
        final ParsedSource src;
        final int line;

        KeyOccurrence(final ParsedSource src, final int line)
        {
            this.src = src;
            this.line = line;
        }
    }
}
