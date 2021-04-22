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
package org.openrewrite.java;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Option;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.internal.FormatFirstClassPrefix;
import org.openrewrite.java.style.ImportLayoutStyle;
import org.openrewrite.java.style.IntelliJ;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;

import java.util.List;
import java.util.Optional;

/**
 * This recipe will group and order the imports for a compilation unit using the rules defined by an {@link ImportLayoutStyle}.
 * If a style has not been defined, this recipe will use the default import layout style that is modelled after
 * IntelliJ's default import settings.
 * <p>
 * The @{link {@link OrderImports#removeUnused}} flag (which is defaulted to true) can be used to also remove any
 * imports that are not referenced within the compilation unit.
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@NoArgsConstructor
public class OrderImports extends Recipe {

    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    @With
    @Option(displayName = "Remove unused", description = "Remove unnecessary imports.", required = false)
    private boolean removeUnused = true;

    @Override
    public String getDisplayName() {
        return "Order imports";
    }

    @Override
    public String getDescription() {
        return "Group and order imports.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new OrderImportsVisitor<>(removeUnused);
    }

    /**
     * This visitor is used directly by AddImport, which is why this class is both static and package private
     */
    static class OrderImportsVisitor<P> extends JavaIsoVisitor<P> {
        private final boolean removeUnused;

        OrderImportsVisitor(boolean removeUnused) {
            this.removeUnused = removeUnused;
        }

        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, P ctx) {
            ImportLayoutStyle layoutStyle = Optional.ofNullable(cu.getStyle(ImportLayoutStyle.class))
                    .orElse(IntelliJ.importLayout());

            List<JRightPadded<J.Import>> orderedImports = layoutStyle.orderImports(cu.getPadding().getImports());

            boolean changed = false;
            if (orderedImports.size() != cu.getImports().size()) {
                cu = cu.getPadding().withImports(orderedImports);
                changed = true;
            } else {
                for (int i = 0; i < orderedImports.size(); i++) {
                    if (orderedImports.get(i) != cu.getPadding().getImports().get(i)) {
                        cu = cu.getPadding().withImports(orderedImports);
                        changed = true;
                        break;
                    }
                }
            }

            if (removeUnused) {
                doAfterVisit(new RemoveUnusedImports());
            } else if (changed) {
                doAfterVisit(new FormatFirstClassPrefix<>());
            }

            return cu;
        }
    }
}
