// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.crashshield;

import com.facebook.ads.injkit.ConfigurationParser;
import com.facebook.ads.injkit.InjectorFactory;
import com.facebook.ads.injkit.Module;

public class CrashShieldModule implements Module<CrashShieldConfiguration> {
    @Override
    public ConfigurationParser<CrashShieldConfiguration> makeConfigurationParser() {
        return new CrashShieldConfigurationParser();
    }

    @Override
    public InjectorFactory<CrashShieldConfiguration> makeInjectorFactory() {
        return new CrashShieldInjectorFactory();
    }
}
