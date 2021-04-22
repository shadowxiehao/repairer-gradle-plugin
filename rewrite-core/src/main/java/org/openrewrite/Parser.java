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
package org.openrewrite;

import org.openrewrite.internal.StringUtils;
import org.openrewrite.internal.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public interface Parser<S extends SourceFile> {
    default List<S> parse(Iterable<Path> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        return parseInputs(StreamSupport
                        .stream(sourceFiles.spliterator(), false)
                        .map(sourceFile -> new Input(sourceFile, () -> {
                                    try {
                                        return Files.newInputStream(sourceFile);
                                    } catch (IOException e) {
                                        throw new UncheckedIOException(e);
                                    }
                                })
                        )
                        .collect(toList()),
                relativeTo,
                ctx
        );
    }

    default List<S> parse(ExecutionContext ctx, String... sources) {
        return parseInputs(
                Arrays.stream(sources).map(source ->
                        new Input(
                                Paths.get(Long.toString(System.nanoTime())),
                                () -> new ByteArrayInputStream(source.getBytes()),
                                true
                        )
                ).collect(toList()),
                null,
                ctx
        );
    }

    /**
     * Used for testing primarily, when a multi-line source string is written inline.
     *
     * @param sources One or more sources.
     * @return Parsed list of {@link SourceFile}.
     */
    List<S> parse(String... sources);

    /**
     * @param sources    A collection of inputs. At the conclusion of parsing all sources' {@link Input#source}
     *                   are closed.
     * @param relativeTo A common relative path for all {@link Input#path}.
     * @param ctx        The execution context
     * @return A list of {@link SourceFile}.
     */
    List<S> parseInputs(Iterable<Input> sources, @Nullable Path relativeTo, ExecutionContext ctx);

    boolean accept(Path path);

    default boolean accept(Input input) {
        return input.isSynthetic() || accept(input.getPath());
    }

    default List<Input> acceptedInputs(Iterable<Input> input) {
        return StreamSupport.stream(input.spliterator(), false)
                .filter(this::accept)
                .collect(toList());
    }

    default Parser<S> reset() {
        return this;
    }

    /**
     * A source input. {@link Input#path} may be a synthetic path and not
     * represent a resolvable path on disk, as is the case when parsing sources
     * from BigQuery (we have a relative path from the original Github repository
     * and the sources, but don't have these sources on disk).
     * <p>
     * Nevertheless, this class is a generalization that applies well enough to
     * paths that are resolvable on disk, where the file has been pre-read into
     * memory.
     */
    class Input {
        private final boolean synthetic;
        private final Path path;
        private final Supplier<InputStream> source;

        public Input(Path path, Supplier<InputStream> source) {
            this(path, source, false);
        }

        public Input(Path path, Supplier<InputStream> source, boolean synthetic) {
            this.path = path;
            this.source = source;
            this.synthetic = synthetic;
        }

        @Incubating(since = "7.0.0")
        public static Input fromString(String source) {
            return new Input(
                    Paths.get(Long.toString(System.nanoTime())),
                    () -> new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)),
                    true
            );
        }

        @Incubating(since = "7.0.0")
        public static Input fromResource(String resource) {
            return new Input(
                    Paths.get(Long.toString(System.nanoTime())),
                    () -> Input.class.getResourceAsStream(resource),
                    true
            );
        }

        @Incubating(since = "7.0.0")
        public static List<Input> fromResource(String resource, String delimiter) {
            return Arrays.stream(StringUtils.readFully(Input.class.getResourceAsStream(resource)).split(delimiter))
                    .map(source -> new Parser.Input(
                            Paths.get(Long.toString(System.nanoTime())),
                            () -> new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)),
                            true
                    ))
                    .collect(toList());
        }

        public Path getPath() {
            return path;
        }

        public Path getRelativePath(@Nullable Path relativeTo) {
            return relativeTo == null ? path : relativeTo.relativize(path);
        }

        public InputStream getSource() {
            return source.get();
        }

        public boolean isSynthetic() {
            return synthetic;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Input input = (Input) o;
            return Objects.equals(path, input.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }
    }
}
