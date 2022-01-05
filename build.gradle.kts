import java.text.SimpleDateFormat
import java.util.*

plugins {
  id("java-library")
  id("groovy")
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions") version "0.41.0"
  id("net.ossindex.audit") version "0.4.11"
  id("io.freefair.maven-central.validate-poms") version "6.3.0"
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

repositories {
  mavenCentral()
}

dependencies {
  api("ch.qos.logback:logback-classic:[1.2,2)!!1.2.9")
  implementation("org.slf4j:slf4j-api:[1.7,1.8)!!1.7.32")
  testImplementation("org.codehaus.groovy:groovy:(3,4]!!3.0.9")
  testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
}

val dependencyVersions = listOf<String>(
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
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  withType(Test::class.java) {
    useJUnitPlatform()

    // for the de.gesellix.testutil.ResourceReaderTest
    environment("ROOT_PROJECT_BUILD_DIRECTORY", rootProject.buildDir)
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

nexusPublishing {
  repositories {
    if (!isSnapshot) {
      sonatype {
        // custom repository name - 'sonatype' is pre-configured
        // for Sonatype Nexus (OSSRH) which is used for The Central Repository
        stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: findProperty("sonatype.staging.profile.id")) //can reduce execution time by even 10 seconds
        username.set(System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype.username"))
        password.set(System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype.password"))
      }
    }
  }
}
