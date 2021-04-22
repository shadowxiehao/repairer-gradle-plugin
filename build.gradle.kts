// plugins {
//     id("nebula.release") version "15.3.1"
// }

// configure<nebula.plugin.release.git.base.ReleasePluginExtension> {
//     defaultVersionStrategy = nebula.plugin.release.NetflixOssStrategies.SNAPSHOT(project)
// }

// allprojects {
//     group = "org.openrewrite"
//     description = "Eliminate Tech-Debt. At build time."
// }

// evaluationDependsOn("plugin")

import com.github.jk1.license.LicenseReportExtension
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    `java-library`
    `maven-publish`
    signing

    id("com.github.johnrengelman.shadow") version "6.1.0" apply false
    id("com.github.hierynomus.license") version "0.15.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.4.31" apply false
    id("org.gradle.test-retry") version "1.1.6" apply false
    id("com.github.jk1.dependency-license-report") version "1.16" apply false

}



allprojects {
    apply(plugin = "license")
    group = "org.openrewrite"
    description = "Eliminate tech-debt. Automatically."
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.jk1.dependency-license-report")

    if(!name.contains("benchmark")) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        apply(plugin = "org.gradle.test-retry")

    }

    repositories {
        mavenCentral()
    }

    tasks.withType(GenerateModuleMetadata::class.java) {
        enabled = false
    }

    dependencies {
        "compileOnly"("com.google.code.findbugs:jsr305:latest.release")

        "compileOnly"("org.projectlombok:lombok:latest.release")
        "annotationProcessor"("org.projectlombok:lombok:latest.release")

        "testImplementation"("org.junit.jupiter:junit-jupiter-api:latest.release")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:latest.release")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:latest.release")

        "testImplementation"("org.jetbrains.kotlin:kotlin-reflect")
        "testImplementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        "testImplementation"("org.jetbrains.kotlin:kotlin-stdlib-common")

        "testImplementation"("org.assertj:assertj-core:latest.release")

        "testRuntimeOnly"("ch.qos.logback:logback-classic:1.0.13")
    }

    // This eagerly realizes KotlinCompile tasks, which is undesirable from the perspective of minimizing
    // time spent during Gradle's configuration phase.
    // But if we don't proactively make sure the destination dir exists, sometimes JavaCompile can fail with:
    // '..rewrite-core\build\classes\java\main' specified for property 'compileKotlinOutputClasses' does not exist.
    tasks.withType(KotlinCompile::class.java) {
        kotlinOptions {
            jvmTarget = "1.8"
            useIR = true
        }
        destinationDir.mkdirs()
    }

    tasks.withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType(Javadoc::class.java) {
        options.encoding = "UTF-8"
    }

    tasks.named<JavaCompile>("compileJava") {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()

        options.isFork = true
        options.forkOptions.executable = "javac"
        options.compilerArgs.addAll(listOf("--release", "8"))
    }

    configure<LicenseExtension> {
        ext.set("year", Calendar.getInstance().get(Calendar.YEAR))
        skipExistingHeaders = true
        excludePatterns.addAll(listOf("**/*.tokens", "**/*.config", "**/*.interp", "**/*.txt"))
        header = project.rootProject.file("gradle/licenseHeader.txt")
        mapping(mapOf("kt" to "SLASHSTAR_STYLE", "java" to "SLASHSTAR_STYLE"))
        strictCheck = true
    }

    tasks.named<Test>("test") {
        useJUnitPlatform {
            excludeTags("debug")
        }
        jvmArgs = listOf("-XX:+UnlockDiagnosticVMOptions", "-XX:+ShowHiddenFrames")
    }

    configurations.all {
        resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    configure<LicenseReportExtension> {
        renderers = arrayOf(com.github.jk1.license.render.CsvReportRenderer())
    }


}

defaultTasks("build")
