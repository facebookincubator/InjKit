/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.sdkdebugger;

import com.facebook.ads.injkit.ConfigurationParser;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.LineDirectiveSplit;
import com.facebook.ads.injkit.ParseContext;
import com.facebook.ads.injkit.UniqueSetting;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
class SdkDebuggerConfigurationParser implements ConfigurationParser<SdkDebuggerConfiguration> {

  private final UniqueSetting sdkDebuggerEnabled =
      new UniqueSetting(SdkDebuggerConfigurationConstants.ENABLED);
  private final UniqueSetting logCallAnnotationClass =
      new UniqueSetting(SdkDebuggerConfigurationConstants.LOG_CALL_ANNOTATIONS);
  private final UniqueSetting callLoggerClass =
      new UniqueSetting(SdkDebuggerConfigurationConstants.CALL_LOGGER);

  @Override
  public boolean parse(LineDirectiveSplit split, ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException {
    switch (split.getDirective()) {
      case SdkDebuggerConfigurationConstants.ENABLED:
        sdkDebuggerEnabled.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      case SdkDebuggerConfigurationConstants.LOG_CALL_ANNOTATIONS:
        logCallAnnotationClass.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      case SdkDebuggerConfigurationConstants.CALL_LOGGER:
        callLoggerClass.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      default:
        return false;
    }
  }

  @Override
  public SdkDebuggerConfiguration finish(ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException {
    if (!sdkDebuggerEnabled.isSet() || !sdkDebuggerEnabled.asBoolean(ctx)) {
      return SdkDebuggerConfiguration.makeDisabled();
    }

    return SdkDebuggerConfiguration.makeEnabled(
        logCallAnnotationClass.getValue(ctx), callLoggerClass.getValue(ctx));
  }
}
