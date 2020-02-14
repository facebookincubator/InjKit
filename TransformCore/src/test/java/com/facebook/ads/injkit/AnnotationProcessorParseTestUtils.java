// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class AnnotationProcessorParseTestUtils {
    private AnnotationProcessorParseTestUtils() {}

    public static <ConfigurationT> ConfigurationT parse(
            File file,
            ConfigurationParser<ConfigurationT> parser)
            throws InvalidAnnotationProcessorConfigurationException, IOException {
        AtomicReference<ConfigurationT> configHolder = new AtomicReference<>();

        AnnotationProcessorConfiguration annotationConfig =
                AnnotationProcessorConfiguration.parse(
                        file,
                        Collections.singleton(new Module<ConfigurationT>() {
                                @Override
                                public ConfigurationParser<ConfigurationT>
                                makeConfigurationParser() {
                                    return parser;
                                }

                                @Override
                                public InjectorFactory<ConfigurationT> makeInjectorFactory() {
                                    return makeDummyFactory(configHolder);
                                }
                            }));

        annotationConfig.makeInjectors(new URLClassLoader(new URL[0]));

        return configHolder.get();
    }

    private static <ConfigurationT> InjectorFactory<ConfigurationT> makeDummyFactory(
            AtomicReference<ConfigurationT> configHolder) {
        return (URLClassLoader applicationCode, ConfigurationT configuration) -> {
            configHolder.set(configuration);
            return new NopInjector();
        };
    }
}
