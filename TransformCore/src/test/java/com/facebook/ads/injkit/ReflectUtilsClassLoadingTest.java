/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReflectUtilsClassLoadingTest {

  @Test
  public void loadsAnnotations() throws Exception {
    Object r = ReflectUtils.loadAnnotation(Foo.class.getName(), Foo.class.getClassLoader());
    assertThat(r).isEqualTo(Foo.class);
  }

  @Test
  public void doesNotLoadNonAnnotation() {
    assertThatThrownBy(
            () -> ReflectUtils.loadAnnotation(Bar.class.getName(), Bar.class.getClassLoader()))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining("Bar")
        .hasMessageContaining("annotation");
  }

  @Test
  public void doesNotLoadNonExistingClass() {
    assertThatThrownBy(
            () ->
                ReflectUtils.loadAnnotation(Bar.class.getName() + "r", Bar.class.getClassLoader()))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining("Barr");
  }

  public @interface Foo {}

  public interface Bar {}
}
