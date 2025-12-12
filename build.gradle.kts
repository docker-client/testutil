import java.text.SimpleDateFormat
import java.util.*

plugins {
  id("java-library")
  id("groovy")
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions") version "0.53.0"
  id("org.sonatype.gradle.plugins.scan") version "3.1.4"
  id("io.freefair.maven-central.validate-poms") version "9.1.0"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

repositories {
  mavenCentral()
}

dependencies {
  api("ch.qos.logback:logback-classic:${libs.versions.logbackVersionrange.get()}!!${libs.versions.logback.get()}")
  implementation("org.slf4j:slf4j-api:${libs.versions.slf4jVersionrange.get()}!!${libs.versions.slf4j.get()}")
  testImplementation("org.spockframework:spock-core:2.4-groovy-5.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val dependencyVersions = listOf(
  libs.junit4
)

val dependencyGroupVersions = mapOf<String, String>()

configurations.all {
  resolutionStrategy {
    failOnVersionConflict()
    force(dependencyVersions)
    eachDependency {
      val forcedVersion = dependencyGroupVersions[requested.group]
      if (forcedVersion != null) {
        useVersion(forcedVersion)
      }
    }
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

tasks {
  withType<JavaCompile> {
    options.encoding = "UTF-8"
  }
  withType<Test> {
    useJUnitPlatform()

    // for the de.gesellix.testutil.ResourceReaderTest
    environment("ROOT_PROJECT_BUILD_DIRECTORY", rootProject.layout.buildDirectory.get())
  }
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("javadoc")
  from(tasks.javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

artifacts {
  add("archives", sourcesJar.get())
  add("archives", javadocJar.get())
}

fun findProperty(s: String) = project.findProperty(s) as String?

val isSnapshot = project.version == "unspecified"
val artifactVersion = if (!isSnapshot) project.version as String else SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date())!!
val publicationName = "testutil"
publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/${property("github.package-registry.owner")}/${property("github.package-registry.repository")}")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username")
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password")
      }
    }
  }
  publications {
    register(publicationName, MavenPublication::class) {
      pom {
        name.set("testutil")
        description.set("Test utilities for the docker-utils libraries")
        url.set("https://github.com/docker-client/testutil")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("gesellix")
            name.set("Tobias Gesellchen")
            email.set("tobias@gesellix.de")
          }
        }
        scm {
          connection.set("scm:git:github.com/docker-client/testutil.git")
          developerConnection.set("scm:git:ssh://github.com/docker-client/testutil.git")
          url.set("https://github.com/docker-client/testutil")
        }
      }
      artifactId = "testutil"
      version = artifactVersion
      from(components["java"])
      artifact(sourcesJar.get())
      artifact(javadocJar.get())
    }
  }
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications[publicationName])
}

ossIndexAudit {
  username = System.getenv("SONATYPE_INDEX_USERNAME") ?: findProperty("sonatype.index.username")
  password = System.getenv("SONATYPE_INDEX_PASSWORD") ?: findProperty("sonatype.index.password")
}

nexusPublishing {
  repositories {
    if (!isSnapshot) {
      sonatype {
        // custom repository name - 'sonatype' is pre-configured
        // for Sonatype Nexus (OSSRH) which is used for The Central Repository
        stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: findProperty("sonatype.staging.profile.id")) //can reduce execution time by even 10 seconds
        username.set(System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype.username"))
        password.set(System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype.password"))
        nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
        snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
      }
    }
  }
}
