// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

class ZipRecursionHandler {
  private ZipRecursionHandler() {}

  static void handle(File file, FileConsumer consumer)
      throws IOException, AnnotationProcessingException {
    if (isZip(file)) {
      consumer.consumeZip(zipEntryConsumer -> handleZip(file, zipEntryConsumer));
    } else if (file.isFile()) {
      try (FileInputStream input = new FileInputStream(file);
          ClassFileDetectorStream cfds = new ClassFileDetectorStream(file.getName(), input)) {
        consumer.consumeFile(cfds.isClass(), cfds);
      }
    }
  }

  @SuppressWarnings("unused")
  private static boolean isZip(File file) throws IOException {
    try (ZipFile unused = new ZipFile(file)) {
      return true;
    } catch (ZipException e) {
      return false;
    }
  }

  private static void handleZip(File file, ZipEntryConsumer zipEntryConsumer)
      throws IOException, AnnotationProcessingException {
    try (ZipFile inputZipFile = new ZipFile(file)) {

      // pathsInZip protects against zips having more than one file with the same path.
      // It is weird, but it can happen, and ZipOutputStream doesn't work.
      Set<String> pathsInZip = new HashSet<>();
      Enumeration<? extends ZipEntry> entries = inputZipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry nextEntry = entries.nextElement();

        if (pathsInZip.contains(nextEntry.getName())) {
          continue;
        }

        pathsInZip.add(nextEntry.getName());

        try (InputStream fileInput = inputZipFile.getInputStream(nextEntry);
            ClassFileDetectorStream cfds =
                new ClassFileDetectorStream(nextEntry.getName(), fileInput)) {
          try {
            zipEntryConsumer.consumeZipEntry(nextEntry.getName(), cfds.isClass(), cfds);
          } catch (IOException e) {
            throw new IOException(
                String.format(Locale.US, "Failed to process zip '%s'", file.getAbsolutePath()), e);
          } catch (AnnotationProcessingException e) {
            throw new AnnotationProcessingException(
                String.format(Locale.US, "Failed to process zip '%s'", file.getAbsolutePath()), e);
          }
        }
      }
    }
  }

  interface FileConsumer {
    void consumeFile(boolean isClass, InputStream input)
        throws IOException, AnnotationProcessingException;

    void consumeZip(ZipHandler handler) throws IOException, AnnotationProcessingException;
  }

  @FunctionalInterface
  interface ZipHandler {
    void handleZip(ZipEntryConsumer consumer) throws IOException, AnnotationProcessingException;
  }

  @FunctionalInterface
  interface ZipEntryConsumer {
    void consumeZipEntry(String path, boolean isClass, InputStream input)
        throws IOException, AnnotationProcessingException;
  }
}
