/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield;

public final class CrashShieldConfigurationConstants {
  private CrashShieldConfigurationConstants() {}

  public static final String ENABLED = "exception-handling-enabled";
  public static final String DISABLE_ANNOTATION_CLASS = "no-auto-handle-exceptions";
  public static final String ENABLE_ANNOTATION_CLASS = "auto-handle-exceptions";
  public static final String EXCEPTION_HANDLER_CLASS = "auto-exception-handler";
  public static final String SHOULD_PROCESS_CONSTRUCTOR = "should-process-constructor";
  public static final String SHOULD_PROCESS_VIEWS = "should-process-views";
}
