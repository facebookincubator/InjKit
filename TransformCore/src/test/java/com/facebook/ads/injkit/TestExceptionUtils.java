/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import static org.assertj.core.api.Assertions.fail;

import java.util.Locale;
import javax.annotation.Nullable;

public class TestExceptionUtils {
  private TestExceptionUtils() {}

  public static void expectChainContainsMessage(Throwable t, String message) {
    for (Throwable tt = t; tt != null; tt = causeOf(tt)) {
      if (tt.getMessage() == null) {
        continue;
      }

      if (tt.getMessage().contains(message)) {
        return;
      }
    }

    fail(String.format(Locale.US, "Expected message chain to contain '%s'", message));
  }

  @Nullable
  private static Throwable causeOf(Throwable t) {
    if (t.getCause() == t) {
      return null;
    }

    return t.getCause();
  }
}
