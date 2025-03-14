/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;
import com.facebook.infer.annotation.Nullsafe;
import java.net.URLClassLoader;

@Nullsafe(Nullsafe.Mode.LOCAL)
@FunctionalInterface
public interface ClassFileProcessorFactory {
  ClassFileProcessor make(
      AnnotationProcessorConfiguration configuration, URLClassLoader classLoader, Model model)
      throws InvalidAnnotationProcessorConfigurationException;

  static ClassFileProcessorFactory getDefault() {
    return ClassFileProcessorImpl::new;
  }
}
