// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.crashshield;

import static com.google.common.truth.Truth.assertThat;

import android.annotation.SuppressLint;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@SuppressLint("")
public class CrashShieldInvocationTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameter(0)
  public Class<?> cls;

  @Parameterized.Parameter(1)
  public String methodName;

  @Parameterized.Parameter(2)
  public boolean isStatic;

  @Parameterized.Parameter(3)
  public boolean tryCatchInjected;

  @Parameterized.Parameter(4)
  public boolean shouldProcessConstructors;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {TestClass1.class, "methodToCall", true, true, false},
          {TestClass2.class, "methodToCall", false, true, false},
          {TestClass3.class, "methodToCall", false, true, false},
          {TestClass4.class, "methodToCall", false, true, true},
          {TestClass4.class, "methodToCall", false, false, false},
          {TestClass5.class, "methodToCall", false, true, true},
          {TestClass5.class, "methodToCall", false, false, false},
          {UnsupportedSubClass.class, "methodToCall", false, false, false}
        });
  }

  @Before
  public void before() {
    FakeExceptionHandler.reset();
  }

  @Test
  public void testCrashShield() throws Exception {
    Class<?> loaded =
        new TransformationEnvironment(temporaryFolder)
            .addProcessingClass(cls)
            .newLoadableConfigurationWriter()
            .enable(new CrashShieldConfigurationWriter.Factory<>())
            .transformAnnotation(FakeHandleExceptionsAnnotation.class)
            .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
            .handler(FakeExceptionHandler.class)
            .shouldProcessConstructor(shouldProcessConstructors)
            .processPackage(cls)
            .done()
            .transformAndLoad()
            .loadClass(cls.getName());

    if (!tryCatchInjected) {
      boolean throwsException = false;
      try {
        loaded.getConstructor().newInstance();
      } catch (InvocationTargetException t) {
        throwsException = true;
      }
      Assert.assertTrue(throwsException);
      return;
    }

    Method method = loaded.getMethod(methodName);
    if (isStatic) {
      method.invoke(null);

      assertThat(FakeExceptionHandler.getHandledObjects()).containsExactly(loaded);
    } else {
      Object instance = loaded.newInstance();
      method.invoke(instance);

      assertThat(FakeExceptionHandler.getHandledObjects()).containsExactly(instance);
    }

    assertThat(tryCatchInjected).isTrue();
    assertThat(FakeExceptionHandler.getHandledThrowables()).hasSize(1);
    assertThat(FakeExceptionHandler.getHandledThrowables().get(0))
        .hasMessageThat()
        .isEqualTo("boom");
  }

  public static class TestClass1 {
    @FakeHandleExceptionsAnnotation
    public static void methodToCall() {
      throw new RuntimeException("boom");
    }
  }

  public static class TestClass2 {
    @FakeHandleExceptionsAnnotation
    public void methodToCall() {
      throw new RuntimeException("boom");
    }
  }

  @FakeHandleExceptionsAnnotation
  public static class TestClass3 {
    public void methodToCall() {
      throw new RuntimeException("boom");
    }
  }

  @FakeHandleExceptionsAnnotation
  public static class TestClass4 {
    public TestClass4() {
      throw new RuntimeException("boom");
    }

    public void methodToCall() {}
  }

  @FakeHandleExceptionsAnnotation
  public static class TestClass5 {
    public TestClass5() {
      this("another constructor!");
    }

    private TestClass5(String unused) {
      throw new RuntimeException("boom");
    }

    public void methodToCall() {}
  }

  public static class UnsupportedClass {
    public UnsupportedClass() {}

    public UnsupportedClass(UnsupportedClass u) {}
  }

  @FakeHandleExceptionsAnnotation
  public static class UnsupportedSubClass extends UnsupportedClass {
    public UnsupportedSubClass() {
      super(new UnsupportedClass());
      throw new RuntimeException("test");
    }
  }
}
