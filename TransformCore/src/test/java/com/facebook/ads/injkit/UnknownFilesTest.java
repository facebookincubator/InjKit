// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class UnknownFilesTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final Random random = new Random();

  private byte[] randomData() {
    byte[] randomData = new byte[1000];
    random.nextBytes(randomData);
    return randomData;
  }

  private void writeRandom(File file) throws IOException {
    Files.write(file.toPath(), randomData());
  }

  private static byte[] readFile(File file) throws IOException {
    return Files.readAllBytes(file.toPath());
  }

  private static void mkdir(File file) throws IOException {
    if (!file.mkdir()) {
      throw new IOException("Failed to create directory: " + file.getAbsolutePath());
    }
  }

  private File makeConfigurationFile() throws IOException {
    File file = temporaryFolder.newFile();
    return file;
  }

  private static byte[] readFileFromZip(File zip, String name) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try (ZipFile zipFile = new ZipFile(zip)) {
      InputStream input = zipFile.getInputStream(zipFile.getEntry(name));
      byte[] buffer = new byte[4096];
      int r;
      while ((r = input.read(buffer)) > 0) {
        output.write(buffer, 0, r);
      }
    }

    return output.toByteArray();
  }

  @Test
  public void unknownFileIsCopiedIfSpecifiedDirectlyAndNotExists() throws Exception {
    File file = temporaryFolder.newFile();
    File outDir = temporaryFolder.newFolder();
    File outFile = new File(outDir, file.getName());
    writeRandom(file);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(file, outFile)
        .setConfigurationFile(makeConfigurationFile())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(outFile.isFile()).isTrue();
    assertThat(readFile(outFile)).isEqualTo(readFile(file));
  }

  @Test
  public void unknownFileIsCopiedIfSpecifiedDirectlyAndExists() throws Exception {
    File file = temporaryFolder.newFile();
    File outDir = temporaryFolder.newFolder();
    File outFile = new File(outDir, file.getName());
    writeRandom(file);
    writeRandom(outFile);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(file, outFile)
        .setConfigurationFile(makeConfigurationFile())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(outFile.isFile()).isTrue();
    assertThat(readFile(outFile)).isEqualTo(readFile(file));
  }

  @Test
  public void unknownFileIsCopiedIfInDirectoryAndNotExists() throws Exception {
    File dir = temporaryFolder.newFolder();
    File outDir = temporaryFolder.newFolder();
    File inFile = new File(dir, "foo");
    File outFile = new File(outDir, "foo");
    writeRandom(inFile);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(dir, outDir)
        .setConfigurationFile(makeConfigurationFile())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(outFile.isFile()).isTrue();
    assertThat(readFile(outFile)).isEqualTo(readFile(inFile));
  }

  @Test
  public void unknownFileIsCopiedIfInDirectoryAndExists() throws Exception {
    File dir = temporaryFolder.newFolder();
    File outDir = temporaryFolder.newFolder();
    File inFile = new File(dir, "foo");
    File outFile = new File(outDir, "foo");
    writeRandom(inFile);
    writeRandom(outFile);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(dir, outDir)
        .setConfigurationFile(makeConfigurationFile())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(outFile.isFile()).isTrue();
    assertThat(readFile(outFile)).isEqualTo(readFile(inFile));
  }

  @Test
  public void unknownFileIsCopiedIfInSubDirectory() throws Exception {
    File dir = temporaryFolder.newFolder();
    File subDir = new File(dir, "dir");
    mkdir(subDir);
    File outDir = temporaryFolder.newFolder();
    File inFile = new File(subDir, "foo");
    File outFile = new File(new File(outDir, "dir"), "foo");
    writeRandom(inFile);

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(dir, outDir)
        .setConfigurationFile(makeConfigurationFile())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(outFile.isFile()).isTrue();
    assertThat(readFile(outFile)).isEqualTo(readFile(inFile));
  }

  @Test
  public void unknownFileIsCopiedIfInZipAndDestinationZipDoesNotExist() throws Exception {
    File zip = temporaryFolder.newFile();
    byte[] fileData = randomData();
    try (FileOutputStream out = new FileOutputStream(zip);
        ZipOutputStream zipOut = new ZipOutputStream(out)) {
      zipOut.putNextEntry(new ZipEntry("foo"));
      zipOut.write(fileData);
    }

    File outFile = new File(temporaryFolder.newFolder(), "out");

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(zip, outFile)
        .setConfigurationFile(makeConfigurationFile())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(outFile.isFile()).isTrue();
    assertThat(readFileFromZip(outFile, "foo")).isEqualTo(fileData);
  }

  @Test
  public void unknownFileIsCopiedIfInZipAndDestinationZipExists() throws Exception {
    File zip = temporaryFolder.newFile();
    byte[] fileData = randomData();
    try (FileOutputStream out = new FileOutputStream(zip);
        ZipOutputStream zipOut = new ZipOutputStream(out)) {
      zipOut.putNextEntry(new ZipEntry("foo"));
      zipOut.write(fileData);
    }

    File outFile = new File(temporaryFolder.newFolder(), "out");
    try (FileOutputStream out = new FileOutputStream(outFile);
        ZipOutputStream zipOut = new ZipOutputStream(out)) {
      zipOut.putNextEntry(new ZipEntry("foo"));
      zipOut.write(randomData());
    }

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(zip, outFile)
        .setConfigurationFile(makeConfigurationFile())
        .processSystemPath(false)
        .build()
        .process();

    assertThat(outFile.isFile()).isTrue();
    assertThat(readFileFromZip(outFile, "foo")).isEqualTo(fileData);
  }
}
