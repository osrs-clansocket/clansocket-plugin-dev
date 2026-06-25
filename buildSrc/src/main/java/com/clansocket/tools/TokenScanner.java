package com.clansocket.tools;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.ImportDeclaration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class TokenScanner
{
    private final int minTokens;

    TokenScanner(final int minTokens)
    {
        this.minTokens = minTokens;
    }

    List<DupMatch> scan(final List<ParsedSource> files)
    {
        final List<FileTokens> streams = new ArrayList<>();
        for (final ParsedSource f : files)
        {
            streams.add(extractTokens(f));
        }

        final Map<String, List<Occurrence>> index = new HashMap<>();
        for (final FileTokens ft : streams)
        {
            if (ft.tokens.size() < minTokens)
            {
                continue;
            }
            for (int i = 0; i + minTokens <= ft.tokens.size(); i++)
            {
                final String hash = hashWindow(ft.tokens, i);
                index.computeIfAbsent(hash, k -> new ArrayList<>()).add(new Occurrence(ft, i));
            }
        }

        final List<DupMatch> candidates = new ArrayList<>();
        for (final Map.Entry<String, List<Occurrence>> e : index.entrySet())
        {
            final List<Occurrence> deduped = dedupePerFile(e.getValue());
            if (deduped.size() < 2)
            {
                continue;
            }
            candidates.add(buildMatch(deduped));
        }
        return DupMerger.merge(candidates);
    }

    private List<Occurrence> dedupePerFile(final List<Occurrence> raw)
    {
        final Map<Path, List<Occurrence>> byFile = new HashMap<>();
        for (final Occurrence o : raw)
        {
            byFile.computeIfAbsent(o.ft.path, k -> new ArrayList<>()).add(o);
        }
        final List<Occurrence> out = new ArrayList<>();
        for (final List<Occurrence> list : byFile.values())
        {
            list.sort((a, b) -> Integer.compare(a.start, b.start));
            int lastEnd = Integer.MIN_VALUE;
            for (final Occurrence o : list)
            {
                if (o.start >= lastEnd)
                {
                    out.add(o);
                    lastEnd = o.start + minTokens;
                }
            }
        }
        return out;
    }

    private DupMatch buildMatch(final List<Occurrence> deduped)
    {
        final List<DupMatch.Location> locs = new ArrayList<>();
        for (final Occurrence occ : deduped)
        {
            final int startLine = occ.ft.tokens.get(occ.start).line;
            final int endIdx = Math.min(occ.start + minTokens - 1, occ.ft.tokens.size() - 1);
            final int endLine = occ.ft.tokens.get(endIdx).line;
            locs.add(new DupMatch.Location(occ.ft.path, startLine, endLine, ""));
        }
        return new DupMatch(DupMatch.Kind.IDENTICAL, minTokens, minTokens + " tokens", locs);
    }

    private FileTokens extractTokens(final ParsedSource f)
    {
        final FileTokens ft = new FileTokens();
        ft.path = f.path;
        final Set<Integer> importLines = collectImportLines(f);
        if (!f.unit.getTokenRange().isPresent())
        {
            return ft;
        }
        for (final JavaToken t : f.unit.getTokenRange().get())
        {
            if (isTrivia(t) || isImportLine(t, importLines))
            {
                continue;
            }
            final Tok tok = new Tok();
            tok.text = t.getText();
            tok.kind = t.getKind();
            tok.line = t.getRange().map(r -> r.begin.line).orElse(0);
            ft.tokens.add(tok);
        }
        return ft;
    }

    private Set<Integer> collectImportLines(final ParsedSource f)
    {
        final Set<Integer> lines = new HashSet<>();
        for (final ImportDeclaration imp : f.unit.getImports())
        {
            final Optional<com.github.javaparser.Range> range = imp.getRange();
            if (!range.isPresent())
            {
                continue;
            }
            final int begin = range.get().begin.line;
            final int end = range.get().end.line;
            for (int l = begin; l <= end; l++)
            {
                lines.add(l);
            }
        }
        return lines;
    }

    private boolean isImportLine(final JavaToken t, final Set<Integer> importLines)
    {
        if (importLines.isEmpty())
        {
            return false;
        }
        final Optional<com.github.javaparser.Range> r = t.getRange();
        return r.isPresent() && importLines.contains(r.get().begin.line);
    }

    private boolean isTrivia(final JavaToken t)
    {
        final JavaToken.Category cat = t.getCategory();
        return Arrays.asList(JavaToken.Category.WHITESPACE_NO_EOL, JavaToken.Category.EOL,
                JavaToken.Category.COMMENT).contains(cat);
    }

    private String hashWindow(final List<Tok> tokens, final int start)
    {
        final StringBuilder sb = new StringBuilder(minTokens * 8);
        for (int i = start; i < start + minTokens; i++)
        {
            sb.append(tokens.get(i).kind).append(':').append(tokens.get(i).text).append('|');
        }
        return sb.toString();
    }

    static final class FileTokens
    {
        Path path;
        final List<Tok> tokens = new ArrayList<>();
    }

    static final class Tok
    {
        String text;
        int kind;
        int line;
    }

    static final class Occurrence
    {
        final FileTokens ft;
        final int start;

        Occurrence(final FileTokens ft, final int start)
        {
            this.ft = ft;
            this.start = start;
        }
    }
}
