/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.sdkdebugger;

import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class SdkDebuggerConfiguration {
  private final boolean enabled;
  private final String logCallAnnotationClass;
  private final String callLoggerClass;

  private SdkDebuggerConfiguration(
      boolean enabled, String logCallAnnotationClass, String callLoggerClass) {
    this.enabled = enabled;
    this.logCallAnnotationClass = logCallAnnotationClass;
    this.callLoggerClass = callLoggerClass;
  }

  static SdkDebuggerConfiguration makeDisabled() {
    // NULLSAFE_FIXME[Parameter Not Nullable]
    return new SdkDebuggerConfiguration(false, null, null);
  }

  static SdkDebuggerConfiguration makeEnabled(
      String logCallAnnotationClass, String callLoggerClass) {
    return new SdkDebuggerConfiguration(true, logCallAnnotationClass, callLoggerClass);
  }

  boolean isEnabled() {
    return enabled;
  }

  String getCallLoggerClass() {
    return callLoggerClass;
  }

  String getLogCallAnnotationClass() {
    return logCallAnnotationClass;
  }
}
