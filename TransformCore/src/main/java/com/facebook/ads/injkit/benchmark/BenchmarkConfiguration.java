/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.benchmark;

import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class BenchmarkConfiguration {
  private final boolean benchmarkEnabled;
  private final String benchmarkAnnotationClass;
  private final String benchmarkReceiverClass;

  private BenchmarkConfiguration(
      boolean benchmarkEnabled, String benchmarkAnnotationClass, String benchmarkReceiverClass) {
    this.benchmarkEnabled = benchmarkEnabled;
    this.benchmarkAnnotationClass = benchmarkAnnotationClass;
    this.benchmarkReceiverClass = benchmarkReceiverClass;
  }

  static BenchmarkConfiguration makeDisabled(String benchmarkAnnotationClass) {
    // NULLSAFE_FIXME[Parameter Not Nullable]
    return new BenchmarkConfiguration(false, benchmarkAnnotationClass, null);
  }

  static BenchmarkConfiguration makeEnabled(
      String benchmarkAnnotationClass, String benchmarkReceiverClass) {
    return new BenchmarkConfiguration(true, benchmarkAnnotationClass, benchmarkReceiverClass);
  }

  boolean isBenchmarkEnabled() {
    return benchmarkEnabled;
  }

  String getBenchmarkAnnotationClass() {
    return benchmarkAnnotationClass;
  }

  String getBenchmarkReceiverClass() {
    return benchmarkReceiverClass;
  }
}
