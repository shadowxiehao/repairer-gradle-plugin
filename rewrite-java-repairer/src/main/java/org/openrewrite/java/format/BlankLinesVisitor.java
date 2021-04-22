/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.format;

import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.style.BlankLinesStyle;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;

import java.util.Iterator;
import java.util.List;

class BlankLinesVisitor<P> extends JavaIsoVisitor<P> {
    @Nullable
    private final Tree stopAfter;

    private final BlankLinesStyle style;

    public BlankLinesVisitor(BlankLinesStyle style) {
        this(style, null);
    }

    public BlankLinesVisitor(BlankLinesStyle style, @Nullable Tree stopAfter) {
        this.style = style;
        this.stopAfter = stopAfter;
    }

    @Override
    public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, P p) {
        J.CompilationUnit j = cu;
        if (j.getPackageDeclaration() != null) {
            if (!j.getPrefix().getComments().isEmpty()) {
                j = j.withComments(ListUtils.mapLast(j.getComments(), c -> {
                    String suffix = keepMaximumLines(c.getSuffix(), style.getKeepMaximum().getBetweenHeaderAndPackage());
                    suffix = minimumLines(suffix, style.getMinimum().getBeforePackage());
                    return c.withSuffix(suffix);
                }));
            } else {
                /*
                 if comments are empty and package is present, leading whitespace is on the compilation unit and
                 should be removed
                 */
                j = j.withPrefix(Space.EMPTY);
            }
        }

        if (j.getPackageDeclaration() == null) {
            if (j.getComments().isEmpty()) {
                /*
                if package decl and comments are null/empty, leading whitespace is on the
                compilation unit and should be removed
                 */
                j = j.withPrefix(Space.EMPTY);
            } else {
                j = j.withComments(ListUtils.mapLast(j.getComments(), c ->
                        c.withSuffix(minimumLines(c.getSuffix(), style.getMinimum().getBeforeImports()))));
            }
        } else {
            j = j.getPadding().withImports(ListUtils.mapFirst(j.getPadding().getImports(), i ->
                    minimumLines(i, style.getMinimum().getAfterPackage())));
        }
        return super.visitCompilationUnit(j, p);
    }

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, P p) {
        J.ClassDeclaration j = super.visitClassDeclaration(classDecl, p);
        List<JRightPadded<Statement>> statements = j.getBody().getPadding().getStatements();
        j = j.withBody(j.getBody().getPadding().withStatements(ListUtils.map(statements, (i, s) -> {
            if (i == 0) {
                s = minimumLines(s, style.getMinimum().getAfterClassHeader());
            } else if (statements.get(i - 1).getElement() instanceof J.Block) {
                s = minimumLines(s, style.getMinimum().getAroundInitializer());
            }

            return s;
        })));

        j = j.withBody(j.getBody().withEnd(minimumLines(j.getBody().getEnd(),
                style.getMinimum().getBeforeClassEnd())));

        J.CompilationUnit cu = getCursor().firstEnclosingOrThrow(J.CompilationUnit.class);
        boolean hasImports = !cu.getImports().isEmpty();
        boolean firstClass = j.equals(cu.getClasses().get(0));

        j = firstClass ?
                (hasImports ? minimumLines(j, style.getMinimum().getAfterImports()) : j) :
                minimumLines(j, style.getMinimum().getAroundClass());

        if (!hasImports && firstClass) {
            j = minimumLines(j, style.getMinimum().getAfterPackage());
        }

        if(!hasImports && firstClass && cu.getPackageDeclaration() == null) {
                j = j.withPrefix(j.getPrefix().withWhitespace(""));
        }

        return j;
    }

    @Override
    public J.Import visitImport(J.Import impoort, P p) {
        J.Import i = super.visitImport(impoort, p);
        J.CompilationUnit cu = getCursor().firstEnclosingOrThrow(J.CompilationUnit.class);
        if(i.equals(cu.getImports().get(0)) && cu.getPackageDeclaration() == null && cu.getPrefix().equals(Space.EMPTY)) {
            i = i.withPrefix(i.getPrefix().withWhitespace(""));
        }
        return i;
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, P p) {
        J.MethodDeclaration j = super.visitMethodDeclaration(method, p);
        if (j.getBody() != null) {
            if (j.getBody().getStatements().isEmpty()) {
                Space end = minimumLines(j.getBody().getEnd(),
                        style.getMinimum().getBeforeMethodBody());
                if (end.getIndent().isEmpty() && style.getMinimum().getBeforeMethodBody() > 0) {
                    end = end.withWhitespace(end.getWhitespace() + method.getPrefix().getIndent());
                }
                j = j.withBody(j.getBody().withEnd(end));
            } else {
                j = j.withBody(j.getBody().withStatements(ListUtils.mapFirst(j.getBody().getStatements(), s ->
                        minimumLines(s, style.getMinimum().getBeforeMethodBody()))));
            }
        }

        return j;
    }

    @Override
    public J.NewClass visitNewClass(J.NewClass newClass, P p) {
        J.NewClass j = super.visitNewClass(newClass, p);
        if (j.getBody() != null) {
            j = j.withBody(j.getBody().withStatements(ListUtils.mapFirst(j.getBody().getStatements(), s ->
                    minimumLines(s, style.getMinimum().getAfterAnonymousClassHeader()))));
        }
        return j;
    }

    @Override
    public Statement visitStatement(Statement statement, P p) {
        Statement j = super.visitStatement(statement, p);
        Iterator<Object> cursorPath = getCursor().getParentOrThrow().getPath(J.class::isInstance);
        Object parentTree = cursorPath.next();
        if (cursorPath.hasNext()) {
            Object grandparentTree = cursorPath.next();
            if (grandparentTree instanceof J.ClassDeclaration && parentTree instanceof J.Block) {
                J.Block block = (J.Block) parentTree;
                J.ClassDeclaration classDecl = (J.ClassDeclaration) grandparentTree;

                j = keepMaximumLines(j, style.getKeepMaximum().getInDeclarations());

                // don't adjust the first statement in a block
                if (!block.getStatements().isEmpty() && block.getStatements().iterator().next() != j) {
                    if (j instanceof J.VariableDeclarations) {
                        if (classDecl.getKind() == J.ClassDeclaration.Kind.Type.Interface) {
                            j = minimumLines(j, style.getMinimum().getAroundFieldInInterface());
                        } else {
                            j = minimumLines(j, style.getMinimum().getAroundField());
                        }
                    } else if (j instanceof J.MethodDeclaration) {
                        if (classDecl.getKind() == J.ClassDeclaration.Kind.Type.Interface) {
                            j = minimumLines(j, style.getMinimum().getAroundMethodInInterface());
                        } else {
                            j = minimumLines(j, style.getMinimum().getAroundMethod());
                        }
                    } else if (j instanceof J.Block) {
                        j = minimumLines(j, style.getMinimum().getAroundInitializer());
                    }
                }
            } else {
                return keepMaximumLines(j, style.getKeepMaximum().getInCode());
            }
        }
        return j;
    }

    @Override
    public J.Block visitBlock(J.Block block, P p) {
        J.Block j = super.visitBlock(block, p);
        j = j.withEnd(keepMaximumLines(j.getEnd(), style.getKeepMaximum().getBeforeEndOfBlock()));
        return j;
    }

    private <J2 extends J> J2 keepMaximumLines(J2 tree, int max) {
        return tree.withPrefix(keepMaximumLines(tree.getPrefix(), max));
    }

    private Space keepMaximumLines(Space prefix, int max) {
        return prefix.withWhitespace(keepMaximumLines(prefix.getWhitespace(), max));
    }

    private String keepMaximumLines(String whitespace, int max) {
        long blankLines = whitespace.chars().filter(c -> c == '\n').count() - 1;
        if (blankLines > max) {
            int startWhitespaceAtIndex = 0;
            for (int i = 0; i < blankLines - max + 1; i++, startWhitespaceAtIndex++) {
                startWhitespaceAtIndex = whitespace.indexOf('\n', startWhitespaceAtIndex);
            }
            startWhitespaceAtIndex--;
            return whitespace.substring(startWhitespaceAtIndex);
        }
        return whitespace;
    }

    private <J2 extends J> JRightPadded<J2> minimumLines(JRightPadded<J2> tree, int min) {
        return tree.withElement(minimumLines(tree.getElement(), min));
    }

    private <J2 extends J> J2 minimumLines(J2 tree, int min) {
        return tree.withPrefix(minimumLines(tree.getPrefix(), min));
    }

    private Space minimumLines(Space prefix, int min) {
        return prefix.withWhitespace(minimumLines(prefix.getWhitespace(), min));
    }

    private String minimumLines(String whitespace, int min) {
        if (min == 0) {
            return whitespace;
        }
        String minWhitespace = whitespace;
        for (int i = 0; i < min - whitespace.chars().filter(c -> c == '\n').count() + 1; i++) {
            //noinspection StringConcatenationInLoop
            minWhitespace = "\n" + minWhitespace;
        }
        return minWhitespace;
    }

    @Nullable
    @Override
    public J postVisit(J tree, P p) {
        if (stopAfter != null && stopAfter == tree) {
            getCursor().putMessageOnFirstEnclosing(J.CompilationUnit.class, "stop", true);
        }
        return super.postVisit(tree, p);
    }

    @Nullable
    @Override
    public J visit(@Nullable Tree tree, P p) {
        if (getCursor().getNearestMessage("stop") != null) {
            return (J) tree;
        }
        return super.visit(tree, p);
    }
}
