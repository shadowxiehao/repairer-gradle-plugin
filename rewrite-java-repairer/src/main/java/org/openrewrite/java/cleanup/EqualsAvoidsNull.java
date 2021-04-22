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
package org.openrewrite.java.cleanup;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.style.IntelliJ;
import org.openrewrite.java.tree.J;

public class EqualsAvoidsNull extends Recipe {

    @Override
    public String getDisplayName() {
        return "Equals avoids null";
    }

    @Override
    public String getDescription() {
        return "Checks that any combination of String literals is on the left side of an `equals()` comparison. Also checks for String literals assigned to some field (such as `someString.equals(anotherString = \"text\"))`.";
    }

    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new EqualsAvoidsNullFromCompilationUnitStyle();
    }

    private static class EqualsAvoidsNullFromCompilationUnitStyle extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {
            EqualsAvoidsNullStyle style = cu.getStyle(EqualsAvoidsNullStyle.class);
            if (style == null) {
                style = IntelliJ.equalsAvoidsNull();
            }
            doAfterVisit(new EqualsAvoidsNullVisitor<>(style));
            return cu;
        }
    }
}
