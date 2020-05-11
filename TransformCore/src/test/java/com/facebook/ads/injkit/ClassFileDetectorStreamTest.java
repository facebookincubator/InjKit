// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ClassFileDetectorStreamTest {

  private static final byte[] VALID_CLASS =
      hexStringToByteArray(
          ""
              + "cafebabe00000034000d0a0003000a07"
              + "000b07000c0100063c696e69743e0100"
              + "03282956010004436f646501000f4c69"
              + "6e654e756d6265725461626c6501000a"
              + "536f7572636546696c65010006412e6a"
              + "6176610c00040005010001410100106a"
              + "6176612f6c616e672f4f626a65637400"
              + "21000200030000000000010001000400"
              + "05000100060000001d00010001000000"
              + "052ab70001b100000001000700000006"
              + "00010000000100010008000000020009");

  @Parameterized.Parameter(0)
  public int readArraySize;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {0}, {1}, {2}, {3}, {4}, {5}, {10}, {100}, {1000},
        });
  }

  private byte[] readAll(InputStream is) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    if (readArraySize == 0) {
      int r;
      while ((r = is.read()) != -1) {
        result.write(r);
      }

      return result.toByteArray();
    }

    byte[] buffer = new byte[readArraySize];
    int r;
    while ((r = is.read(buffer)) > 0) {
      result.write(buffer, 0, r);
    }

    return result.toByteArray();
  }

  @Test
  public void emptyFileIsDetectedAsNotClass() throws IOException {
    ByteArrayInputStream data = new ByteArrayInputStream(new byte[0]);

    ClassFileDetectorStream detector = new ClassFileDetectorStream("Foo.class", data);
    int readByte = detector.read();

    assertThat(detector.isClass()).isFalse();
    assertThat(readByte).isEqualTo(-1);
  }

  @Test
  public void nonClassFileIsDetectedAsNotClass() throws IOException {
    byte[] dataArray = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    ByteArrayInputStream data = new ByteArrayInputStream(dataArray);

    ClassFileDetectorStream detector = new ClassFileDetectorStream("Foo.class", data);
    byte[] read = readAll(detector);

    assertThat(detector.isClass()).isFalse();
    assertThat(read).isEqualTo(dataArray);
  }

  @Test
  public void classFileIsDetectedAsClass() throws IOException {
    ByteArrayInputStream data = new ByteArrayInputStream(VALID_CLASS);

    ClassFileDetectorStream detector = new ClassFileDetectorStream("Foo.class", data);
    byte[] read = readAll(detector);

    assertThat(detector.isClass()).isTrue();
    assertThat(read).isEqualTo(VALID_CLASS);
  }

  @Test
  public void classFileInNonClassFileIsNotDetectedAsClass() throws IOException {
    ByteArrayInputStream data = new ByteArrayInputStream(VALID_CLASS);

    ClassFileDetectorStream detector = new ClassFileDetectorStream("Foo.clazz", data);
    byte[] read = readAll(detector);

    assertThat(detector.isClass()).isFalse();
    assertThat(read).isEqualTo(VALID_CLASS);
  }

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }
}
