/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import java.util.Locale;

public class UniqueSetting {
  private final String name;
  private String value;

  public UniqueSetting(String name) {
    this.name = name;
    value = "";
  }

  String getName() {
    return name;
  }

  public void setValue(ParseContext ctx, String value)
      throws InvalidAnnotationProcessorConfigurationException {
    if (!this.value.isEmpty()) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(
              Locale.US,
              "%s: Setting '%s' already defined (previous value '%s')",
              ctx.lineDescription(),
              name,
              this.value));
    }

    this.value = value;
  }

  public String getValue(ParseContext ctx) throws InvalidAnnotationProcessorConfigurationException {
    if (value.isEmpty()) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(Locale.US, "%s: Setting '%s' not defined", ctx.fileDescription(), name));
    }

    return value;
  }

  public boolean isSet() {
    return !value.isEmpty();
  }

  public boolean asBoolean(ParseContext ctx)
      throws InvalidAnnotationProcessorConfigurationException {
    String text = getValue(ctx);

    if (Boolean.TRUE.toString().equals(text)) {
      return true;
    } else if (Boolean.FALSE.toString().equals(text)) {
      return false;
    }

    throw new InvalidAnnotationProcessorConfigurationException(
        String.format(
            Locale.US,
            "'%s' is not '%s' or '%s'",
            text,
            Boolean.TRUE.toString(),
            Boolean.FALSE.toString()));
  }
}
