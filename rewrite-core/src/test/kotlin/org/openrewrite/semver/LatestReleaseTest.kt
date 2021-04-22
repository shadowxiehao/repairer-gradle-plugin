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

class LatestReleaseTest {
    private val latestRelease = LatestRelease(null)

    @Test
    fun onlyNumericPartsValid() {
        assertThat(latestRelease.isValid("1.1.1")).isTrue()
        assertThat(latestRelease.isValid("1.1")).isTrue()
        assertThat(latestRelease.isValid("1")).isTrue()
        assertThat(latestRelease.isValid("1.1.a")).isFalse()
    }

    @Test
    fun differentPatchVersions() {
        assertThat(latestRelease.compare("1.1.1", "1.1.2")).isLessThan(0)
    }

    @Test
    fun differentMinorVersions() {
        assertThat(latestRelease.compare("1.1.1", "1.2.1")).isLessThan(0)
        assertThat(latestRelease.compare("1.1", "1.2")).isLessThan(0)
    }

    @Test
    fun differentMajorVersions() {
        assertThat(latestRelease.compare("1.1.1", "2.1.1")).isLessThan(0)
        assertThat(latestRelease.compare("1.1", "2.1")).isLessThan(0)
        assertThat(latestRelease.compare("1", "2")).isLessThan(0)
    }

    @Test
    fun differentNumberOfParts() {
        assertThat(latestRelease.compare("1.1", "1.1.1")).isLessThan(0)
        assertThat(latestRelease.compare("1", "1.1")).isLessThan(0)
    }

    @Test
    fun guavaVariants() {
        assertThat(latestRelease.compare("25.0-jre", "29.0-jre")).isLessThan(0)
    }

    @Test
    fun matchMetadata() {
        assertThat(LatestRelease("-jre").isValid("29.0-jre")).isTrue()
        assertThat(LatestRelease("-jre").isValid("29.0")).isFalse()
        assertThat(LatestRelease("-jre").isValid("29.0-android")).isFalse()
    }

    @Test
    fun normalizeVersionStripReleaseSuffix() {
        assertThat(LatestRelease.normalizeVersion("1.5.1.RELEASE")).isEqualTo("1.5.1")
        assertThat(LatestRelease.normalizeVersion("1.5.1.FINAL")).isEqualTo("1.5.1")
        assertThat(LatestRelease.normalizeVersion("1.5.1.Final")).isEqualTo("1.5.1")
    }

    @Test
    fun normalizeVersionToHaveMajorMinorPatch() {
        assertThat(LatestRelease.normalizeVersion("29.0")).isEqualTo("29.0.0")
        assertThat(LatestRelease.normalizeVersion("29.0-jre")).isEqualTo("29.0.0-jre")
        assertThat(LatestRelease.normalizeVersion("29-jre")).isEqualTo("29.0.0-jre")
    }
}
