/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import java.io.IOException;

public interface AnnotationProcessor {
  void process() throws IOException, AnnotationProcessingException;
}
