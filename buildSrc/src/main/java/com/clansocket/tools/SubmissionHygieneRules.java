package com.clansocket.tools;

import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.ArrayList;
import java.util.List;

final class SubmissionHygieneRules
{
    private SubmissionHygieneRules()
    {
    }

    static List<RuneliteFinding> scan(final ParsedSource src)
    {
        final List<RuneliteFinding> out = new ArrayList<>();
        scanSuppressWarnings(src, out);
        scanComments(src, out);
        return out;
    }

    private static void scanSuppressWarnings(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final AnnotationExpr ann : src.unit.findAll(AnnotationExpr.class))
        {
            if (!"SuppressWarnings".equals(ann.getNameAsString()))
            {
                continue;
            }
            final int line = ann.getBegin().map(p -> p.line).orElse(0);
            out.add(RuneliteFinding.warn("submission.suppress-warnings-present", src.path, line,
                    "@SuppressWarnings present — strip-pass must remove before plugin-hub submission"));
        }
    }

    private static void scanComments(final ParsedSource src, final List<RuneliteFinding> out)
    {
        for (final Comment c : src.unit.getAllContainedComments())
        {
            final int line = c.getBegin().map(p -> p.line).orElse(0);
            out.add(RuneliteFinding.warn("submission.comment-present", src.path, line,
                    "comment present — strip-pass removes all // /* */ before submission"));
        }
    }
}
