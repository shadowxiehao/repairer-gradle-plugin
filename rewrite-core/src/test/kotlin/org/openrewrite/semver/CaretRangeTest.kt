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
package org.openrewrite.semver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CaretRangeTest {
    /**
     * ^1.2.3 := >=1.2.3 <2.0.0
     */
    @Test
    fun updateMinorAndPatch() {
        val caretRange: CaretRange = CaretRange.build("^1.2.3", null).getValue()!!

        assertThat(caretRange.isValid("1.2.3")).isTrue()
        assertThat(caretRange.isValid("1.2.3.RELEASE")).isTrue()
        assertThat(caretRange.isValid("1.2.4")).isTrue()
        assertThat(caretRange.isValid("1.9.0")).isTrue()
        assertThat(caretRange.isValid("2.0.0")).isFalse()
    }

    /**
     * ^0.2.3 := >=0.2.3 <0.3.0
     */
    @Test
    fun updatePatch() {
        val caretRange: CaretRange = CaretRange.build("^0.2.3", null).getValue()!!

        assertThat(caretRange.isValid("0.2.3")).isTrue()
        assertThat(caretRange.isValid("0.2.4")).isTrue()
        assertThat(caretRange.isValid("0.3.0")).isFalse()
    }

    @Test
    fun updateNothing() {
        val caretRange: CaretRange = CaretRange
                .build("^0.0.3", null)
                .getValue()!!

        assertThat(caretRange.isValid("0.0.3")).isFalse()
        assertThat(caretRange.isValid("0.0.4")).isFalse()
    }

    /**
     * ^1.x := >=1.0.0 <2.0.0
     */
    @Test
    fun desugarMinorWildcard() {
        val caretRange: CaretRange = CaretRange.build("^1.x", null).getValue()!!

        assertThat(caretRange.isValid("1.0.0")).isTrue()
        assertThat(caretRange.isValid("1.0.1")).isTrue()
        assertThat(caretRange.isValid("1.1.0")).isTrue()
        assertThat(caretRange.isValid("2.0.0")).isFalse()
    }

    /**
     * ^0.0.x := >=0.0.0 <0.1.0
     */
    @Test
    fun desugarPatchWildcard() {
        val caretRange: CaretRange = CaretRange.build("^0.0.x", null).getValue()!!

        assertThat(caretRange.isValid("0.0.0")).isTrue()
        assertThat(caretRange.isValid("0.0.1")).isTrue()
        assertThat(caretRange.isValid("0.1.0")).isFalse()
    }
}
