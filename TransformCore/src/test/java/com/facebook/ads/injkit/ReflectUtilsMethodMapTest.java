/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReflectUtilsMethodMapTest {

  @Test
  public void mapMethodThatExists() throws Exception {
    Method m = ClassWithMethod.class.getDeclaredMethod("mm", int.class, String.class);

    Executable mapped = ReflectUtils.mapMethod(m, SuccessfulMap.class);
    Method expected = SuccessfulMap.class.getDeclaredMethod("mm", int.class, String.class);

    assertThat(mapped).isEqualTo(expected);
  }

  @Test
  public void mapMethodThatDoesNotExist() throws Exception {
    Method m = ClassWithMethod.class.getDeclaredMethod("mm", int.class, String.class);

    Executable mapped = ReflectUtils.mapMethod(m, MethodDoesNotExist.class);

    assertThat(mapped).isNull();
  }

  @Test
  public void mapMethodThatHasDifferentParameters() throws Exception {
    Method m = ClassWithMethod.class.getDeclaredMethod("mm", int.class, String.class);

    Executable mapped = ReflectUtils.mapMethod(m, DifferentParameters.class);

    assertThat(mapped).isNull();
  }

  @Test
  public void mapMethodToPrivateMethod() throws Exception {
    Method m = ClassWithMethod.class.getDeclaredMethod("mm", int.class, String.class);

    Executable mapped = ReflectUtils.mapMethod(m, PrivateMethod.class);

    assertThat(mapped).isNull();
  }

  @Test
  public void mapMethodToStaticMethod() throws Exception {
    Method m = ClassWithMethod.class.getDeclaredMethod("mm", int.class, String.class);

    Executable mapped = ReflectUtils.mapMethod(m, StaticMethod.class);

    assertThat(mapped).isNull();
  }

  @Test
  public void mapStaticMethodToStaticMethod() throws Exception {
    Method m = StaticMethod.class.getDeclaredMethod("mm", int.class, String.class);

    Executable mapped = ReflectUtils.mapMethod(m, StaticMethod2.class);

    assertThat(mapped).isNull();
  }

  @Test
  public void mapStaticMethodToNonStaticMethod() throws Exception {
    Method m = StaticMethod.class.getDeclaredMethod("mm", int.class, String.class);

    Executable mapped = ReflectUtils.mapMethod(m, ClassWithMethod.class);

    assertThat(mapped).isNull();
  }

  @Test
  public void mapMethodToNullClassIsNull() throws Exception {
    Method m = StaticMethod.class.getDeclaredMethod("mm", int.class, String.class);

    Executable mapped = ReflectUtils.mapMethod(m, null);

    assertThat(mapped).isNull();
  }

  @Test
  public void mapConstructorThatExists() throws Exception {
    Constructor<?> m = SubClass.class.getDeclaredConstructor(int.class);

    Executable mapped = ReflectUtils.mapMethod(m, BaseClass.class);

    assertThat(mapped).isNull();
  }

  static class ClassWithMethod {
    void mm(int x, String y) {}
  }

  static class SuccessfulMap {
    void mm(int x, String y) {}
  }

  static class MethodDoesNotExist {
    void nn(int x, String y) {}
  }

  static class DifferentParameters {
    void mm(int x, String y, double z) {}
  }

  static class PrivateMethod {
    private void mm(int x, String y) {}
  }

  static class StaticMethod {
    static void mm(int x, String y) {}
  }

  static class StaticMethod2 {
    static void mm(int x, String y) {}
  }

  static class BaseClass {
    BaseClass(int i) {}
  }

  static class SubClass extends BaseClass {
    SubClass(int i) {
      super(i);
    }
  }
}
