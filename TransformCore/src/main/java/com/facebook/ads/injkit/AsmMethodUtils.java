/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import org.objectweb.asm.tree.MethodNode;

public class AsmMethodUtils {
  private static final String CONSTRUCTOR_METHOD_NAME = "<init>";
  private static final String STATIC_INITIALIZER_METHOD_NAME = "<clinit>";

  private AsmMethodUtils() {}

  public static boolean isConstructor(MethodNode method) {
    return method.name.equals(CONSTRUCTOR_METHOD_NAME);
  }

  public static boolean isDefaultConstructor(MethodNode method) {
    return isConstructor(method) && method.desc.equals("()V");
  }

  public static boolean isStaticInitializer(MethodNode method) {
    return method.name.equals(STATIC_INITIALIZER_METHOD_NAME);
  }

  public static boolean isConstructorName(String name) {
    return name.equals(CONSTRUCTOR_METHOD_NAME);
  }
}
