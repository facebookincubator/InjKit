/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

public class InvalidAnnotationProcessorConfigurationException extends Exception {
  public InvalidAnnotationProcessorConfigurationException(String description) {
    super(description);
  }

  public InvalidAnnotationProcessorConfigurationException(String description, Throwable cause) {
    super(description, cause);
  }
}
