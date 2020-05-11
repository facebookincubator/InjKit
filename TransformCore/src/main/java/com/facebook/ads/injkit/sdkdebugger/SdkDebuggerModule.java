// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.sdkdebugger;

import com.facebook.ads.injkit.ConfigurationParser;
import com.facebook.ads.injkit.InjectorFactory;
import com.facebook.ads.injkit.Module;

public class SdkDebuggerModule implements Module<SdkDebuggerConfiguration> {
  @Override
  public ConfigurationParser<SdkDebuggerConfiguration> makeConfigurationParser() {
    return new SdkDebuggerConfigurationParser();
  }

  @Override
  public InjectorFactory<SdkDebuggerConfiguration> makeInjectorFactory() {
    return new SdkDebuggerInjectorFactory();
  }
}
