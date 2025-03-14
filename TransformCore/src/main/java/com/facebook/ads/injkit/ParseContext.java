/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import com.facebook.infer.annotation.Nullsafe;
import java.util.Locale;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class ParseContext {
  final String filePath;
  int lineNumber;

  ParseContext(String filePath) {
    this.filePath = filePath;
    lineNumber = 0;
  }

  void nextLine() {
    lineNumber++;
  }

  String lineDescription() {
    return String.format(Locale.US, "%s:%d", filePath, lineNumber);
  }

  String fileDescription() {
    return filePath;
  }
}
