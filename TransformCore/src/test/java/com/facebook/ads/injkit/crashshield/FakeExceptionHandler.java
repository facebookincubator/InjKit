/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FakeExceptionHandler {
  private static final List<Throwable> sHandledThrowables = new ArrayList<>();
  private static final List<Object> sHandledObjects = new ArrayList<>();
  private static final Set<Object> sCrashingObjects = new HashSet<>();

  public static void handleThrowable(Throwable t, Object o) {
    sHandledThrowables.add(t);
    sHandledObjects.add(o);
    sCrashingObjects.add(o);
  }

  public static boolean isObjectCrashing(Object o) {
    return sCrashingObjects.contains(o);
  }

  public static void methodFinished(Object o) {}

  public static void reset() {
    sHandledThrowables.clear();
    sHandledObjects.clear();
    resetCrashingObjects();
  }

  public static List<Throwable> getHandledThrowables() {
    return sHandledThrowables;
  }

  public static List<Object> getHandledObjects() {
    return sHandledObjects;
  }

  public static void resetCrashingObjects() {
    sCrashingObjects.clear();
  }
}
