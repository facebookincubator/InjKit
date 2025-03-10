/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

@FakeHandleExceptionsAnnotation
@RunWith(RobolectricTestRunner.class)
@SuppressLint("CatchGeneralException")
public class CrashShieldAsyncTaskInjectionTest {

  @Rule public TemporaryFolder temporaryFolderRule = new TemporaryFolder();

  private TransformationEnvironment environment;

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolderRule);
  }

  @Test(timeout = 5000)
  @SuppressLint("unchecked")
  public void execute_afterAsyncTaskProcessing_shouldWorkCorrectly() throws Exception {
    ClassLoader classLoader = processClasses(FakeAsyncTask.class);
    Class<?> cls = classLoader.loadClass(FakeAsyncTask.class.getName());

    AtomicBoolean finishedCondition = new AtomicBoolean();

    AsyncTask<Integer, String, String> instance =
        (AsyncTask<Integer, String, String>)
            cls.getConstructor(AtomicBoolean.class).newInstance(finishedCondition);
    instance.executeOnExecutor(
        new Executor() {
          @Override
          public void execute(Runnable runnable) {
            runnable.run();
          }
        },
        42);

    while (!finishedCondition.get()) {
      ShadowLooper.idleMainLooper(1, TimeUnit.SECONDS);
      Thread.sleep(1000);
    }

    Field paramField = instance.getClass().getField("param");
    Field resultField = instance.getClass().getField("result");

    assertEquals(42, paramField.get(instance));
    assertEquals(FakeAsyncTask.SUCCESS, resultField.get(instance));
  }

  @Test
  public void doInBackground_throwsException_shouldCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeAsyncTask.class);
    Class<?> cls = classLoader.loadClass(FakeAsyncTask.class.getName());

    AsyncTask instance = (AsyncTask) cls.getConstructor().newInstance();

    try {
      Method doInBackground =
          instance.getClass().getDeclaredMethod("doInBackground", Integer[].class);
      doInBackground.setAccessible(true);
      doInBackground.invoke(instance, (Integer) null);
    } catch (Throwable t) {
      fail();
    }

    assertTrue(getBooleanFieldValue(instance, "isBeforeException"));
    assertFalse(getBooleanFieldValue(instance, "isAfterException"));
  }

  @Test
  public void onPreExecute_throwsException_shouldCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeAsyncTask.class);
    Class<?> cls = classLoader.loadClass(FakeAsyncTask.class.getName());

    AsyncTask instance = (AsyncTask) cls.getConstructor().newInstance();

    try {
      Method onPreExecute = instance.getClass().getDeclaredMethod("onPreExecute");
      onPreExecute.setAccessible(true);
      onPreExecute.invoke(instance);
    } catch (Throwable t) {
      fail();
    }

    assertTrue(getBooleanFieldValue(instance, "isPreExecuteBeforeException"));
    assertFalse(getBooleanFieldValue(instance, "isPreExecuteAfterException"));
  }

  @Test
  public void onPostExecute_throwsException_shouldCatchException() throws Exception {
    ClassLoader classLoader = processClasses(FakeAsyncTask.class);
    Class<?> cls = classLoader.loadClass(FakeAsyncTask.class.getName());

    AsyncTask instance = (AsyncTask) cls.getConstructor().newInstance();

    try {
      Method onPostExecute = instance.getClass().getDeclaredMethod("onPostExecute", String.class);
      onPostExecute.setAccessible(true);
      onPostExecute.invoke(instance, (String) null);
    } catch (Throwable t) {
      fail();
    }

    assertTrue(getBooleanFieldValue(instance, "isPostExecuteBeforeException"));
    assertFalse(getBooleanFieldValue(instance, "isPostExecuteAfterException"));
  }

  private ClassLoader processClasses(Class<?>... processingClass) throws Exception {

    String pkg = null;
    for (Class clazz : processingClass) {
      environment.addProcessingClass(clazz);
      String clsPkg = clazz.getName().substring(0, clazz.getName().lastIndexOf('.'));
      if (pkg == null) {
        pkg = clsPkg;
      } else {
        assertThat(pkg).isEqualTo(clsPkg);
      }
    }

    assertThat(pkg).isNotNull();

    ClassLoader classLoader =
        environment
            .newLoadableConfigurationWriter()
            .enable(new CrashShieldConfigurationWriter.Factory<>())
            .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
            .transformAnnotation(FakeHandleExceptionsAnnotation.class)
            .handler(FakeExceptionHandler.class)
            .shouldProcessViews(true)
            .processPackage(pkg)
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
  public static class FakeAsyncTask extends AsyncTask<Integer, String, String> {

    public static final String SUCCESS = "success";

    public boolean isBeforeException = false;
    public boolean isAfterException = false;

    public boolean isPreExecuteBeforeException = false;
    public boolean isPreExecuteAfterException = false;

    public boolean isPostExecuteBeforeException = false;
    public boolean isPostExecuteAfterException = false;

    public Integer param;
    public String result;

    private final boolean throwException;

    @Nullable private final AtomicBoolean finishedCondition;

    public FakeAsyncTask(AtomicBoolean finishedCondition) {
      this.throwException = false;
      this.finishedCondition = finishedCondition;
    }

    public FakeAsyncTask() {
      this.throwException = true;
      this.finishedCondition = null;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      isPreExecuteBeforeException = true;
      if (throwException) {
        throw new RuntimeException("onPreExecute() exception");
      }
      isPreExecuteAfterException = true;
    }

    @Override
    protected String doInBackground(Integer... integers) {
      if (integers != null && integers.length > 0) {
        param = integers[0];
      }

      isBeforeException = true;
      if (throwException) {
        throw new RuntimeException("doInBackground() exception");
      }
      isAfterException = true;

      return SUCCESS;
    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);

      result = s;

      isPostExecuteBeforeException = true;
      if (throwException) {
        throw new RuntimeException("onPostExecute() exception");
      }
      isPostExecuteAfterException = true;

      if (finishedCondition != null) {
        finishedCondition.set(true);
      }
    }
  }
}
