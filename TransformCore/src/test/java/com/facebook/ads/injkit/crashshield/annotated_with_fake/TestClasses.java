/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield.annotated_with_fake;

import com.facebook.ads.injkit.crashshield.FakeHandleExceptionsAnnotation;

public class TestClasses {
  public void foo() {}

  public static class ClassWithNonAnnotatedMethod {
    public void someUnannotatedPublicMethod() {}
  }

  public static class ClassWithAnnotatedMethod {
    @FakeHandleExceptionsAnnotation
    public void annotatedMethod() {}
  }

  @FakeHandleExceptionsAnnotation
  public static class AnnotatedClassWithNonAnnotatedMethod {
    public void nonAnnotatedMethod() {}
  }

  @FakeHandleExceptionsAnnotation
  public static class AnnotatedClassWithAnnotatedMethod {
    @FakeHandleExceptionsAnnotation
    public void annotatedMethod() {}
  }
}
