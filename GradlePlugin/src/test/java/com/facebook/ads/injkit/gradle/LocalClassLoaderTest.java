// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.gradle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LocalClassLoaderTest {

  private static final String RESOURCE_PATH =
      LocalClassLoaderTest.class
              .getName()
              .substring(0, LocalClassLoaderTest.class.getName().lastIndexOf('.'))
              .replace('.', '/')
          + "/testdatafile";
  private static final String RESOURCE_CONTENTS = "test contents";
  private static final String AUTONOMOUS_CLASS_NAME = AutonomousCodeInTestJar.class.getName();
  private static final String CLASS_WITH_DEPENDENCY_NAME = ClassWithDependency.class.getName();

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private ClassLoader rootClassLoader;
  private URLClassLoader testClassLoader;
  private URL[] allUrls;
  private URL[] urlsWithoutTestCode;
  private URL[] urlsWithoutTestResource;

  private static ClassLoader findRootClassLoader() {
    ClassLoader rootClassLoader;

    for (rootClassLoader = LocalClassLoader.class.getClassLoader();
        rootClassLoader != null;
        rootClassLoader = rootClassLoader.getParent()) {
      try {
        rootClassLoader.loadClass(LocalClassLoaderTest.class.getName());
        // Continue.
      } catch (ClassNotFoundException e) {
        // Root class loader should not have the local test class.
        return rootClassLoader;
      }
    }

    fail();
    return rootClassLoader;
  }

  private static String resourceOfClass(Class<?> cls) {
    return cls.getName().replace('.', '/') + ".class";
  }

  private URL makeJarWithCode(Class<?> cls) throws IOException {
    File tmp = temporaryFolder.newFile();
    try (ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(tmp))) {
      zipOutput.putNextEntry(new ZipEntry(resourceOfClass(cls)));
      try (InputStream clsRsrc =
          LocalClassLoaderTest.class.getClassLoader().getResourceAsStream(resourceOfClass(cls))) {
        ByteStreams.copy(clsRsrc, zipOutput);
      }
    }

    return tmp.toURI().toURL();
  }

  private ClassLoader makeStdLoaderForClass(Class<?> cls, ClassLoader parent) throws IOException {
    return new URLClassLoader(new URL[] {makeJarWithCode(cls)}, parent);
  }

  private ClassLoader makeLocalLoaderForClass(Class<?> cls, ClassLoader parent) throws IOException {
    return new LocalClassLoader(new URL[] {makeJarWithCode(cls)}, parent);
  }

  @Nullable
  private String fromStream(InputStream stream) throws IOException {
    if (stream == null) {
      return null;
    }

    byte[] data = ByteStreams.toByteArray(stream);
    return new String(data);
  }

  private static URL[] removeUrlThat(URL[] urls, Predicate<URL[]> test) {
    URL[] result = new URL[urls.length - 1];

    boolean removedUrl = false;
    for (int i = 0; i < urls.length; i++) {
      System.arraycopy(urls, 0, result, 0, i);
      System.arraycopy(urls, i + 1, result, i, urls.length - i - 1);
      if (test.test(result)) {
        return result;
      }
    }

    fail();
    return null;
  }

  @Before
  public void before() throws Exception {
    testClassLoader = (URLClassLoader) LocalClassLoaderTest.class.getClassLoader();
    rootClassLoader = findRootClassLoader();

    allUrls = testClassLoader.getURLs();

    // We assume all URLs have at least 2 elements.
    assertThat(allUrls.length).isGreaterThan(1);

    urlsWithoutTestCode =
        removeUrlThat(
            allUrls,
            urls -> {
              URLClassLoader tmpLoader = new URLClassLoader(urls, rootClassLoader);
              try {
                tmpLoader.loadClass(LocalClassLoaderTest.class.getName());
                return false;
              } catch (ClassNotFoundException e) {
                return true;
              }
            });

    urlsWithoutTestResource =
        removeUrlThat(
            allUrls,
            urls -> {
              URLClassLoader tmpLoader = new URLClassLoader(urls, rootClassLoader);
              InputStream is = tmpLoader.getResourceAsStream(RESOURCE_PATH);
              if (is == null) {
                return true;
              }

              try {
                is.close();
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }

              return false;
            });
  }

  @Test
  public void loadsClassFromUrlIfNotInParent() throws Exception {
    ClassLoader ldr = makeLocalLoaderForClass(AutonomousCodeInTestJar.class, rootClassLoader);

    Class<?> loaded = ldr.loadClass(AUTONOMOUS_CLASS_NAME);

    assertThat(loaded).isNotNull();
    assertThat(loaded).isNotSameAs(AutonomousCodeInTestJar.class);
  }

  @Test
  public void loadsClassFromUrlEvenIfInParent() throws Exception {
    ClassLoader ldr =
        makeLocalLoaderForClass(
            AutonomousCodeInTestJar.class,
            makeStdLoaderForClass(AutonomousCodeInTestJar.class, rootClassLoader));

    Class<?> loaded = ldr.loadClass(AUTONOMOUS_CLASS_NAME);

    assertThat(loaded).isNotNull();
    assertThat(loaded).isNotSameAs(AutonomousCodeInTestJar.class);
  }

  @Test
  public void loadsClassFromParentIfNotInUrl() throws Exception {
    ClassLoader ldr =
        makeLocalLoaderForClass(
            ClassNoOneLoads.class,
            makeStdLoaderForClass(AutonomousCodeInTestJar.class, rootClassLoader));

    Class<?> loaded = ldr.loadClass(AUTONOMOUS_CLASS_NAME);

    assertThat(loaded).isNotNull();
    assertThat(loaded).isNotSameAs(AutonomousCodeInTestJar.class);
  }

  @Test
  public void failsToLoadClassIfIsNeitherInParentNorInUrl() throws Exception {
    ClassLoader ldr =
        makeLocalLoaderForClass(
            ClassNoOneLoads.class, makeStdLoaderForClass(ClassNoOneLoads.class, rootClassLoader));

    try {
      ldr.loadClass(AUTONOMOUS_CLASS_NAME);
      fail();
    } catch (ClassNotFoundException e) {
      // Expected.
    }
  }

  @Test
  public void loadsClassWithDependencyOnParent() throws Exception {
    ClassLoader ldr =
        makeLocalLoaderForClass(
            ClassWithDependency.class,
            makeStdLoaderForClass(AutonomousCodeInTestJar.class, rootClassLoader));

    Class<?> cls = ldr.loadClass(CLASS_WITH_DEPENDENCY_NAME);
    cls.newInstance();
  }

  @Test
  public void loadsResourceFromUrlIfNotInParent() throws Exception {
    LocalClassLoader loader = new LocalClassLoader(allUrls, rootClassLoader);

    String resourceContents;
    try (InputStream stream = loader.getResourceAsStream(RESOURCE_PATH)) {
      resourceContents = fromStream(stream);
    }

    assertThat(resourceContents).isEqualTo(RESOURCE_CONTENTS);
  }

  @Test
  public void loadsResourceFromParentIfNotInUrl() throws Exception {
    LocalClassLoader loader = new LocalClassLoader(urlsWithoutTestCode, testClassLoader);

    String resourceContents;
    try (InputStream stream = loader.getResourceAsStream(RESOURCE_PATH)) {
      resourceContents = fromStream(stream);
    }

    assertThat(resourceContents).isEqualTo(RESOURCE_CONTENTS);
  }

  @Test
  public void failsToLoadResourceIfIsNeitherInParentNorInUrl() throws Exception {
    LocalClassLoader loader = new LocalClassLoader(urlsWithoutTestResource, rootClassLoader);

    String resourceContents;
    try (InputStream stream = loader.getResourceAsStream(RESOURCE_PATH)) {
      resourceContents = fromStream(stream);
    }

    assertThat(resourceContents).isNull();
  }

  public static class ClassNoOneLoads {}

  public static class AutonomousCodeInTestJar {}

  public static class ClassWithDependency {
    AutonomousCodeInTestJar variableToForceInitialization = new AutonomousCodeInTestJar();
  }
}
