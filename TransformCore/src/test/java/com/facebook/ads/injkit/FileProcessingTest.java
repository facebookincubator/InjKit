// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FileProcessingTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private final Random random = new Random();
  private final TestClassFileProcessor testClassFileProcessor = new TestClassFileProcessor();

  private File writeConfigurationFile() throws Exception {
    File configurationFile = temporaryFolder.newFile();
    TransformationEnvironment.newConfigurationWriter().write(configurationFile);
    return configurationFile;
  }

  private byte[] generateRandomData() {
    int size = 10 + random.nextInt(100);
    byte[] r = new byte[size];
    random.nextBytes(r);
    return r;
  }

  private byte[] generateRandomClassData() {
    byte[] randomData = generateRandomData();
    System.arraycopy(
        ClassFileDetectorStream.CLASS_FILE_MAGIC,
        0,
        randomData,
        0,
        ClassFileDetectorStream.CLASS_FILE_MAGIC.length);
    return randomData;
  }

  private static File writeDummy(File f, byte[] dummyData) throws IOException {
    Files.write(f.toPath(), dummyData);
    return f;
  }

  private File writeZipWithFile(String path, byte[] dummyData) throws IOException {
    File zipFile = temporaryFolder.newFile("foo.zip");
    try (FileOutputStream zipFileOutput = new FileOutputStream(zipFile);
        ZipOutputStream zip = new ZipOutputStream(zipFileOutput)) {
      zip.putNextEntry(new ZipEntry(path));
      zip.write(dummyData);
    }

    return zipFile;
  }

  private static int countZipFiles(File zip) throws IOException {
    try (ZipFile zf = new ZipFile(zip)) {
      return zf.size();
    }
  }

  private static byte[] extractZipEntry(File zip, String path) throws IOException {
    try (ZipFile zf = new ZipFile(zip)) {
      ZipEntry entry = zf.getEntry(path);
      if (entry == null) {
        return null;
      }

      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ByteStreams.copy(zf.getInputStream(entry), bytes);
      return bytes.toByteArray();
    }
  }

  @Test
  public void processesIsolatedClassFiles() throws Exception {
    byte[] randomClassData = generateRandomClassData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeDummy(temporaryFolder.newFile("dummy.class"), randomClassData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).hasSize(1);
    assertThat(testClassFileProcessor.data.get(0)).isEqualTo(randomClassData);
    assertThat(Files.readAllBytes(out.toPath())).isEqualTo(randomClassOutput);
  }

  @Test
  public void processesIsolatedNonClassFiles() throws Exception {
    byte[] randomData = generateRandomData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeDummy(temporaryFolder.newFile("dummy.data"), randomData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).isEmpty();
    assertThat(Files.readAllBytes(out.toPath())).isEqualTo(randomData);
  }

  @Test
  public void processesIsolatedInvalidClassFiles() throws Exception {
    byte[] randomData = generateRandomData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeDummy(temporaryFolder.newFile("dummy.class"), randomData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).isEmpty();
    assertThat(Files.readAllBytes(out.toPath())).isEqualTo(randomData);
  }

  @Test
  public void processesClassesInZips() throws Exception {
    byte[] randomClassData = generateRandomClassData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeZipWithFile("/foo/dummy.class", randomClassData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).hasSize(1);
    assertThat(countZipFiles(out)).isEqualTo(1);
    assertThat(extractZipEntry(out, "/foo/dummy.class")).isEqualTo(randomClassOutput);
  }

  @Test
  public void processesNonClassFilesInZips() throws Exception {
    byte[] randomData = generateRandomData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeZipWithFile("/foo/dummy.data", randomData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).isEmpty();
    assertThat(countZipFiles(out)).isEqualTo(1);
    assertThat(extractZipEntry(out, "/foo/dummy.data")).isEqualTo(randomData);
  }

  @Test
  public void processesInvalidClassFilesInZips() throws Exception {
    byte[] randomData = generateRandomData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeZipWithFile("/foo/dummy.class", randomData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).isEmpty();
    assertThat(countZipFiles(out)).isEqualTo(1);
    assertThat(extractZipEntry(out, "/foo/dummy.class")).isEqualTo(randomData);
  }

  @Test
  public void processesIsolatedClassFilesInDirectories() throws Exception {
    byte[] randomClassData = generateRandomClassData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeDummy(new File(temporaryFolder.newFolder(), "dummy.class"), randomClassData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).hasSize(1);
    assertThat(testClassFileProcessor.data.get(0)).isEqualTo(randomClassData);
    assertThat(Files.readAllBytes(out.toPath())).isEqualTo(randomClassOutput);
  }

  @Test
  public void processesIsolatedNonClassFilesInDirectories() throws Exception {
    byte[] randomData = generateRandomData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeDummy(new File(temporaryFolder.newFolder(), "dummy.data"), randomData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).isEmpty();
    assertThat(Files.readAllBytes(out.toPath())).isEqualTo(randomData);
  }

  @Test
  public void processesIsolatedInvalidClassFilesInDirectories() throws Exception {
    byte[] randomData = generateRandomData();
    byte[] randomClassOutput = generateRandomClassData();
    File in = writeDummy(new File(temporaryFolder.newFolder(), "dummy.class"), randomData);
    File out = temporaryFolder.newFile();
    testClassFileProcessor.outs.add(randomClassOutput);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(writeConfigurationFile())
        .setClassFileProcessorFactory(testClassFileProcessor.factoryOfMyself())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(out.isFile()).isTrue();
    assertThat(testClassFileProcessor.data).isEmpty();
    assertThat(Files.readAllBytes(out.toPath())).isEqualTo(randomData);
  }
}
