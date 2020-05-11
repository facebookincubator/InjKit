// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.model;

@FunctionalInterface
public interface ModelFactory {
  Model make();

  static ModelFactory defaultFactory() {
    return ModelImpl::new;
  }
}
