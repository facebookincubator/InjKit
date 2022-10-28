/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import java.util.Locale;
import java.util.regex.Pattern;

public class LineDirectiveSplit {
  private final String directive;
  private final String contents;

  LineDirectiveSplit(String directive, String contents) {
    this.directive = directive;
    this.contents = contents;
  }

  public String getDirective() {
    return directive;
  }

  public String getContentsSingleString(ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException {
    if (contents.isEmpty() || Pattern.matches(".*\\s.*", contents)) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(
              Locale.US,
              "%s: directive '%s' should have a single value",
              ctx.lineDescription(),
              directive));
    }

    return contents;
  }
}
