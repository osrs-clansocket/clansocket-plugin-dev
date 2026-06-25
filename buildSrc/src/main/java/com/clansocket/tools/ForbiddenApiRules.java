package com.clansocket.tools;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

import java.util.ArrayList;
import java.util.List;

final class ForbiddenApiRules
{
    private static final String JAVAX_SOUND_PKG = "javax.sound.sampled";

    private ForbiddenApiRules()
    {
    }

    static List<RuneliteFinding> scan(final ParsedSource src)
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        scanImports(src, out);
        scanFieldAccess(src, out);
        scanMethodCalls(src, out);
        return out;
    }

    private static void scanImports(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final ImportDeclaration imp : src.unit.getImports())
        {
            final String name = imp.getNameAsString();
            final int line = imp.getBegin().map(p -> p.line).orElse(0);
            if (name.startsWith(JAVAX_SOUND_PKG))
            {
                out.add(RuneliteFinding.error("forbidden-api.javax-sound", src.path, line,
                        "javax.sound.sampled banned; use net.runelite.client.audio.AudioPlayer"));
            }
        }
    }

    private static void scanFieldAccess(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final FieldAccessExpr fae : src.unit.findAll(FieldAccessExpr.class))
        {
            if (isSystemOutOrErr(fae))
            {
                final int line = fae.getBegin().map(p -> p.line).orElse(0);
                out.add(RuneliteFinding.error("forbidden-api.system-out", src.path, line,
                        "System.out / System.err banned; use slf4j logger (log.info/debug/error)"));
            }
        }
    }

    private static boolean isSystemOutOrErr(final FieldAccessExpr fae)
    {
        final String name = fae.getNameAsString();
        if (!"out".equals(name) && !"err".equals(name))
        {
            return false;
        }
        return fae.getScope() instanceof NameExpr && "System".equals(((NameExpr) fae.getScope()).getNameAsString());
    }

    private static void scanMethodCalls(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final MethodCallExpr call : src.unit.findAll(MethodCallExpr.class))
        {
            final String name = call.getNameAsString();
            final int line = call.getBegin().map(p -> p.line).orElse(0);
            if ("printStackTrace".equals(name) && call.getArguments().isEmpty())
            {
                out.add(RuneliteFinding.error("forbidden-api.print-stack-trace", src.path, line,
                        "printStackTrace() banned; use log.error(\"...\", e)"));
            }
            else if ("forName".equals(name) && isScopeName(call, "Class"))
            {
                out.add(RuneliteFinding.error("forbidden-api.class-forname", src.path, line,
                        "Class.forName(...) reflection banned"));
            }
            else if ("setAccessible".equals(name))
            {
                out.add(RuneliteFinding.error("forbidden-api.set-accessible", src.path, line,
                        ".setAccessible(...) banned (reflection)"));
            }
            else if ("exit".equals(name) && isScopeName(call, "System"))
            {
                out.add(RuneliteFinding.error("forbidden-api.system-exit", src.path, line,
                        "System.exit(...) banned — would kill the runelite client"));
            }
        }
    }

    static boolean isScopeName(final MethodCallExpr call, final String scopeName)
    {
        return call.getScope().filter(s -> s instanceof NameExpr)
                .map(s -> scopeName.equals(((NameExpr) s).getNameAsString())).orElse(false);
    }

    @SuppressWarnings("unused")
    static CompilationUnit nop(final ParsedSource src)
    {
        return src.unit;
    }
}
