// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

public class InvalidMethodDefinitionException extends AnnotationProcessingException {
  public InvalidMethodDefinitionException(String description) {
    super(description);
  }
}
