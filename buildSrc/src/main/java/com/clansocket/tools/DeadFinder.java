package com.clansocket.tools;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class DeadFinder
{
    private static final Set<String> LOMBOK_CLASS = new HashSet<>(Arrays.asList(
        "Data", "Value", "Builder", "Getter", "Setter",
        "RequiredArgsConstructor", "NoArgsConstructor", "AllArgsConstructor",
        "EqualsAndHashCode", "ToString", "Slf4j", "Log4j", "Log4j2", "Log"));

    private static final Set<String> CLASS_ENTRY_POINT = new HashSet<>(Arrays.asList(
        "PluginDescriptor"));

    private static final Set<String> SKIP_METHOD_ANN = new HashSet<>(Arrays.asList(
        "Subscribe", "Inject", "Provides", "ConfigItem", "ConfigGroup", "ConfigSection",
        "ConfigTitleSection", "Override", "Test", "Property", "Before", "After", "BeforeClass", "AfterClass",
        "BeforeEach", "AfterEach", "ParameterizedTest", "RepeatedTest", "TestFactory",
        "PostConstruct", "PreDestroy", "Bean", "ProvidesIntoSet", "ProvidesIntoMap"));

    private static final Set<String> SKIP_FIELD_ANN = new HashSet<>(Arrays.asList(
        "Inject", "Provides", "ConfigItem", "Getter", "Setter"));

    private static final Set<String> SKIP_METHOD_NAMES = new HashSet<>(Arrays.asList(
        "equals", "hashCode", "toString", "clone", "finalize",
        "readObject", "writeObject", "readResolve", "writeReplace",
        "startUp", "shutDown", "main"));

    private final ReferenceIndex refs;

    DeadFinder(final ReferenceIndex refs)
    {
        this.refs = refs;
    }

    List<UnusedFinding> scan(final List<ParsedSource> sources)
    {
        final List<UnusedFinding> out = new ArrayList<>();
        for (final ParsedSource s : sources)
        {
            for (final TypeDeclaration<?> td : s.unit.getTypes())
            {
                scanType(s, td, out);
            }
        }
        return out;
    }

    private void scanType(final ParsedSource s, final TypeDeclaration<?> td, final List<UnusedFinding> out)
    {
        if (td instanceof AnnotationDeclaration)
        {
            return;
        }
        if (hasAnyAnn(td, LOMBOK_CLASS))
        {
            return;
        }
        if (!hasAnyAnn(td, CLASS_ENTRY_POINT) && !hasMainOrTest(td))
        {
            checkType(s, td, out);
        }
        if (td instanceof ClassOrInterfaceDeclaration)
        {
            final ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) td;
            if (!cid.isInterface())
            {
                scanMembers(s, cid, out);
            }
        }
        else if (td instanceof EnumDeclaration)
        {
            scanEnum(s, (EnumDeclaration) td, out);
        }
    }

    private void checkType(final ParsedSource s, final TypeDeclaration<?> td, final List<UnusedFinding> out)
    {
        if (td.getAccessSpecifier() == AccessSpecifier.PRIVATE)
        {
            return;
        }
        final String name = td.getNameAsString();
        if (refs.contains(name))
        {
            return;
        }
        out.add(new UnusedFinding(UnusedFinding.Kind.CLASS, name,
            s.path, td.getBegin().map(p -> p.line).orElse(0)));
    }

    private void scanMembers(final ParsedSource s, final ClassOrInterfaceDeclaration cid, final List<UnusedFinding> out)
    {
        for (final MethodDeclaration m : cid.getMethods())
        {
            checkMethod(s, m, out);
        }
        for (final FieldDeclaration f : cid.getFields())
        {
            checkField(s, f, out);
        }
        for (final BodyDeclaration<?> member : cid.getMembers())
        {
            if (member instanceof TypeDeclaration)
            {
                scanType(s, (TypeDeclaration<?>) member, out);
            }
        }
    }

    private void checkMethod(final ParsedSource s, final MethodDeclaration m, final List<UnusedFinding> out)
    {
        if (m.isAbstract() || m.isPrivate())
        {
            return;
        }
        if (hasAnyAnn(m, SKIP_METHOD_ANN))
        {
            return;
        }
        final String name = m.getNameAsString();
        if (SKIP_METHOD_NAMES.contains(name))
        {
            return;
        }
        if (refs.contains(name))
        {
            return;
        }
        out.add(new UnusedFinding(UnusedFinding.Kind.METHOD, name + "()",
            s.path, m.getBegin().map(p -> p.line).orElse(0)));
    }

    private void checkField(final ParsedSource s, final FieldDeclaration f, final List<UnusedFinding> out)
    {
        if (f.isPrivate())
        {
            return;
        }
        if (hasAnyAnn(f, SKIP_FIELD_ANN))
        {
            return;
        }
        for (final VariableDeclarator v : f.getVariables())
        {
            final String name = v.getNameAsString();
            if (refs.contains(name))
            {
                continue;
            }
            out.add(new UnusedFinding(UnusedFinding.Kind.FIELD, name,
                s.path, f.getBegin().map(p -> p.line).orElse(0)));
        }
    }

    private void scanEnum(final ParsedSource s, final EnumDeclaration ed, final List<UnusedFinding> out)
    {
        for (final EnumConstantDeclaration c : ed.getEntries())
        {
            final String name = c.getNameAsString();
            if (refs.contains(name))
            {
                continue;
            }
            out.add(new UnusedFinding(UnusedFinding.Kind.ENUM_CONST, name,
                s.path, c.getBegin().map(p -> p.line).orElse(0)));
        }
        for (final MethodDeclaration m : ed.getMethods())
        {
            checkMethod(s, m, out);
        }
    }

    private boolean hasMainOrTest(final TypeDeclaration<?> td)
    {
        if (!(td instanceof ClassOrInterfaceDeclaration))
        {
            return false;
        }
        for (final MethodDeclaration m : ((ClassOrInterfaceDeclaration) td).getMethods())
        {
            if (isMain(m))
            {
                return true;
            }
            for (final AnnotationExpr a : m.getAnnotations())
            {
                final String n = a.getNameAsString();
                if ("Test".equals(n) || "Property".equals(n) || "ParameterizedTest".equals(n) || "RepeatedTest".equals(n) || "TestFactory".equals(n))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMain(final MethodDeclaration m)
    {
        return "main".equals(m.getNameAsString()) && m.isStatic() && m.getParameters().size() == 1;
    }

    private boolean hasAnyAnn(final NodeWithAnnotations<?> node, final Set<String> names)
    {
        for (final AnnotationExpr a : node.getAnnotations())
        {
            if (names.contains(a.getNameAsString()))
            {
                return true;
            }
        }
        return false;
    }
}
