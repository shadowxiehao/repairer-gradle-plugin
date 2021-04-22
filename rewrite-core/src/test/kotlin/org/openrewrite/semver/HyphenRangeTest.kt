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

class HyphenRangeTest {
    /**
     * 1.2.3 - 2.3.4 := >=1.2.3 <=2.3.4
     */
    @Test
    fun inclusiveSet() {
        val hyphenRange: HyphenRange = HyphenRange.build("1.2.3 - 2.3.4", null).getValue()

        assertThat(hyphenRange.isValid("1.2.2")).isFalse()
        assertThat(hyphenRange.isValid("1.2.3.RELEASE")).isTrue()
        assertThat(hyphenRange.isValid("1.2.3")).isTrue()
        assertThat(hyphenRange.isValid("2.3.4")).isTrue()
        assertThat(hyphenRange.isValid("2.3.5")).isFalse()
    }

    /**
     * 1.2 - 2 := >=1.2.0 <=2.0.0
     */
    @Test
    fun partialVersion() {
        val hyphenRange: HyphenRange = HyphenRange.build("1.2 - 2", null).getValue()

        assertThat(hyphenRange.isValid("1.1.9")).isFalse()
        assertThat(hyphenRange.isValid("1.2.0")).isTrue()
        assertThat(hyphenRange.isValid("2.0.0")).isTrue()
        assertThat(hyphenRange.isValid("2.0.1")).isFalse()
    }
}
