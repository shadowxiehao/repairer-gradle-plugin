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
package org.openrewrite.semver;

import org.openrewrite.Validated;
import org.openrewrite.internal.lang.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import static java.lang.Integer.parseInt;

public class LatestRelease implements VersionComparator {
    @Nullable
    private final String metadataPattern;

    public LatestRelease(@Nullable String metadataPattern) {
        this.metadataPattern = metadataPattern;
    }

    @Override
    public boolean isValid(String version) {
        Matcher matcher = VersionComparator.RELEASE_PATTERN.matcher(normalizeVersion(version));
        if (!matcher.matches() || PRE_RELEASE_ENDING.matcher(version).find()) {
            return false;
        }
        return metadataPattern == null ||
                (matcher.group(4) != null && matcher.group(4).matches(metadataPattern));
    }

    static String normalizeVersion(String version) {
        if (version.endsWith(".RELEASE")) {
            return version.substring(0, version.length() - ".RELEASE".length());
        } else if (version.endsWith(".FINAL") || version.endsWith(".Final")) {
            return version.substring(0, version.length() - ".FINAL".length());
        }

        AtomicBoolean beforeMetadata = new AtomicBoolean(true);
        long versionParts = version.chars()
                .filter(c -> {
                    if (c == '-' || c == '+') {
                        beforeMetadata.set(false);
                    }
                    return beforeMetadata.get();
                })
                .filter(c -> c == '.')
                .count();

        if (versionParts < 2) {
            String[] versionAndMetadata = version.split("(?=[-+])");
            for (; versionParts < 2; versionParts++) {
                versionAndMetadata[0] += ".0";
            }
            version = versionAndMetadata[0] + (versionAndMetadata.length > 1 ?
                    versionAndMetadata[1] : "");
        }

        return version;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public int compare(String v1, String v2) {
        Matcher v1Gav = VersionComparator.RELEASE_PATTERN.matcher(normalizeVersion(v1));
        Matcher v2Gav = VersionComparator.RELEASE_PATTERN.matcher(normalizeVersion(v2));

        v1Gav.matches();
        v2Gav.matches();

        for (int i = 1; i <= 3; i++) {
            String v1Part = v1Gav.group(i);
            String v2Part = v2Gav.group(i);
            if (v1Part == null) {
                return v2Part == null ? 0 : -11;
            } else if (v2Part == null) {
                return 1;
            }

            int diff = parseInt(v1Part) - parseInt(v2Part);
            if (diff != 0) {
                return diff;
            }
        }

        return v1.compareTo(v2);
    }

    public static Validated build(String toVersion, @Nullable String metadataPattern) {
        return toVersion.equals("latest.release") ?
                Validated.valid("latestRelease", new LatestRelease(metadataPattern)) :
                Validated.invalid("latestRelease", toVersion, "not a hyphen range");
    }
}
