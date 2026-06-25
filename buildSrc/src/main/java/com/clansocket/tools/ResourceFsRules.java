package com.clansocket.tools;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ResourceFsRules
{
    private static final Set<String> WRITER_TYPES = new HashSet<>(
            Arrays.asList("FileOutputStream", "FileWriter", "PrintWriter", "RandomAccessFile"));
    private static final Set<String> WRITE_CALLS = new HashSet<>(
            Arrays.asList("write", "writeString", "createDirectories", "createFile", "createTempFile", "newOutputStream",
                    "newBufferedWriter", "copy", "move"));
    private static final Set<String> SAFE_ROOT_HINTS = new HashSet<>(Arrays.asList("RUNELITE_DIR", "PRESETS_DIR",
            "PRESET_DIR", "PLUGIN_DIR", "CLANSOCKET_DIR", "RuneLite.RUNELITE_DIR"));

    private ResourceFsRules()
    {
    }

    static List<RuneliteFinding> scan(final ParsedSource src)
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        scanGetResource(src, out);
        scanFsWrites(src, out);
        return out;
    }

    private static void scanGetResource(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final MethodCallExpr call : src.unit.findAll(MethodCallExpr.class))
        {
            if (!"getResource".equals(call.getNameAsString()) || call.getArguments().size() != 1)
            {
                continue;
            }
            final int line = call.getBegin().map(p -> p.line).orElse(0);
            out.add(RuneliteFinding.error("plugin-resources.get-resource", src.path, line,
                    "getResource(...) banned; use getResourceAsStream(...) or ImageUtil.loadImageResource(...)"));
        }
    }

    private static void scanFsWrites(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final ObjectCreationExpr ctor : src.unit.findAll(ObjectCreationExpr.class))
        {
            final String typeName = ctor.getType().getNameAsString();
            if (!WRITER_TYPES.contains(typeName))
            {
                continue;
            }
            if (referencesSafeRoot(ctor.toString()))
            {
                continue;
            }
            final int line = ctor.getBegin().map(p -> p.line).orElse(0);
            out.add(RuneliteFinding.warn("filesystem-rules.write-outside-plugin-dir", src.path, line,
                    "new " + typeName + " — verify path is under RuneLite.RUNELITE_DIR/<plugin>/"));
        }
        for (final MethodCallExpr call : src.unit.findAll(MethodCallExpr.class))
        {
            if (!WRITE_CALLS.contains(call.getNameAsString()))
            {
                continue;
            }
            if (!isNioWrite(call))
            {
                continue;
            }
            if (referencesSafeRoot(call.toString()))
            {
                continue;
            }
            final int line = call.getBegin().map(p -> p.line).orElse(0);
            out.add(RuneliteFinding.warn("filesystem-rules.write-outside-plugin-dir", src.path, line,
                    "Files." + call.getNameAsString() + "(...) — verify path is under RuneLite.RUNELITE_DIR/<plugin>/"));
        }
    }

    private static boolean isNioWrite(final MethodCallExpr call)
    {
        return call.getScope().filter(s -> s.toString().equals("Files")).isPresent();
    }

    private static boolean referencesSafeRoot(final String snippet)
    {
        for (final String hint : SAFE_ROOT_HINTS)
        {
            if (snippet.contains(hint))
            {
                return true;
            }
        }
        return false;
    }
}
