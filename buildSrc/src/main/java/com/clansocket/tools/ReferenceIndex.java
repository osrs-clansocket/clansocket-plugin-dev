package com.clansocket.tools;

import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ReferenceIndex
{
    private final Set<String> referenced = new HashSet<>();

    ReferenceIndex(final List<ParsedSource> sources)
    {
        for (final ParsedSource s : sources)
        {
            indexOne(s);
        }
    }

    boolean contains(final String name)
    {
        return referenced.contains(name);
    }

    private void indexOne(final ParsedSource s)
    {
        s.unit.findAll(NameExpr.class).forEach(n -> add(n.getNameAsString()));
        s.unit.findAll(FieldAccessExpr.class).forEach(f -> add(f.getNameAsString()));
        s.unit.findAll(MethodCallExpr.class).forEach(m -> add(m.getNameAsString()));
        s.unit.findAll(MethodReferenceExpr.class).forEach(m -> add(m.getIdentifier()));
        s.unit.findAll(ObjectCreationExpr.class).forEach(o -> add(o.getTypeAsString()));
        s.unit.findAll(ClassOrInterfaceType.class).forEach(t -> add(t.getNameAsString()));
    }

    private void add(final String name)
    {
        if (name == null || name.isEmpty())
        {
            return;
        }
        referenced.add(name);
    }
}
