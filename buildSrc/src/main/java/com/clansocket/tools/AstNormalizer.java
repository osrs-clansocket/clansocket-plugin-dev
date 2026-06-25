package com.clansocket.tools;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class AstNormalizer
{
    private final int minNodes;

    AstNormalizer(final int minNodes)
    {
        this.minNodes = minNodes;
    }

    List<DupMatch> scan(final List<ParsedSource> files)
    {
        final Map<String, List<MethodRef>> index = new HashMap<>();
        for (final ParsedSource f : files)
        {
            for (final MethodDeclaration m : f.unit.findAll(MethodDeclaration.class))
            {
                if (!m.getBody().isPresent())
                {
                    continue;
                }
                final Node body = m.getBody().get();
                final int nodeCount = countNodes(body);
                if (nodeCount < minNodes)
                {
                    continue;
                }
                final String hash = normalize(body);
                index.computeIfAbsent(hash, k -> new ArrayList<>()).add(new MethodRef(f.path, m, nodeCount));
            }
        }

        final List<DupMatch> candidates = new ArrayList<>();
        for (final Map.Entry<String, List<MethodRef>> e : index.entrySet())
        {
            if (e.getValue().size() < 2)
            {
                continue;
            }
            candidates.add(buildMatch(e.getValue()));
        }
        return DupMerger.merge(candidates);
    }

    private DupMatch buildMatch(final List<MethodRef> refs)
    {
        final List<DupMatch.Location> locs = new ArrayList<>();
        int size = 0;
        for (final MethodRef r : refs)
        {
            final int start = r.method.getBegin().map(p -> p.line).orElse(0);
            final int end = r.method.getEnd().map(p -> p.line).orElse(0);
            locs.add(new DupMatch.Location(r.file, start, end, r.method.getNameAsString() + "()"));
            size = r.nodeCount;
        }
        return new DupMatch(DupMatch.Kind.LOGICAL, size, "method body (" + size + " AST nodes)", locs);
    }

    private int countNodes(final Node n)
    {
        int count = 1;
        for (final Node child : n.getChildNodes())
        {
            count += countNodes(child);
        }
        return count;
    }

    private String normalize(final Node n)
    {
        final StringBuilder sb = new StringBuilder();
        appendNormalized(n, sb);
        return sb.toString();
    }

    private void appendNormalized(final Node n, final StringBuilder sb)
    {
        sb.append(tokenFor(n));
        sb.append('(');
        final List<Node> children = n.getChildNodes();
        for (int i = 0; i < children.size(); i++)
        {
            if (i > 0)
            {
                sb.append(',');
            }
            appendNormalized(children.get(i), sb);
        }
        sb.append(')');
    }

    private String tokenFor(final Node n)
    {
        if (n instanceof SimpleName)
        {
            return "IDENT";
        }
        if (n instanceof StringLiteralExpr)
        {
            return "LIT_STR";
        }
        if (n instanceof IntegerLiteralExpr || n instanceof LongLiteralExpr || n instanceof DoubleLiteralExpr)
        {
            return "LIT_NUM";
        }
        if (n instanceof BooleanLiteralExpr)
        {
            return "LIT_BOOL";
        }
        if (n instanceof NullLiteralExpr)
        {
            return "LIT_NULL";
        }
        if (n instanceof CharLiteralExpr)
        {
            return "LIT_CHAR";
        }
        return n.getClass().getSimpleName();
    }

    static final class MethodRef
    {
        final Path file;
        final MethodDeclaration method;
        final int nodeCount;

        MethodRef(final Path file, final MethodDeclaration method, final int nodeCount)
        {
            this.file = file;
            this.method = method;
            this.nodeCount = nodeCount;
        }
    }
}
