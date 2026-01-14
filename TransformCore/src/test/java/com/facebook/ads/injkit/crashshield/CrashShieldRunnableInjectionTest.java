/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import android.annotation.SuppressLint;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@FakeHandleExceptionsAnnotation
@RunWith(RobolectricTestRunner.class)
@SuppressLint("CatchGeneralException")
public class CrashShieldRunnableInjectionTest {

  @Rule public TemporaryFolder temporaryFolderRule = new TemporaryFolder();

  private TransformationEnvironment environment;

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolderRule);
  }

  @Test
  public void run_throwsException_shouldCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeRunnable.class);
    Class<?> cls = classLoader.loadClass(FakeRunnable.class.getName());

    Runnable instance = (Runnable) cls.getConstructor().newInstance();

    try {
      instance.run();
    } catch (Throwable t) {
      fail("");
    }

    assertThat(getBooleanFieldValue(instance, "isBeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isAfterException")).isFalse();
  }

  @Test
  public void run_throwsException_shouldNotCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeNoInjRunnable.class);
    Class<?> cls = classLoader.loadClass(FakeNoInjRunnable.class.getName());

    Runnable instance = (Runnable) cls.getConstructor().newInstance();
    boolean throwsException = false;

    try {
      instance.run();
    } catch (Throwable t) {
      throwsException = true;
    }

    assertThat(throwsException).isTrue();
    assertThat(getBooleanFieldValue(instance, "isBeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isAfterException")).isFalse();
  }

  private ClassLoader processClasses(Class<?>... processingClass) throws Exception {

    for (Class clazz : processingClass) {
      environment.addProcessingClass(clazz);
    }

    return environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .shouldProcessViews(true)
        .processPackage(processingClass)
        .done()
        .transformAndLoad();
  }

  public static boolean getBooleanFieldValue(Object instance, String methodName) {
    try {
      Field field = instance.getClass().getField(methodName);
      return (Boolean) field.get(instance);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressLint("ClownyBooleanExpression")
  public static class FakeRunnable implements Runnable {

    public boolean isBeforeException = false;
    public boolean isAfterException = false;

    @Override
    public void run() {
      isBeforeException = true;
      if (true) {
        throw new RuntimeException("run() exception");
      }
      isAfterException = true;
    }
  }

  @SuppressLint("ClownyBooleanExpression")
  @FakeDoNotHandleExceptionAnnotation
  public static class FakeNoInjRunnable implements Runnable {

    public boolean isBeforeException = false;
    public boolean isAfterException = false;

    @Override
    public void run() {
      isBeforeException = true;
      if (true) {
        throw new RuntimeException("run() exception");
      }
      isAfterException = true;
    }
  }
}
