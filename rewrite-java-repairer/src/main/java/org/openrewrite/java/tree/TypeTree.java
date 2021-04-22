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
package org.openrewrite.java.tree;

import org.openrewrite.marker.Markers;

import java.util.Scanner;

import static org.openrewrite.Tree.randomId;

/**
 * A tree identifying a type (e.g. a simple or fully qualified class name, a primitive,
 * array, or parameterized type).
 */
public interface TypeTree extends NameTree {
    static <T extends TypeTree & Expression> T build(String fullyQualifiedName) {
        Scanner scanner = new Scanner(fullyQualifiedName);
        scanner.useDelimiter("\\.");

        String fullName = "";
        Expression expr = null;
        String nextLeftPad = "";
        for (int i = 0; scanner.hasNext(); i++) {
            StringBuilder whitespaceBefore = new StringBuilder();
            StringBuilder partBuilder = null;
            StringBuilder whitespaceBeforeNext = new StringBuilder();

            for (char c : scanner.next().toCharArray()) {
                if (!Character.isWhitespace(c)) {
                    if (partBuilder == null) {
                        partBuilder = new StringBuilder();
                    }
                    partBuilder.append(c);
                } else {
                    if (partBuilder == null) {
                        whitespaceBefore.append(c);
                    } else {
                        whitespaceBeforeNext.append(c);
                    }
                }
            }

            assert partBuilder != null;
            String part = partBuilder.toString();

            if (i == 0) {
                fullName = part;
                expr = Identifier.build(randomId(), Space.format(whitespaceBefore.toString()), Markers.EMPTY, part, null);
            } else {
                fullName += "." + part;
                expr = new J.FieldAccess(
                        randomId(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        expr,
                        new JLeftPadded<>(
                                Space.format(nextLeftPad),
                                Identifier.build(
                                        randomId(),
                                        Space.format(whitespaceBefore.toString()),
                                        Markers.EMPTY,
                                        part.trim(),
                                        null
                                ),
                                Markers.EMPTY
                        ),
                        (Character.isUpperCase(part.charAt(0))) ?
                                JavaType.Class.build(fullName) :
                                null
                );
            }

            nextLeftPad = whitespaceBeforeNext.toString();
        }

        assert expr != null;

        //noinspection unchecked
        return (T) expr;
    }
}
