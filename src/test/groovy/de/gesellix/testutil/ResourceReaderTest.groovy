package de.gesellix.testutil

import spock.lang.Specification

class ResourceReaderTest extends Specification {

  def "resolves to a filesystem path"() {
    given:
    def classpathResource = "/example-path/example-file.txt"
    def fileResource = new File("${System.getenv("ROOT_PROJECT_BUILD_DIRECTORY")}/resources/test/example-path", "example-file.txt")

    when:
    def file = ResourceReader.getClasspathResourceAsFile(classpathResource, ResourceReader)

    then:
    file == fileResource
  }
}
