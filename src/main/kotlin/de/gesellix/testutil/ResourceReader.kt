package de.gesellix.testutil

import java.io.File

class ResourceReader {

    fun getClasspathResourceAsFile(classpathResource: String, baseClass: Class<*>): File {
        val resource = baseClass.getResource(classpathResource)
        return File(resource.toURI())
    }
}
