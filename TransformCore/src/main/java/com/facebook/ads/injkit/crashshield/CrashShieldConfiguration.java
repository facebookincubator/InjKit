/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield;

public class CrashShieldConfiguration {
  private final boolean enabled;
  private final String disableAnnotationClass;
  private final String enableAnnotationClass;
  private final String exceptionHandlerClass;
  private boolean shouldProcessConstructors;
  private boolean shouldProcessViews;

  private CrashShieldConfiguration(
      boolean enabled,
      String disableAnnotationClass,
      String enableAnnotationClass,
      String exceptionHandlerClass,
      boolean shouldProcessConstructors,
      boolean shouldProcessViews) {
    this.enabled = enabled;
    this.disableAnnotationClass = disableAnnotationClass;
    this.enableAnnotationClass = enableAnnotationClass;
    this.exceptionHandlerClass = exceptionHandlerClass;
    this.shouldProcessConstructors = shouldProcessConstructors;
    this.shouldProcessViews = shouldProcessViews;
  }

  static CrashShieldConfiguration makeDisabled() {
    return new CrashShieldConfiguration(false, null, null, null, false, false);
  }

  static CrashShieldConfiguration makeEnabled(
      String disableAnnotationClass,
      String enableAnnotationClass,
      String exceptionHandlerClass,
      boolean shouldProcessConstructors,
      boolean shouldProcessViews) {
    return new CrashShieldConfiguration(
        true,
        disableAnnotationClass,
        enableAnnotationClass,
        exceptionHandlerClass,
        shouldProcessConstructors,
        shouldProcessViews);
  }

  boolean isEnabled() {
    return enabled;
  }

  String getDisableAnnotationClass() {
    return disableAnnotationClass;
  }

  String getEnableAnnotationClass() {
    return enableAnnotationClass;
  }

  String getExceptionHandlerClass() {
    return exceptionHandlerClass;
  }

  boolean isShouldProcessConstructors() {
    return this.shouldProcessConstructors;
  }

  boolean isShouldProcessViews() {
    return shouldProcessViews;
  }
}
