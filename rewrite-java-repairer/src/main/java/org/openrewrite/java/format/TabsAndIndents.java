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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.style.IntelliJ;
import org.openrewrite.java.style.TabsAndIndentsStyle;
import org.openrewrite.java.tree.J;

public class TabsAndIndents extends Recipe {
    @Override
    public String getDisplayName() {
        return "Tabs and indents";
    }

    @Override
    public String getDescription() {
        return "Format tabs and indents in Java code.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TabsAndIndentsFromCompilationUnitStyle();
    }

    private static class TabsAndIndentsFromCompilationUnitStyle extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
            TabsAndIndentsStyle style = cu.getStyle(TabsAndIndentsStyle.class);
            if (style == null) {
                style = IntelliJ.tabsAndIndents();
            }
            doAfterVisit(new TabsAndIndentsVisitor<>(style));
            return cu;
        }
    }
}
