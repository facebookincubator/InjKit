/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.benchmark;

import com.facebook.ads.injkit.ConfigurationParser;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.LineDirectiveSplit;
import com.facebook.ads.injkit.ParseContext;
import com.facebook.ads.injkit.UniqueSetting;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
class BenchmarkConfigurationParser implements ConfigurationParser<BenchmarkConfiguration> {
  private final UniqueSetting benchmarkEnabled =
      new UniqueSetting(BenchmarkConfigurationConstants.ENABLED);
  private final UniqueSetting benchmarkAnnotationClass =
      new UniqueSetting(BenchmarkConfigurationConstants.ANNOTATION_CLASS);
  private final UniqueSetting benchmarkReceiverClass =
      new UniqueSetting(BenchmarkConfigurationConstants.RECEIVER_CLASS);

  @Override
  public boolean parse(LineDirectiveSplit split, ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException {
    switch (split.getDirective()) {
      case BenchmarkConfigurationConstants.ENABLED:
        benchmarkEnabled.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      case BenchmarkConfigurationConstants.ANNOTATION_CLASS:
        benchmarkAnnotationClass.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      case BenchmarkConfigurationConstants.RECEIVER_CLASS:
        benchmarkReceiverClass.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      default:
        return false;
    }
  }

  @Override
  public BenchmarkConfiguration finish(ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException {
    if (!benchmarkEnabled.isSet() || !benchmarkEnabled.asBoolean(ctx)) {
      return BenchmarkConfiguration.makeDisabled(
          // NULLSAFE_FIXME[Parameter Not Nullable]
          benchmarkAnnotationClass.isSet() ? benchmarkAnnotationClass.getValue(ctx) : null);
    }

    return BenchmarkConfiguration.makeEnabled(
        benchmarkAnnotationClass.getValue(ctx), benchmarkReceiverClass.getValue(ctx));
  }
}
