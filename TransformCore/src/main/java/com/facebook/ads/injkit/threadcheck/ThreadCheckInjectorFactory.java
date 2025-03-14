/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.threadcheck;

import com.facebook.ads.injkit.Injector;
import com.facebook.ads.injkit.InjectorFactory;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.infer.annotation.Nullsafe;
import java.net.URLClassLoader;

@Nullsafe(Nullsafe.Mode.LOCAL)
class ThreadCheckInjectorFactory implements InjectorFactory<ThreadCheckConfiguration> {
  @Override
  public Injector make(URLClassLoader applicationCode, ThreadCheckConfiguration configuration)
      throws InvalidAnnotationProcessorConfigurationException {
    return new ThreadCheckInjector(configuration, applicationCode);
  }
}
