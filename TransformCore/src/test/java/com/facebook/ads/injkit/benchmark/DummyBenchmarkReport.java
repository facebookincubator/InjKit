// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.benchmark;

import java.util.ArrayList;
import java.util.List;

public class DummyBenchmarkReport {
  static List<String> called = new ArrayList<>();
  static List<String> owner = new ArrayList<>();
  static List<String> methodName = new ArrayList<>();
  static List<String> methodDesc = new ArrayList<>();
  static List<Long> timeNanos = new ArrayList<>();
  static List<Long> limitNanos = new ArrayList<>();
  static List<Throwable> exceptions = new ArrayList<>();

  public static void executed(
      String ownerClass, String methodName, String methodDesc, long timeNanos) {
    DummyBenchmarkReport.called.add("executed");
    DummyBenchmarkReport.owner.add(ownerClass);
    DummyBenchmarkReport.methodName.add(methodName);
    DummyBenchmarkReport.methodDesc.add(methodDesc);
    DummyBenchmarkReport.timeNanos.add(timeNanos);
  }

  public static void executedWithWarning(
      String ownerClass, String methodName, String methodDesc, long timeNanos, long limitNanos) {
    DummyBenchmarkReport.called.add("executedWithWarning");
    DummyBenchmarkReport.owner.add(ownerClass);
    DummyBenchmarkReport.methodName.add(methodName);
    DummyBenchmarkReport.methodDesc.add(methodDesc);
    DummyBenchmarkReport.timeNanos.add(timeNanos);
    DummyBenchmarkReport.limitNanos.add(limitNanos);
  }

  public static void failed(
      String ownerClass, String methodName, String methodDesc, long timeNanos, long limitNanos) {
    DummyBenchmarkReport.called.add("failed");
    DummyBenchmarkReport.owner.add(ownerClass);
    DummyBenchmarkReport.methodName.add(methodName);
    DummyBenchmarkReport.methodDesc.add(methodDesc);
    DummyBenchmarkReport.timeNanos.add(timeNanos);
    DummyBenchmarkReport.limitNanos.add(limitNanos);
  }

  public static void thrown(
      String ownerClass,
      String methodName,
      String methodDesc,
      Throwable exception,
      long timeNanos) {
    DummyBenchmarkReport.called.add("thrown");
    DummyBenchmarkReport.owner.add(ownerClass);
    DummyBenchmarkReport.methodName.add(methodName);
    DummyBenchmarkReport.methodDesc.add(methodDesc);
    DummyBenchmarkReport.timeNanos.add(timeNanos);
    exceptions.add(exception);
  }

  static void reset() {
    called.clear();
    owner.clear();
    methodName.clear();
    methodDesc.clear();
    timeNanos.clear();
    limitNanos.clear();
    exceptions.clear();
  }
}
