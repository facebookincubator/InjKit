/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield;

import static org.assertj.core.api.Assertions.assertThat;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@FakeHandleExceptionsAnnotation
@RunWith(RobolectricTestRunner.class)
@SuppressLint("CatchGeneralException")
public class CrashShieldViewListenersInjectionTest {

  @Rule public TemporaryFolder temporaryFolderRule = new TemporaryFolder();

  private TransformationEnvironment environment;

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolderRule);
  }

  @Test
  public void doInBackground_throwsException_shouldCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeAsyncTask.class);
    Class<?> cls = classLoader.loadClass(FakeAsyncTask.class.getName());

    AsyncTask instance = (AsyncTask) cls.getConstructor().newInstance();
    boolean throwsException = false;

    try {
      Method doInBackground =
          instance.getClass().getDeclaredMethod("doInBackground", Integer[].class);
      doInBackground.setAccessible(true);
      doInBackground.invoke(instance, (Integer) null);
    } catch (Throwable t) {
      throwsException = true;
    }

    assertThat(getBooleanFieldValue(instance, "isBeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isAfterException")).isFalse();

    assertThat(throwsException).isFalse();
  }

  @Test
  public void onPreExecute_throwsException_shouldCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeAsyncTask.class);
    Class<?> cls = classLoader.loadClass(FakeAsyncTask.class.getName());

    AsyncTask instance = (AsyncTask) cls.getConstructor().newInstance();
    boolean throwsPreException = false;

    try {
      Method onPreExecute = instance.getClass().getDeclaredMethod("onPreExecute");
      onPreExecute.setAccessible(true);
      onPreExecute.invoke(instance);
    } catch (Throwable t) {
      throwsPreException = true;
    }

    assertThat(getBooleanFieldValue(instance, "isPreExecuteBeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isPreExecuteAfterException")).isFalse();

    assertThat(throwsPreException).isFalse();
  }

  @Test
  public void onPostExecute_throwsException_shouldCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeAsyncTask.class);
    Class<?> cls = classLoader.loadClass(FakeAsyncTask.class.getName());

    AsyncTask instance = (AsyncTask) cls.getConstructor().newInstance();
    boolean throwsPostException = false;

    try {
      Method onPostExecute = instance.getClass().getDeclaredMethod("onPostExecute", Boolean.class);
      onPostExecute.setAccessible(true);
      onPostExecute.invoke(instance, (Boolean) null);
    } catch (Throwable t) {
      throwsPostException = true;
    }

    assertThat(getBooleanFieldValue(instance, "isPostExecuteBeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isPostExecuteAfterException")).isFalse();

    assertThat(throwsPostException).isFalse();
  }

  @Test
  public void listenerAOnClick_throwsException_shouldCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeListenerA.class);
    Class<?> cls = classLoader.loadClass(FakeListenerA.class.getName());

    View.OnClickListener instance = (View.OnClickListener) cls.getConstructor().newInstance();
    boolean throwsException = false;

    try {
      instance.onClick(null);
    } catch (Throwable t) {
      throwsException = true;
    }

    assertThat(getBooleanFieldValue(instance, "isOnClickABeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isOnClickAAfterException")).isFalse();

    assertThat(throwsException).isFalse();
  }

  @Test
  public void listenerBOnClick_throwsExceptionBeforeSuperCall_catchAndNoListenerACall()
      throws Exception {
    ClassLoader classLoader = processClasses(FakeListenerB.class, FakeListenerA.class);
    Class<?> cls = classLoader.loadClass(FakeListenerB.class.getName());
    View.OnClickListener instance = (View.OnClickListener) cls.getConstructor().newInstance();

    instance.onClick(null);

    assertThat(getBooleanFieldValue(instance, "isOnClickBBeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isOnClickAAfterException")).isFalse();
    assertThat(getBooleanFieldValue(instance, "isOnClickABeforeException")).isFalse();
    assertThat(getBooleanFieldValue(instance, "isOnClickAAfterException")).isFalse();
  }

  @Test
  public void listenerCOnClick_throwsExceptionAfterSuperCall_callSuperBAndCatchExceptionInC()
      throws Exception {
    ClassLoader classLoader =
        processClasses(FakeListenerC.class, FakeListenerB.class, FakeListenerA.class);
    Class<?> cls = classLoader.loadClass(FakeListenerC.class.getName());
    View.OnClickListener instance = (View.OnClickListener) cls.getConstructor().newInstance();

    instance.onClick(null);

    assertThat(getBooleanFieldValue(instance, "isOnClickABeforeException")).isFalse();
    assertThat(getBooleanFieldValue(instance, "isOnClickAAfterException")).isFalse();
    assertThat(getBooleanFieldValue(instance, "isOnClickBBeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isOnClickBAfterException")).isFalse();
    assertThat(getBooleanFieldValue(instance, "isOnClickCBeforeException")).isTrue();
    assertThat(getBooleanFieldValue(instance, "isOnClickCAfterException")).isFalse();
  }

  private ClassLoader processClasses(Class<?>... processingClass) throws Exception {

    for (Class clazz : processingClass) {
      environment.addProcessingClass(clazz);
    }

    ClassLoader classLoader =
        environment
            .newLoadableConfigurationWriter()
            .enable(new CrashShieldConfigurationWriter.Factory<>())
            .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
            .transformAnnotation(FakeHandleExceptionsAnnotation.class)
            .handler(FakeExceptionHandler.class)
            .shouldProcessViews(true)
            .processPackage(processingClass)
            .done()
            .transformAndLoad();

    return classLoader;
  }

  public static boolean getBooleanFieldValue(Object instance, String methodName) {
    try {
      Field field = instance.getClass().getField(methodName);
      return (Boolean) field.get(instance);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressLint({"BadSuperClassAsyncTask.FbAsyncTask", "ClownyBooleanExpression"})
  public static class FakeAsyncTask extends AsyncTask<Integer, String, Boolean> {

    public boolean isBeforeException = false;
    public boolean isAfterException = false;

    public boolean isPreExecuteBeforeException = false;
    public boolean isPreExecuteAfterException = false;

    public boolean isPostExecuteBeforeException = false;
    public boolean isPostExecuteAfterException = false;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      isPreExecuteBeforeException = true;
      if (true) {
        throw new RuntimeException("onPreExecute() exception");
      }
      isPreExecuteAfterException = true;
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
      isBeforeException = true;
      if (true) {
        throw new RuntimeException("doInBackground() exception");
      }
      isAfterException = true;

      return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
      super.onPostExecute(aBoolean);

      isPostExecuteBeforeException = true;
      if (true) {
        throw new RuntimeException("onPostExecute() exception");
      }
      isPostExecuteAfterException = true;
    }
  }

  @SuppressLint("ClownyBooleanExpression")
  public static class FakeListenerA implements View.OnClickListener {

    public boolean isOnClickABeforeException = false;
    public boolean isOnClickAAfterException = false;

    @Override
    public void onClick(View view) {
      isOnClickABeforeException = true;
      if (true) {
        throw new RuntimeException("onClick() exception");
      }
      isOnClickAAfterException = true;
    }
  }

  @SuppressLint("ClownyBooleanExpression")
  public static class FakeListenerB extends FakeListenerA {

    public boolean isOnClickBBeforeException = false;
    public boolean isOnClickBAfterException = false;

    @Override
    public void onClick(View view) {
      isOnClickBBeforeException = true;
      if (true) {
        throw new RuntimeException("onClick() in listener B exception");
      }
      super.onClick(view);
      isOnClickBAfterException = true;
    }
  }

  @SuppressLint("ClownyBooleanExpression")
  public static class FakeListenerC extends FakeListenerB {

    public boolean isOnClickCBeforeException = false;
    public boolean isOnClickCAfterException = false;

    @Override
    public void onClick(View view) {
      isOnClickCBeforeException = true;
      super.onClick(view);
      if (true) {
        throw new RuntimeException("onClick() in listener B exception");
      }
      isOnClickCAfterException = true;
    }
  }
}
