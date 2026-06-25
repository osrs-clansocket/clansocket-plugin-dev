package com.clansocket.tools;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public abstract class AddFinalsTask extends DefaultTask
{
    @InputFiles
    public abstract ConfigurableFileCollection getSourceRoots();

    @TaskAction
    public void execute() throws IOException
    {
        int processed = 0;
        int changed = 0;
        for (final java.io.File root : getSourceRoots())
        {
            if (!root.exists())
            {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root.toPath()))
            {
                final List<Path> files = new ArrayList<>();
                stream.filter(p -> p.toString().endsWith(".java")).forEach(files::add);
                for (final Path file : files)
                {
                    processed++;
                    final String before = new String(Files.readAllBytes(file));
                    final CompilationUnit cu = StaticJavaParser.parse(before);
                    LexicalPreservingPrinter.setup(cu);

                    cu.findAll(Parameter.class).forEach(p -> {
                        if (p.isFinal())
                        {
                            return;
                        }
                        if (p.getParentNode().map(n -> n instanceof com.github.javaparser.ast.expr.LambdaExpr)
                                .orElse(false))
                        {
                            return;
                        }
                        if (p.getType().isUnknownType())
                        {
                            return;
                        }
                        if (isReassigned(enclosingScope(p), p.getNameAsString()))
                        {
                            return;
                        }
                        try
                        {
                            p.addModifier(Modifier.Keyword.FINAL);
                        }
                        catch (final RuntimeException e)
                        {
                            getLogger().info("skipping param {} in {}: {}", p.getNameAsString(), file, e.getMessage());
                        }
                    });

                    cu.findAll(VariableDeclarationExpr.class).forEach(v -> {
                        if (v.isFinal())
                        {
                            return;
                        }
                        final Optional<Node> parent = v.getParentNode();
                        if (parent.isPresent() && parent.get() instanceof ForStmt)
                        {
                            return;
                        }
                        final Node scope = enclosingScope(v);
                        boolean anyReassigned = false;
                        for (final VariableDeclarator d : v.getVariables())
                        {
                            if (isReassigned(scope, d.getNameAsString()))
                            {
                                anyReassigned = true;
                                break;
                            }
                        }
                        if (anyReassigned)
                        {
                            return;
                        }
                        try
                        {
                            v.addModifier(Modifier.Keyword.FINAL);
                        }
                        catch (final RuntimeException e)
                        {
                            getLogger().info("skipping local in {}: {}", file, e.getMessage());
                        }
                    });

                    final String after = LexicalPreservingPrinter.print(cu);
                    if (!after.equals(before))
                    {
                        Files.write(file, after.getBytes());
                        changed++;
                    }
                }
            }
        }
        getLogger().lifecycle("AddFinals: processed={} changed={}", processed, changed);
    }

    private static Node enclosingScope(final Node n)
    {
        Node cur = n;
        while (cur.getParentNode().isPresent())
        {
            cur = cur.getParentNode().get();
            if (cur instanceof com.github.javaparser.ast.body.MethodDeclaration
                    || cur instanceof com.github.javaparser.ast.body.ConstructorDeclaration
                    || cur instanceof com.github.javaparser.ast.body.InitializerDeclaration
                    || cur instanceof com.github.javaparser.ast.expr.LambdaExpr)
            {
                return cur;
            }
        }
        return n.findCompilationUnit().map(c -> (Node) c).orElse(n);
    }

    private static boolean isReassigned(final Node scope, final String name)
    {
        final Set<String> writes = new HashSet<>();
        scope.findAll(AssignExpr.class).forEach(a -> {
            if (a.getTarget() instanceof NameExpr)
            {
                writes.add(((NameExpr) a.getTarget()).getNameAsString());
            }
        });
        scope.findAll(UnaryExpr.class).forEach(u -> {
            final UnaryExpr.Operator op = u.getOperator();
            if (op == UnaryExpr.Operator.PREFIX_INCREMENT || op == UnaryExpr.Operator.PREFIX_DECREMENT
                    || op == UnaryExpr.Operator.POSTFIX_INCREMENT || op == UnaryExpr.Operator.POSTFIX_DECREMENT)
            {
                if (u.getExpression() instanceof NameExpr)
                {
                    writes.add(((NameExpr) u.getExpression()).getNameAsString());
                }
            }
        });
        return writes.contains(name);
    }
}
