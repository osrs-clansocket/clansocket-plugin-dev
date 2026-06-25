package com.clansocket.tools;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ForbiddenLanguageRules
{
    private static final String REFLECT_PKG = "java.lang.reflect";
    private static final String REFLECT_TYPE_ALLOWED = "java.lang.reflect.Type";
    private static final Set<String> BANNED_TYPES = new HashSet<>(Arrays.asList("ProcessBuilder", "URLClassLoader"));

    private ForbiddenLanguageRules()
    {
    }

    static List<RuneliteFinding> scan(final ParsedSource src)
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        scanReflectImports(src, out);
        scanForbiddenCalls(src, out);
        scanForbiddenConstructions(src, out);
        scanNativeMethods(src, out);
        return out;
    }

    private static void scanReflectImports(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final ImportDeclaration imp : src.unit.getImports())
        {
            final String name = imp.getNameAsString();
            final int line = imp.getBegin().map(p -> p.line).orElse(0);
            if (name.equals(REFLECT_TYPE_ALLOWED))
            {
                continue;
            }
            if (name.startsWith(REFLECT_PKG + ".") || name.equals(REFLECT_PKG))
            {
                out.add(RuneliteFinding.error("forbidden-lang.reflection", src.path, line,
                        "java.lang.reflect.* banned (except Type for Gson TypeToken)"));
            }
        }
    }

    private static void scanForbiddenCalls(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final MethodCallExpr call : src.unit.findAll(MethodCallExpr.class))
        {
            final String name = call.getNameAsString();
            final int line = call.getBegin().map(p -> p.line).orElse(0);
            if (("loadLibrary".equals(name) || "load".equals(name))
                    && ForbiddenApiRules.isScopeName(call, "System"))
            {
                out.add(RuneliteFinding.error("forbidden-lang.system-load", src.path, line,
                        "System.loadLibrary / System.load (JNI) banned"));
            }
            else if ("exec".equals(name) && isRuntimeExec(call))
            {
                out.add(RuneliteFinding.error("forbidden-lang.runtime-exec", src.path, line,
                        "Runtime.exec(...) banned (no subprocess execution)"));
            }
        }
    }

    private static boolean isRuntimeExec(final MethodCallExpr call)
    {
        return call.getScope().filter(s -> s.toString().contains("Runtime")).isPresent();
    }

    private static void scanForbiddenConstructions(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final ObjectCreationExpr ctor : src.unit.findAll(ObjectCreationExpr.class))
        {
            final String typeName = ctor.getType().getNameAsString();
            if (!BANNED_TYPES.contains(typeName))
            {
                continue;
            }
            final int line = ctor.getBegin().map(p -> p.line).orElse(0);
            out.add(RuneliteFinding.error("forbidden-lang." + typeName.toLowerCase(java.util.Locale.ROOT), src.path,
                    line, "new " + typeName + "(...) banned"));
        }
        for (final NameExpr ne : src.unit.findAll(NameExpr.class))
        {
            if (BANNED_TYPES.contains(ne.getNameAsString()))
            {
                final int line = ne.getBegin().map(p -> p.line).orElse(0);
                out.add(RuneliteFinding.warn("forbidden-lang.type-reference", src.path, line,
                        "reference to " + ne.getNameAsString() + " — banned class"));
            }
        }
    }

    private static void scanNativeMethods(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final MethodDeclaration m : src.unit.findAll(MethodDeclaration.class))
        {
            if (m.getModifiers().contains(Modifier.nativeModifier()))
            {
                final int line = m.getBegin().map(p -> p.line).orElse(0);
                out.add(RuneliteFinding.error("forbidden-lang.native-method", src.path, line,
                        "native methods banned (JNI not allowed)"));
            }
        }
    }
}
