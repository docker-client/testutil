import com.jfrog.bintray.gradle.BintrayExtension
import java.text.SimpleDateFormat
import java.util.*

rootProject.extra.set("artifactVersion", SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date()))
rootProject.extra.set("bintrayDryRun", false)

buildscript {
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
    }
}

plugins {
    id("java-library")
    id("groovy")
    id("maven-publish")
    id("com.github.ben-manes.versions") version "0.28.0"
    id("com.jfrog.bintray") version "1.8.4"
    id("net.ossindex.audit") version "0.4.11"
    id("io.freefair.github.package-registry-maven-publish") version "4.1.6"
}

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
}

dependencies {
    api("org.slf4j:slf4j-api:1.7.29")
    api("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("org.codehaus.groovy:groovy:2.5.8")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}

val dependencyVersions = listOf(
        "org.codehaus.groovy:groovy:2.5.8",
        "org.slf4j:slf4j-api:1.7.29"
)

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()
        force(dependencyVersions)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType(Test::class.java) {
        useJUnit()

        // for the de.gesellix.testutil.ResourceReaderTest
        environment("ROOT_PROJECT_BUILD_DIRECTORY", rootProject.buildDir)
    }

    bintrayUpload {
        dependsOn("build")
    }

    wrapper {
        gradleVersion = "6.2.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    add("archives", sourcesJar.get())
}

val publicationName = "testutil"
publishing {
    publications {
        register(publicationName, MavenPublication::class) {
            groupId = "de.gesellix"
            artifactId = "testutil"
            version = rootProject.extra["artifactVersion"] as String
            from(components["java"])
//            artifact(sourcesJar.get())
        }
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?

rootProject.github {
    slug.set("${project.property("github.package-registry.owner")}/${project.property("github.package-registry.repository")}")
    username.set(System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username"))
    token.set(System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password"))
}

bintray {
    user = System.getenv()["BINTRAY_USER"] ?: findProperty("bintray.user")
    key = System.getenv()["BINTRAY_API_KEY"] ?: findProperty("bintray.key")
    setPublications(publicationName)
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "docker-utils"
        name = "testutil"
        desc = "Test utilities for the docker-utils libraries"
        setLicenses("MIT")
        setLabels("docker", "util", "test", "java")
        version.name = rootProject.extra["artifactVersion"] as String
        vcsUrl = "https://github.com/docker-client/testutil.git"
    })
    dryRun = rootProject.extra["bintrayDryRun"] as Boolean
}
