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

import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.Space;

/**
 * Ensures that whitespace is on the outermost AST element possible.
 */
public class NormalizeFormatVisitor<P> extends JavaIsoVisitor<P> {
    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, P p) {
        J.ClassDeclaration c = super.visitClassDeclaration(classDecl, p);

        if (!c.getLeadingAnnotations().isEmpty()) {
            c = concatenatePrefix(c, Space.firstPrefix(c.getLeadingAnnotations()));
            c = c.withLeadingAnnotations(Space.formatFirstPrefix(c.getLeadingAnnotations(), Space.EMPTY));
            return c;
        }

        if (!c.getModifiers().isEmpty()) {
            c = concatenatePrefix(c, Space.firstPrefix(c.getModifiers()));
            c = c.withModifiers(Space.formatFirstPrefix(c.getModifiers(), Space.EMPTY));
            return c;
        }

        if(!c.getAnnotations().getKind().getPrefix().isEmpty()) {
            c = concatenatePrefix(c, c.getAnnotations().getKind().getPrefix());
            c = c.getAnnotations().withKind(c.getAnnotations().getKind().withPrefix(Space.EMPTY));
            return c;
        }

        JContainer<J.TypeParameter> typeParameters = c.getPadding().getTypeParameters();
        if (typeParameters != null && !typeParameters.getElements().isEmpty()) {
            c = concatenatePrefix(c, typeParameters.getBefore());
            c = c.getPadding().withTypeParameters(typeParameters.withBefore(Space.EMPTY));
            return c;
        }

        return c.withName(c.getName().withPrefix(c.getName().getPrefix().withWhitespace(" ")));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, P p) {
        J.MethodDeclaration m = super.visitMethodDeclaration(method, p);

        if (!m.getLeadingAnnotations().isEmpty()) {
            m = concatenatePrefix(m, Space.firstPrefix(m.getLeadingAnnotations()));
            m = m.withLeadingAnnotations(Space.formatFirstPrefix(m.getLeadingAnnotations(), Space.EMPTY));
            return m;
        }

        if (!m.getModifiers().isEmpty()) {
            m = concatenatePrefix(m, Space.firstPrefix(m.getModifiers()));
            m = m.withModifiers(Space.formatFirstPrefix(m.getModifiers(), Space.EMPTY));
            return m;
        }

        if (m.getAnnotations().getTypeParameters() != null && !m.getAnnotations().getTypeParameters().getTypeParameters().isEmpty()) {
            m = concatenatePrefix(m, m.getAnnotations().getTypeParameters().getPrefix());
            m = m.getAnnotations().withTypeParameters(m.getAnnotations().getTypeParameters().withPrefix(Space.EMPTY));
            return m;
        }

        if (m.getReturnTypeExpression() != null && !m.getReturnTypeExpression().getPrefix().getWhitespace().isEmpty()) {
            m = concatenatePrefix(m, m.getReturnTypeExpression().getPrefix());
            m = m.withReturnTypeExpression(m.getReturnTypeExpression().withPrefix(Space.EMPTY));
            return m;
        }

        m = concatenatePrefix(m, m.getName().getPrefix());
        m = m.withName(m.getName().withPrefix(Space.EMPTY));
        return m;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, P p) {
        J.VariableDeclarations v = super.visitVariableDeclarations(multiVariable, p);

        if (!v.getLeadingAnnotations().isEmpty()) {
            v = concatenatePrefix(v, Space.firstPrefix(v.getLeadingAnnotations()));
            v = v.withLeadingAnnotations(Space.formatFirstPrefix(v.getLeadingAnnotations(), Space.EMPTY));
            return v;
        }

        if (!v.getModifiers().isEmpty()) {
            v = concatenatePrefix(v, Space.firstPrefix(v.getModifiers()));
            v = v.withModifiers(Space.formatFirstPrefix(v.getModifiers(), Space.EMPTY));
            return v;
        }

        if (v.getTypeExpression() != null) {
            v = concatenatePrefix(v, v.getTypeExpression().getPrefix());
            v = v.withTypeExpression(v.getTypeExpression().withPrefix(Space.EMPTY));
            return v;
        }

        return v;
    }

    private <J2 extends J> J2 concatenatePrefix(J2 j, Space prefix) {
        return j.withPrefix(j.getPrefix().withWhitespace(j.getPrefix().getWhitespace() + prefix.getWhitespace()));
    }
}
