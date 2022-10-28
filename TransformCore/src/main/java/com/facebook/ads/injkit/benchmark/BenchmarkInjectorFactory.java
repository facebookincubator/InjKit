/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.benchmark;

import com.facebook.ads.injkit.Injector;
import com.facebook.ads.injkit.InjectorFactory;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import java.net.URLClassLoader;

class BenchmarkInjectorFactory implements InjectorFactory<BenchmarkConfiguration> {
  @Override
  public Injector make(URLClassLoader applicationCode, BenchmarkConfiguration configuration)
      throws InvalidAnnotationProcessorConfigurationException {
    return BenchmarkInjector.make(applicationCode, configuration);
  }
}
