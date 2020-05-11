// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.crashshield;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CrashShieldConfigurationTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameter(0)
  public String className;

  @Parameterized.Parameter(1)
  public String[] expectedErrors;

  private static Object[] makeBadHandlerParameters(Class<?> badHandlerClass, String... errors) {
    String[] expectedErrorMessages = new String[errors.length + 1];
    System.arraycopy(errors, 0, expectedErrorMessages, 1, errors.length);
    expectedErrorMessages[0] = badHandlerClass.getName();

    return new Object[] {badHandlerClass.getName(), expectedErrorMessages};
  }

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {"some.class.that.does.not.Exist", new String[] {"some.class.that.does.not.Exist"}},
          makeBadHandlerParameters(BadHandler_A1.class, "handleThrowable"),
          makeBadHandlerParameters(BadHandler_A2.class, "handleThrowable", "static"),
          makeBadHandlerParameters(BadHandler_A3.class, "handleThrowable", "public"),
          makeBadHandlerParameters(BadHandler_A4.class, "handleThrowable", "public"),
          makeBadHandlerParameters(BadHandler_A5.class, "handleThrowable", "public"),
          makeBadHandlerParameters(BadHandler_A6.class, "handleThrowable", "Throwable"),
          makeBadHandlerParameters(BadHandler_A7.class, "handleThrowable", "Object"),
          makeBadHandlerParameters(BadHandler_B1.class, "methodFinished"),
          makeBadHandlerParameters(BadHandler_B2.class, "methodFinished", "static"),
          makeBadHandlerParameters(BadHandler_B3.class, "methodFinished", "public"),
          makeBadHandlerParameters(BadHandler_B4.class, "methodFinished", "public"),
          makeBadHandlerParameters(BadHandler_B5.class, "methodFinished", "public"),
          makeBadHandlerParameters(BadHandler_B6.class, "methodFinished", "Object"),
          makeBadHandlerParameters(BadHandler_B7.class, "methodFinished", "Object"),
          makeBadHandlerParameters(BadHandler_B8.class, "methodFinished", "Object"),
          makeBadHandlerParameters(BadHandler_C0.class, "public"),
          makeBadHandlerParameters(BadHandler_C1.class, "public"),
          makeBadHandlerParameters(BadHandler_C2.class, "public"),
        });
  }

  @Test
  public void exceptionHandlerTest() throws Exception {
    try {
      new TransformationEnvironment(temporaryFolder)
          .addProcessingClass(CrashShieldConfigurationTest.class)
          .newLoadableConfigurationWriter()
          .enable(new CrashShieldConfigurationWriter.Factory<>())
          .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
          .transformAnnotation(FakeHandleExceptionsAnnotation.class)
          .handler(className)
          .done()
          .transformAndLoad();
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      for (String expectedError : expectedErrors) {
        assertThat(e).hasMessageThat().contains(expectedError);
      }
    }
  }

  public static class BadHandler_A1 {
    public static void methodFinished(Object obj) {}
  }

  public static class BadHandler_A2 {
    public void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  public static class BadHandler_A3 {
    static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  public static class BadHandler_A4 {
    protected static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  public static class BadHandler_A5 {
    private static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  public static class BadHandler_A6 {
    public static void handleThrowable(Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  public static class BadHandler_A7 {
    private static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  public static class BadHandler_B1 {
    public static void handleThrowable(Throwable t, Object obj) {}
  }

  public static class BadHandler_B2 {
    public static void handleThrowable(Throwable t, Object obj) {}

    public void methodFinished(Object obj) {}
  }

  public static class BadHandler_B3 {
    public static void handleThrowable(Throwable t, Object obj) {}

    static void methodFinished(Object obj) {}
  }

  public static class BadHandler_B4 {
    public static void handleThrowable(Throwable t, Object obj) {}

    protected static void methodFinished(Object obj) {}
  }

  public static class BadHandler_B5 {
    public static void handleThrowable(Throwable t, Object obj) {}

    private static void methodFinished(Object obj) {}
  }

  public static class BadHandler_B6 {
    public static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished() {}
  }

  public static class BadHandler_B7 {
    public static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj, String str) {}
  }

  public static class BadHandler_B8 {
    public static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(String str) {}
  }

  static class BadHandler_C0 {
    public static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  protected static class BadHandler_C1 {
    public static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  private static class BadHandler_C2 {
    public static void handleThrowable(Throwable t, Object obj) {}

    public static void methodFinished(Object obj) {}
  }

  private static class BadHandler2 {
    public void handleThrowable(Object obj, Throwable t) {}

    public static void methodFinished(Object obj) {}
  }
}
