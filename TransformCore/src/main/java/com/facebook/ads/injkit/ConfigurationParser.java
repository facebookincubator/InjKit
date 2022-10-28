/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

public interface ConfigurationParser<ConfigurationT> {
  boolean parse(LineDirectiveSplit split, ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException;

  ConfigurationT finish(ParseContext ctx) throws InvalidAnnotationProcessorConfigurationException;
}
