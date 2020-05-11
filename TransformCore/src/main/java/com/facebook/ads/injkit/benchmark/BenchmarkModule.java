// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.benchmark;

import com.facebook.ads.injkit.ConfigurationParser;
import com.facebook.ads.injkit.InjectorFactory;
import com.facebook.ads.injkit.Module;

public class BenchmarkModule implements Module<BenchmarkConfiguration> {
  @Override
  public ConfigurationParser<BenchmarkConfiguration> makeConfigurationParser() {
    return new BenchmarkConfigurationParser();
  }

  @Override
  public InjectorFactory<BenchmarkConfiguration> makeInjectorFactory() {
    return new BenchmarkInjectorFactory();
  }
}
