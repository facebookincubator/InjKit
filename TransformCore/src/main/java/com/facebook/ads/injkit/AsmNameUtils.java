/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

public class AsmNameUtils {
  public static final String CL_INIT = "<clinit>";
  public static final String INIT = "<init>";

  private AsmNameUtils() {}

  public static String classJavaNameToInternalName(String name) {
    return name.replaceAll("\\.", "/");
  }

  public static String classJavaNameToDescriptor(String name) {
    return "L" + classJavaNameToInternalName(name) + ";";
  }

  public static String classInternalNameToJavaName(String iname) {
    return iname.replaceAll("/", ".");
  }

  static String classDescriptorToJavaName(String descriptor) {
    return classInternalNameToJavaName(descriptor.substring(1, descriptor.length() - 2));
  }

  public static String packageJavaNameFromClassJavaName(String name) {
    int dotIdx = name.lastIndexOf('.');
    if (dotIdx == -1) {
      return name;
    }

    return name.substring(0, dotIdx);
  }
}
