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
package org.openrewrite.java.style;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.openrewrite.java.cleanup.*;
import org.openrewrite.style.NamedStyles;
import org.openrewrite.style.Style;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.openrewrite.Tree.randomId;

public class IntelliJ extends NamedStyles {
    private static final IntelliJ INSTANCE = new IntelliJ();

    private IntelliJ() {
        super(randomId(),
                "intellij-idea", "IntelliJ IDEA", "IntelliJ IDEA defaults for all ",
                Collections.emptySet(),
                Arrays.asList(
                        importLayout(),
                        blankLines(),
                        tabsAndIndents(),
                        spaces(),
                        wrappingAndBraces(),
                        unnecessaryParentheses(),
                        emptyBlock(),
                        equalsAvoidsNull(),
                        explicitInitialization()
                )
        );
    }

    @JsonCreator
    public static IntelliJ defaults() {
        return INSTANCE;
    }

    @Override
    public Collection<Style> getStyles() {
        return super.getStyles();
    }

    public static ImportLayoutStyle importLayout() {
        return ImportLayoutStyle.builder()
                .importAllOthers()
                .blankLine()
                .importPackage("javax.*")
                .importPackage("java.*")
                .blankLine()
                .importStaticAllOthers()
                .build();
    }

    public static TabsAndIndentsStyle tabsAndIndents() {
        return new TabsAndIndentsStyle(false, 4, 4, 8, false);
    }

    public static BlankLinesStyle blankLines() {
        return new BlankLinesStyle(
                new BlankLinesStyle.KeepMaximum(2, 2, 2, 2),
                new BlankLinesStyle.Minimum(0, 1, 3, 1, 1, 0, 0,
                        0, 0, 0, 1, 1, 0, 1)
        );
    }

    public static SpacesStyle spaces() {
        return new SpacesStyle(
                new SpacesStyle.BeforeParentheses(false, false, true, true, true, true, true, true, true, false),
                new SpacesStyle.AroundOperators(true, true, true, true, true, true, true, true, false, true, false),
                new SpacesStyle.BeforeLeftBrace(true, true, true, true, true, true, true, true, true, true, true, true, false, false),
                new SpacesStyle.BeforeKeywords(true, true, true, true),
                new SpacesStyle.Within(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
                new SpacesStyle.TernaryOperator(true, true, true, true),
                new SpacesStyle.TypeArguments(true, false, false),
                new SpacesStyle.Other(false, true, false, true, true, true, false),
                new SpacesStyle.TypeParameters(false, true)
        );
    }

    public static WrappingAndBracesStyle wrappingAndBraces() {
        return new WrappingAndBracesStyle();
    }

    public static UnnecessaryParenthesesStyle unnecessaryParentheses() {
        return new UnnecessaryParenthesesStyle(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
    }

    public static EmptyBlockStyle emptyBlock() {
        return new EmptyBlockStyle(EmptyBlockStyle.BlockPolicy.Statement, true, true, true, true, true, true, true, true, true, true, true, true);
    }

    public static EqualsAvoidsNullStyle equalsAvoidsNull() {
        return new EqualsAvoidsNullStyle(false);
    }

    public static ExplicitInitializationStyle explicitInitialization() {
        return new ExplicitInitializationStyle(false);
    }
}
