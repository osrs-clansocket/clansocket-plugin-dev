package com.clansocket.tools;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

final class CallPatternScanner
{
    private static final Set<String> TRIVIAL_CALLEES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "append", "toString", "length", "size", "isEmpty", "get", "put", "add", "equals", "hashCode", "compareTo",
            "valueOf", "format", "isPresent", "orElse", "map", "filter", "stream", "of", "iterator", "next", "hasNext",
            "setFont", "setForeground", "setBackground", "setBorder", "setLayout", "setOpaque", "setVisible", "setText",
            "setAlignmentY", "setAlignmentX", "setPreferredSize", "setMaximumSize", "setMinimumSize", "setEnabled",
            "getRunescapeFont", "getRunescapeSmallFont", "getRunescapeBoldFont", "accept", "debug", "warn", "info",
            "error", "getMessage", "addItem", "addSection", "computeIfAbsent")));

    private final int minDistinctCallees;
    private final int minTotalCalls;

    CallPatternScanner(final int minDistinctCallees)
    {
        this.minDistinctCallees = minDistinctCallees;
        this.minTotalCalls = minDistinctCallees + 1;
    }

    List<DupMatch> scan(final List<ParsedSource> files)
    {
        final Map<String, List<MethodEntry>> index = new HashMap<>();
        for (final ParsedSource f : files)
        {
            for (final MethodDeclaration m : f.unit.findAll(MethodDeclaration.class))
            {
                if (!m.getBody().isPresent())
                {
                    continue;
                }
                final String key = patternKey(m.getBody().get());
                if (key == null)
                {
                    continue;
                }
                index.computeIfAbsent(key, k -> new ArrayList<>()).add(new MethodEntry(f, m));
            }
        }

        final List<DupMatch> out = new ArrayList<>();
        for (final Map.Entry<String, List<MethodEntry>> e : index.entrySet())
        {
            final List<MethodEntry> group = e.getValue();
            if (group.size() < 2 || !hasCrossFile(group))
            {
                continue;
            }
            out.add(toMatch(e.getKey(), group));
        }
        return out;
    }

    private String patternKey(final Node body)
    {
        final TreeMap<String, Integer> multiset = new TreeMap<>();
        for (final MethodCallExpr call : body.findAll(MethodCallExpr.class))
        {
            final String name = call.getNameAsString();
            if (TRIVIAL_CALLEES.contains(name))
            {
                continue;
            }
            multiset.merge(name, 1, Integer::sum);
        }
        int total = 0;
        for (final int n : multiset.values())
        {
            total += n;
        }
        if (multiset.size() < minDistinctCallees || total < minTotalCalls)
        {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, Integer> e : multiset.entrySet())
        {
            sb.append(e.getKey()).append(':').append(e.getValue()).append(';');
        }
        return sb.toString();
    }

    private static boolean hasCrossFile(final List<MethodEntry> group)
    {
        final Set<String> files = new HashSet<>();
        for (final MethodEntry e : group)
        {
            files.add(e.file.path.toString());
        }
        return files.size() > 1;
    }

    private static DupMatch toMatch(final String pattern, final List<MethodEntry> group)
    {
        final List<DupMatch.Location> locs = new ArrayList<>();
        for (final MethodEntry e : group)
        {
            final int start = e.method.getBegin().map(p -> p.line).orElse(0);
            final int end = e.method.getEnd().map(p -> p.line).orElse(0);
            locs.add(new DupMatch.Location(e.file.path, start, end, e.method.getNameAsString() + "()"));
        }
        return new DupMatch(DupMatch.Kind.CALL_PATTERN, group.size(), "calls " + pattern.replace(";", " "), locs);
    }

    private static final class MethodEntry
    {
        final ParsedSource file;
        final MethodDeclaration method;

        MethodEntry(final ParsedSource file, final MethodDeclaration method)
        {
            this.file = file;
            this.method = method;
        }
    }
}
