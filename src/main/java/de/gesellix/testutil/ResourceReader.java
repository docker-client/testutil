package de.gesellix.testutil;

import java.io.File;
import java.net.URISyntaxException;

public class ResourceReader {

  public static File getClasspathResourceAsFile(String classpathResource, Class<?> baseClass) {
    try {
      return new File(baseClass.getResource(classpathResource).toURI());
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
