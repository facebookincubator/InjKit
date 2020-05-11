// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.crashshield;

import com.facebook.ads.injkit.Injector;
import com.facebook.ads.injkit.InjectorFactory;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import java.net.URLClassLoader;

class CrashShieldInjectorFactory implements InjectorFactory<CrashShieldConfiguration> {
  @Override
  public Injector make(URLClassLoader applicationCode, CrashShieldConfiguration configuration)
      throws InvalidAnnotationProcessorConfigurationException {
    return CrashShieldInjector.make(applicationCode, configuration);
  }
}
