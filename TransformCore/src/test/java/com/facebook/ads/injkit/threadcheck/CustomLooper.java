/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.threadcheck;

public class CustomLooper {
  public static CustomLooper myself;
  public static CustomLooper main;

  public static CustomLooper myLooper() {
    return myself;
  }

  public static CustomLooper getMainLooper() {
    return main;
  }
}
