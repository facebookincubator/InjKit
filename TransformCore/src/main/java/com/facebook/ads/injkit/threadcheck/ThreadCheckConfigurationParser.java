/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.threadcheck;

import com.facebook.ads.injkit.ConfigurationParser;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.LineDirectiveSplit;
import com.facebook.ads.injkit.ParseContext;
import com.facebook.ads.injkit.UniqueSetting;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
class ThreadCheckConfigurationParser implements ConfigurationParser<ThreadCheckConfiguration> {
  private final UniqueSetting enabledSetting =
      new UniqueSetting(ThreadCheckConfigurationConstants.ENABLED);
  private final UniqueSetting uiThreadAnnotationClass =
      new UniqueSetting(ThreadCheckConfigurationConstants.UI_THREAD_ANNOTATION_CLASS);
  private final UniqueSetting workerThreadAnnotationClass =
      new UniqueSetting(ThreadCheckConfigurationConstants.WORKER_THREAD_ANNOTATION_CLASS);
  private final UniqueSetting anyThreadAnnotationClass =
      new UniqueSetting(ThreadCheckConfigurationConstants.ANY_THREAD_ANNOTATION_CLASS);
  private final UniqueSetting violationHandlerClass =
      new UniqueSetting(ThreadCheckConfigurationConstants.VIOLATION_HANDLER_CLASS);

  @Override
  public boolean parse(LineDirectiveSplit split, ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException {
    switch (split.getDirective()) {
      case ThreadCheckConfigurationConstants.ENABLED:
        enabledSetting.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      case ThreadCheckConfigurationConstants.UI_THREAD_ANNOTATION_CLASS:
        uiThreadAnnotationClass.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      case ThreadCheckConfigurationConstants.WORKER_THREAD_ANNOTATION_CLASS:
        workerThreadAnnotationClass.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      case ThreadCheckConfigurationConstants.ANY_THREAD_ANNOTATION_CLASS:
        anyThreadAnnotationClass.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      case ThreadCheckConfigurationConstants.VIOLATION_HANDLER_CLASS:
        violationHandlerClass.setValue(ctx, split.getContentsSingleString(ctx));
        return true;
      default:
        return false;
    }
  }

  @Override
  public ThreadCheckConfiguration finish(ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException {
    if (enabledSetting.isSet() && enabledSetting.asBoolean(ctx)) {
      return new ThreadCheckConfiguration(
          true,
          uiThreadAnnotationClass.getValue(ctx),
          workerThreadAnnotationClass.getValue(ctx),
          anyThreadAnnotationClass.getValue(ctx),
          violationHandlerClass.getValue(ctx));
    }

    return new ThreadCheckConfiguration(
        false,
        // NULLSAFE_FIXME[Parameter Not Nullable]
        uiThreadAnnotationClass.isSet() ? uiThreadAnnotationClass.getValue(ctx) : null,
        // NULLSAFE_FIXME[Parameter Not Nullable]
        workerThreadAnnotationClass.isSet() ? workerThreadAnnotationClass.getValue(ctx) : null,
        // NULLSAFE_FIXME[Parameter Not Nullable]
        anyThreadAnnotationClass.isSet() ? anyThreadAnnotationClass.getValue(ctx) : null,
        // NULLSAFE_FIXME[Parameter Not Nullable]
        violationHandlerClass.isSet() ? violationHandlerClass.getValue(ctx) : null);
  }
}
