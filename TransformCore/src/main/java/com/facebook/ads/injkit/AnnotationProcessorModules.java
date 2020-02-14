// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit;

import com.facebook.ads.injkit.benchmark.BenchmarkModule;
import com.facebook.ads.injkit.crashshield.CrashShieldModule;
import com.facebook.ads.injkit.sdkdebugger.SdkDebuggerModule;
import com.facebook.ads.injkit.threadcheck.ThreadCheckModule;

import java.util.Arrays;

class AnnotationProcessorModules {
    private AnnotationProcessorModules() {}

    static Iterable<Module<?>> getModules() {
        return Arrays.asList(
                new CrashShieldModule(),
                new SdkDebuggerModule(),
                new BenchmarkModule(),
                new ThreadCheckModule());
    }
}
