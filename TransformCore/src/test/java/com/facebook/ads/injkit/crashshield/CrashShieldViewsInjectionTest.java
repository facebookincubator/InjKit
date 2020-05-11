// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.crashshield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import com.facebook.ads.injkit.TransformationEnvironment;
import com.facebook.ads.injkit.crashshield.safe_components.SafeView;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@FakeHandleExceptionsAnnotation
@RunWith(RobolectricTestRunner.class)
@SuppressLint("CatchGeneralException")
public class CrashShieldViewsInjectionTest {

  @Rule public TemporaryFolder temporaryFolderRule = new TemporaryFolder();

  private TransformationEnvironment environment;
  private final String SAFE_VIEW_INAME =
      "com/facebook/ads/injkit/crashshield/safe_components/SafeView";
  private final String SAFE_VIEW_GROUP_INAME =
      "com/facebook/ads/injkit/crashshield/safe_components/SafeViewGroup";

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolderRule);
    CrashShieldInjector.setSafeNames(SAFE_VIEW_INAME, SAFE_VIEW_GROUP_INAME);
  }

  @Test
  public void fakeViewAOnMeasure_throwsExceptionBeforeSuperCall_catchException() throws Exception {

    ClassLoader classLoader =
        processClasses(
            FakeViewA.class, FakeViewB.class, Class.forName(FakeViewA.class.getName() + "$1"));
    Class<?> cls = classLoader.loadClass(FakeViewB.class.getName());
    SafeView instance =
        (SafeView) cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    setBooleanFieldValueForView(instance, "throwsExceptionABefore", true);
    instance.measure(1, 1);

    assertTrue(getBooleanFieldValueView(instance, "isMethodBBeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodBAfterException"));
    assertTrue(getBooleanFieldValueView(instance, "isMethodABeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodAAfterException"));
  }

  @Test
  public void fakeViewAOnMeasure_throwsExceptionAfterSuperCall_catchException() throws Exception {

    ClassLoader classLoader =
        processClasses(
            FakeViewA.class, FakeViewB.class, Class.forName(FakeViewA.class.getName() + "$1"));
    Class<?> cls = classLoader.loadClass(FakeViewB.class.getName());
    SafeView instance =
        (SafeView) cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    setBooleanFieldValueForView(instance, "throwsExceptionAAfter", true);
    instance.measure(1, 1);

    assertTrue(getBooleanFieldValueView(instance, "isMethodBBeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodBAfterException"));
    assertTrue(getBooleanFieldValueView(instance, "isMethodABeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodAAfterException"));
  }

  @Test
  public void fakeViewBOnMeasure_throwsExceptionBeforeSuperCall_catchException() throws Exception {

    ClassLoader classLoader =
        processClasses(
            FakeViewA.class, FakeViewB.class, Class.forName(FakeViewA.class.getName() + "$1"));
    Class<?> cls = classLoader.loadClass(FakeViewB.class.getName());
    SafeView instance =
        (SafeView) cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    setBooleanFieldValueForView(instance, "throwsExceptionBBefore", true);
    instance.measure(1, 1);

    assertTrue(getBooleanFieldValueView(instance, "isMethodBBeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodBAfterException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodABeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodAAfterException"));
  }

  @Test
  public void fakeViewBOnMeasure_throwsExceptionAfterSuperCall_catchException() throws Exception {

    ClassLoader classLoader =
        processClasses(
            FakeViewA.class, FakeViewB.class, Class.forName(FakeViewA.class.getName() + "$1"));
    Class<?> cls = classLoader.loadClass(FakeViewB.class.getName());
    SafeView instance =
        (SafeView) cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    setBooleanFieldValueForView(instance, "throwsExceptionBAfter", true);
    instance.measure(1, 1);

    assertTrue(getBooleanFieldValueView(instance, "isMethodBBeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodBAfterException"));
    assertTrue(getBooleanFieldValueView(instance, "isMethodABeforeException"));
    assertTrue(getBooleanFieldValueView(instance, "isMethodAAfterException"));
  }

  @Test
  public void processFakeViewA_withAnonymousClass_shouldRenameInAnonymousClass() throws Exception {

    ClassLoader classLoader =
        processClasses(FakeViewA.class, Class.forName(FakeViewA.class.getName() + "$1"));
    Class<?> cls = classLoader.loadClass(FakeViewA.class.getName());
    View fakeViewA =
        (View) cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    Field rField = fakeViewA.getClass().getDeclaredField("r");
    rField.setAccessible(true);
    Runnable r = ((Runnable) rField.get(fakeViewA));
    r.run();

    // verify that call inside run() renamed from onMeasure() to safe_onMeasure()
    assertFalse(((SafeView) fakeViewA).onMeasureCalled);
    assertTrue(((SafeView) fakeViewA).safe_onMeasureCalled);
  }

  public static void setBooleanFieldValueForView(
      View instance, String fieldName, boolean fieldValue) {
    try {
      Field field = instance.getClass().getField(fieldName);
      field.set(instance, fieldValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean getBooleanFieldValueView(View instance, String methodName) {
    try {
      Field field = instance.getClass().getField(methodName);
      return (Boolean) field.get(instance);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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

  public static boolean getBooleanFieldValue(View.OnClickListener instance, String methodName) {
    try {
      Field field = instance.getClass().getField(methodName);
      return (Boolean) field.get(instance);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @FakeHandleExceptionsAnnotation
  public static class FakeViewA extends View {

    public boolean isMethodABeforeException = false;
    public boolean isMethodAAfterException = false;

    public boolean throwsExceptionABefore = false;
    public boolean throwsExceptionAAfter = false;

    public Runnable r;

    public FakeViewA(Context context) {
      super(context);
      r =
          new Runnable() {

            @FakeHandleExceptionsAnnotation
            @Override
            public void run() {
              FakeViewA.super.onMeasure(0, 0);
            }
          };
    }

    @Override
    public void onMeasure(int a, int b) {
      isMethodABeforeException = true;
      if (throwsExceptionABefore) {
        throw new RuntimeException("");
      }
      super.onMeasure(a, b);
      if (throwsExceptionAAfter) {
        throw new RuntimeException("");
      }
      isMethodAAfterException = true;
    }
  }

  public static class FakeViewB extends FakeViewA {

    public boolean isMethodBBeforeException = false;
    public boolean isMethodBAfterException = false;

    public boolean throwsExceptionBBefore = false;
    public boolean throwsExceptionBAfter = false;

    public FakeViewB(Context context) {
      super(context);
    }

    @Override
    public void onMeasure(int a, int b) {
      isMethodBBeforeException = true;
      if (throwsExceptionBBefore) {
        throw new RuntimeException("");
      }
      super.onMeasure(a, b);
      if (throwsExceptionBAfter) {
        throw new RuntimeException("");
      }
      isMethodBAfterException = true;
    }
  }
}
