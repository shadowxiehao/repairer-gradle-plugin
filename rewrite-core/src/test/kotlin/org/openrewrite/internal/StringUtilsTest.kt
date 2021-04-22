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
package org.openrewrite.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openrewrite.internal.StringUtils.*

class StringUtilsTest {
    @Test
    fun detectIndentLevel() {
        assertThat(indentLevel("""
            |<
            |   <
            |   <
            |   <
            |<
        """.trimMargin())).isEqualTo(0)

        assertThat(indentLevel("""
            |<
            |   <
        """.trimMargin())).isEqualTo(3)

        assertThat(indentLevel("""
            |<
            |    <
            |      <
        """.trimMargin())).isEqualTo(4)

        // ignores the last line if it is all blank
        assertThat(indentLevel("""
            class {
                A field;
            }
        """)).isEqualTo(12)

        assertThat(indentLevel("""
            | <
            |  <
            |   <
            |    <
        """.trimMargin())).isEqualTo(1)

        assertThat(indentLevel("""
            | <
            |  <
            |    <
            |    <
            |      <
        """.trimMargin())).isEqualTo(1)

        assertThat(indentLevel("""
            |<
            |<
        """.trimMargin())).isEqualTo(0)

        // doesn't consider newlines that occur as the first character on the first line or terminating newlines
        assertThat(indentLevel("""
            |
            |  <
            |    <
            |    <
            |
        """.trimMargin())).isEqualTo(2)
    }

    @Test
    fun trimIndent() {
        val input = """
            class {
                A field;
            }
        """

        assertThat(trimIndent(input)).isEqualTo(input.trimIndent())
    }

    @Test
    fun trimIndentMinimalIndent() {
        val input = """
        class {
                A field;
            }
        """

        assertThat(trimIndent(input)).isEqualTo(input.trimIndent())
    }

    @Test
    fun trimIndentNoIndent() {
        val input = "class{\n   A field;\n}"

        assertThat(trimIndent(input)).isEqualTo(input.trimIndent())
    }

    @Test
    fun splitComments() {
        assertThat(splitCStyleComments("")).isEqualTo(listOf(""))
        assertThat(splitCStyleComments(" ")).isEqualTo(listOf(" "))
        assertThat(splitCStyleComments("///**/")).isEqualTo(listOf("///**/"))
        assertThat(splitCStyleComments("/*\n//aoeu\n*/")).isEqualTo(listOf("/*\n//aoeu\n*/"))
        val comments =
                """
                    
                // aoeu 
                /***/    // wasd
            """.trimIndent()
        val expected = listOf("    \n// aoeu ", "\n/***/", "    // wasd")
        val splitComments = splitCStyleComments(comments)
        assertThat(splitComments).isEqualTo(expected)
    }

    @Test
    fun desiredNewlines() {
        assertThat(ensureNewlineCountBeforeComment("", 1)).isEqualTo("\n")
        assertThat(ensureNewlineCountBeforeComment(" ", 1)).isEqualTo("\n ")
        assertThat(ensureNewlineCountBeforeComment("\n", 0)).isEqualTo("")
        assertThat(ensureNewlineCountBeforeComment("//", 2)).isEqualTo("\n\n//")
        assertThat(ensureNewlineCountBeforeComment("//\n", 2)).isEqualTo("\n\n//\n")
        assertThat(ensureNewlineCountBeforeComment("\n\n\n//", 2)).isEqualTo("\n\n//")
        assertThat(ensureNewlineCountBeforeComment("\n\n\n//\n", 2)).isEqualTo("\n\n//\n")
        assertThat(ensureNewlineCountBeforeComment("/**\n*/", 2)).isEqualTo("\n\n/**\n*/")
        assertThat(ensureNewlineCountBeforeComment("/**\n*/\n", 2)).isEqualTo("\n\n/**\n*/\n")
        assertThat(ensureNewlineCountBeforeComment("\n\n\n/**\n*/", 2)).isEqualTo("\n\n/**\n*/")
        assertThat(ensureNewlineCountBeforeComment("\n\n\n/**\n*/\n", 2)).isEqualTo("\n\n/**\n*/\n")
        assertThat(ensureNewlineCountBeforeComment("\n    //", 1)).isEqualTo("\n    //")
        assertThat(ensureNewlineCountBeforeComment("\n    \n    //", 1)).isEqualTo("\n    //")
        assertThat(ensureNewlineCountBeforeComment("\n\n\n    /***/", 2)).isEqualTo("\n\n    /***/")
    }

    @Test
    fun replaceFirst() {
        var result = replaceFirst("#{} Fred #{}", "#{}", "I am")
        assertThat(result).isEqualTo("I am Fred #{}")
        result = replaceFirst(result, "#{}", "surely.")
        assertThat(result).isEqualTo("I am Fred surely.")
        result = replaceFirst("#{}#{}#{}", "#{}", "yo")
        assertThat(result).isEqualTo("yo#{}#{}")
        result = replaceFirst(result, "#{}", "yo")
        assertThat(result).isEqualTo("yoyo#{}")
        result = replaceFirst(result, "#{}", "yo")
        assertThat(result).isEqualTo("yoyoyo")
        result = replaceFirst("Nothing to see here", "#{}", "nonsense")
        assertThat(result).isEqualTo("Nothing to see here")
        result = replaceFirst("Nothing to see here", "", "nonsense")
        assertThat(result).isEqualTo("Nothing to see here")
        result = replaceFirst("", "", "nonsense")
        assertThat(result).isEqualTo("")
    }

    @Test
    fun occurrenceCount() {
        assertThat(countOccurrences("yoyoyoyoyo", "yo")).isEqualTo(5)
        assertThat(countOccurrences("yoyoyoyoyo", "yoyo")).isEqualTo(2)
        assertThat(countOccurrences("nonononono", "yo")).isEqualTo(0)
        assertThat(countOccurrences("", "")).isEqualTo(0)
    }
}
