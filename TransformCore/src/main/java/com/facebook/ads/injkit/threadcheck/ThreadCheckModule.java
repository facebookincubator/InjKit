// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.threadcheck;

import com.facebook.ads.injkit.ConfigurationParser;
import com.facebook.ads.injkit.InjectorFactory;
import com.facebook.ads.injkit.Module;

public class ThreadCheckModule implements Module<ThreadCheckConfiguration> {
    @Override
    public ConfigurationParser<ThreadCheckConfiguration> makeConfigurationParser() {
        return new ThreadCheckConfigurationParser();
    }

    @Override
    public InjectorFactory<ThreadCheckConfiguration> makeInjectorFactory() {
        return new ThreadCheckInjectorFactory();
    }
}
