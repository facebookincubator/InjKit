/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class ClassFileDetectorStream extends InputStream {
  // All class files start with this magic sequence.
  // CAFEBABE, but in bigendian notation.
  static final byte[] CLASS_FILE_MAGIC =
      new byte[] {(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe};

  private final InputStream input;
  private final byte[] initial;
  private final byte[] singleByteRead;
  private int initialReadIdx;
  private boolean isClass;

  public ClassFileDetectorStream(String name, InputStream input) throws IOException {
    this.input = input;
    if (name.endsWith(".class")) {
      initial = readInitial(input);
    } else {
      initial = new byte[0];
    }

    isClass = Arrays.equals(initial, CLASS_FILE_MAGIC);
    initialReadIdx = 0;
    singleByteRead = new byte[1];
  }

  public boolean isClass() {
    return isClass;
  }

  @Override
  public int read(byte[] bytes) throws IOException {
    return read(bytes, 0, bytes.length);
  }

  @Override
  public int read(byte[] bytes, int offset, int length) throws IOException {
    int missingInitial = initial.length - initialReadIdx;
    if (missingInitial > 0) {
      int readBytes = Math.min(length, missingInitial);
      System.arraycopy(initial, initialReadIdx, bytes, offset, readBytes);
      initialReadIdx += readBytes;
      if (readBytes < length) {
        return readBytes + read(bytes, offset + readBytes, length - readBytes);
      } else {
        return readBytes;
      }
    }

    return input.read(bytes, offset, length);
  }

  @Override
  public int read() throws IOException {
    int r = read(singleByteRead);
    if (r < 1) {
      return -1;
    }

    return singleByteRead[0] & 0xff;
  }

  private static byte[] readInitial(InputStream input) throws IOException {
    byte[] bytes = new byte[CLASS_FILE_MAGIC.length];
    int idx = 0;
    do {
      int r = input.read(bytes, idx, bytes.length - idx);
      if (r == -1) {
        byte[] newBytes = new byte[idx];
        System.arraycopy(bytes, 0, newBytes, 0, idx);
        return newBytes;
      }

      idx += r;
    } while (idx < bytes.length);

    return bytes;
  }
}
