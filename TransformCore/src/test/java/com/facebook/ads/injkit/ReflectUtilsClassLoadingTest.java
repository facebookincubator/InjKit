// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class ReflectUtilsClassLoadingTest {

    @Test
    public void loadsAnnotations() throws Exception {
        Object r = ReflectUtils.loadAnnotation(Foo.class.getName(), Foo.class.getClassLoader());
        assertThat(r).isEqualTo(Foo.class);
    }

    @Test
    public void doesNotLoadNonAnnotation() {
        try {
            ReflectUtils.loadAnnotation(Bar.class.getName(), Bar.class.getClassLoader());
            fail();
        } catch (InvalidAnnotationProcessorConfigurationException e) {
            assertThat(e.getMessage()).contains("Bar");
            assertThat(e.getMessage()).contains("annotation");
        }
    }

    @Test
    public void doesNotLoadNonExistingClass() {
        try {
            ReflectUtils.loadAnnotation(Bar.class.getName() + "r", Bar.class.getClassLoader());
            fail();
        } catch (InvalidAnnotationProcessorConfigurationException e) {
            assertThat(e.getMessage()).contains("Barr");
        }
    }

    public @interface Foo {}

    public interface Bar {}
}
