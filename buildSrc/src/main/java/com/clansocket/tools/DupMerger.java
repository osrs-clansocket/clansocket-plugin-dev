package com.clansocket.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class DupMerger
{
    private DupMerger()
    {
    }

    static List<DupMatch> merge(final List<DupMatch> candidates)
    {
        final List<DupMatch> normalized = new ArrayList<>();
        for (final DupMatch m : candidates)
        {
            normalized.add(withSortedLocations(m));
        }
        final List<DupMatch> kept = new ArrayList<>();
        for (final DupMatch c : normalized)
        {
            int hit = -1;
            for (int i = 0; i < kept.size(); i++)
            {
                if (canMerge(kept.get(i), c))
                {
                    hit = i;
                    break;
                }
            }
            if (hit >= 0)
            {
                kept.set(hit, doMerge(kept.get(hit), c));
            }
            else
            {
                kept.add(c);
            }
        }
        return kept;
    }

    private static DupMatch withSortedLocations(final DupMatch m)
    {
        final List<DupMatch.Location> sorted = new ArrayList<>(m.locations);
        sorted.sort(Comparator.comparing((DupMatch.Location l) -> l.file.toString())
            .thenComparingInt(l -> l.startLine));
        return new DupMatch(m.kind, m.size, m.summary, sorted);
    }

    private static boolean canMerge(final DupMatch a, final DupMatch b)
    {
        if (a.kind != b.kind)
        {
            return false;
        }
        if (a.locations.size() != b.locations.size())
        {
            return false;
        }
        for (int i = 0; i < a.locations.size(); i++)
        {
            final DupMatch.Location la = a.locations.get(i);
            final DupMatch.Location lb = b.locations.get(i);
            if (!la.file.equals(lb.file))
            {
                return false;
            }
            if (la.endLine < lb.startLine || lb.endLine < la.startLine)
            {
                return false;
            }
        }
        return true;
    }

    private static DupMatch doMerge(final DupMatch a, final DupMatch b)
    {
        final List<DupMatch.Location> merged = new ArrayList<>();
        for (int i = 0; i < a.locations.size(); i++)
        {
            final DupMatch.Location la = a.locations.get(i);
            final DupMatch.Location lb = b.locations.get(i);
            merged.add(new DupMatch.Location(la.file,
                Math.min(la.startLine, lb.startLine),
                Math.max(la.endLine, lb.endLine),
                la.label.isEmpty() ? lb.label : la.label));
        }
        final int span = merged.get(0).endLine - merged.get(0).startLine + 1;
        return new DupMatch(a.kind, span, span + " lines", merged);
    }
}
