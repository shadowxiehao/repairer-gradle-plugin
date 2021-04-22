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
import org.openrewrite.java.tree.Space;

public class RemoveTrailingWhitespaceVisitor<P> extends JavaIsoVisitor<P> {
    @Override
    public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, P p) {
        String eof = cu.getEof().getWhitespace();
        eof = eof.chars().filter(c -> c == '\n' || c == '\r')
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        J.CompilationUnit c = super.visitCompilationUnit(cu, p);
        return c.withEof(c.getEof().withWhitespace(eof));
    }

    @Override
    public Space visitSpace(Space space, Space.Location loc, P p) {
        Space s = space;
        int lastNewline = s.getWhitespace().lastIndexOf('\n');
        if (lastNewline > 0) {
            StringBuilder ws = new StringBuilder();
            char[] charArray = s.getWhitespace().toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (i >= lastNewline) {
                    ws.append(c);
                } else if (c == '\r' || c == '\n') {
                    ws.append(c);
                }
            }
            s = s.withWhitespace(ws.toString());
        }
        return s;
    }
}
