/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ZipProcessingTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private void makeZipWithFooFile(File zip, byte[] fooData) throws Exception {
    try (FileOutputStream output = new FileOutputStream(zip);
        ZipOutputStream zipOutputStream = new ZipOutputStream(output)) {
      zipOutputStream.putNextEntry(new ZipEntry("foo"));
      zipOutputStream.write(fooData);
    }
  }

  private byte[] readZipFooFileContents(File zip) throws Exception {
    try (ZipFile resultZip = new ZipFile(zip)) {
      ZipEntry entry = resultZip.getEntry("foo");
      try (InputStream is = resultZip.getInputStream(entry)) {
        return ByteStreams.toByteArray(is);
      }
    }
  }

  private File makeConfig() throws Exception {
    return temporaryFolder.newFile();
  }

  @Test
  public void createsOutputZip() throws Exception {
    File inputZip = new File(temporaryFolder.getRoot(), "a.zip");
    File outputZip = new File(temporaryFolder.getRoot(), "b.zip");
    File config = makeConfig();

    byte[] fooData = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    makeZipWithFooFile(inputZip, fooData);

    Main.main(
        new String[] {
          "--input=" + inputZip.getCanonicalPath(),
          "--output=" + outputZip.getCanonicalPath(),
          "--config=" + config.getCanonicalPath(),
        });

    byte[] resultBytes = readZipFooFileContents(outputZip);

    assertThat(outputZip.isFile()).isTrue();
    assertThat(resultBytes).isEqualTo(fooData);
  }

  @Test
  public void overwritesOutputZip() throws Exception {
    File inputZip = new File(temporaryFolder.getRoot(), "a.zip");
    File outputZip = new File(temporaryFolder.getRoot(), "b.zip");
    File config = makeConfig();

    makeZipWithFooFile(outputZip, new byte[] {5, 5, 5, 5});

    byte[] fooData = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    makeZipWithFooFile(inputZip, fooData);

    Main.main(
        new String[] {
          "--input=" + inputZip.getCanonicalPath(),
          "--output=" + outputZip.getCanonicalPath(),
          "--config=" + config.getCanonicalPath(),
        });

    byte[] resultBytes = readZipFooFileContents(outputZip);

    assertThat(outputZip.isFile()).isTrue();
    assertThat(resultBytes).isEqualTo(fooData);
  }

  // Dummy handler just to have a real class for the tests
  public static class Handler {
    public static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  @Retention(RetentionPolicy.CLASS)
  @interface Dummy {}

  public static class DummyLogger {
    public static void logCall(String methodName, String description) {}
  }

  @Retention(RetentionPolicy.CLASS)
  @interface DummyLogAnnotation {
    String description();
  }

  @interface DummyBenchmark {
    int warnAtMillis();

    int failAtMillis();
  }
}
