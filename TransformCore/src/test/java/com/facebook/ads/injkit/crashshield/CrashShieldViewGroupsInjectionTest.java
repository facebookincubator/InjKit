// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.crashshield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.ads.injkit.TransformationEnvironment;
import com.facebook.ads.injkit.crashshield.safe_components.SafeViewGroup;
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
public class CrashShieldViewGroupsInjectionTest {

  @Rule public TemporaryFolder temporaryFolderRule = new TemporaryFolder();

  private TransformationEnvironment environment;
  private static final String SAFE_VIEW_INAME =
      "com/facebook/ads/injkit/crashshield/safe_components/SafeView";
  private static final String SAFE_VIEW_GROUP_INAME =
      "com/facebook/ads/injkit/crashshield/safe_components/SafeViewGroup";

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolderRule);
    CrashShieldInjector.setSafeNames(SAFE_VIEW_INAME, SAFE_VIEW_GROUP_INAME);
  }

  @Test
  public void fakeViewGroupAonMeasure_throwsExceptionBeforeSuperCall_catchException()
      throws Exception {

    ClassLoader classLoader = processClasses(FakeViewGroupA.class, FakeViewGroupB.class);
    Class<?> cls = classLoader.loadClass(FakeViewGroupB.class.getName());
    SafeViewGroup instance =
        (SafeViewGroup)
            cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    setBooleanFieldValueForView(instance, "throwsExceptionABefore", true);
    instance.measure(1, 1);

    assertTrue(getBooleanFieldValueView(instance, "isMethodBBeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodBAfterException"));
    assertTrue(getBooleanFieldValueView(instance, "isMethodABeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodAAfterException"));
  }

  @Test
  public void fakeViewAOnMeasure_throwsExceptionAfterSuperCall_catchException() throws Exception {

    ClassLoader classLoader = processClasses(FakeViewGroupA.class, FakeViewGroupB.class);
    Class<?> cls = classLoader.loadClass(FakeViewGroupB.class.getName());
    SafeViewGroup instance =
        (SafeViewGroup)
            cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    setBooleanFieldValueForView(instance, "throwsExceptionAAfter", true);
    instance.measure(1, 1);

    assertTrue(getBooleanFieldValueView(instance, "isMethodBBeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodBAfterException"));
    assertTrue(getBooleanFieldValueView(instance, "isMethodABeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodAAfterException"));
  }

  @Test
  public void fakeViewGroupBOnMeasure_throwsExceptionBeforeSuperCall_catchException()
      throws Exception {

    ClassLoader classLoader = processClasses(FakeViewGroupA.class, FakeViewGroupB.class);
    Class<?> cls = classLoader.loadClass(FakeViewGroupB.class.getName());
    SafeViewGroup instance =
        (SafeViewGroup)
            cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    setBooleanFieldValueForView(instance, "throwsExceptionBBefore", true);
    instance.measure(1, 1);

    assertTrue(getBooleanFieldValueView(instance, "isMethodBBeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodBAfterException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodABeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodAAfterException"));
  }

  @Test
  public void fakeViewGroupBOnMeasure_throwsExceptionAfterSuperCall_catchException()
      throws Exception {

    ClassLoader classLoader = processClasses(FakeViewGroupA.class, FakeViewGroupB.class);
    Class<?> cls = classLoader.loadClass(FakeViewGroupB.class.getName());
    SafeViewGroup instance =
        (SafeViewGroup)
            cls.getConstructor(Context.class).newInstance(RuntimeEnvironment.application);

    setBooleanFieldValueForView(instance, "throwsExceptionBAfter", true);
    instance.measure(1, 1);

    assertTrue(getBooleanFieldValueView(instance, "isMethodBBeforeException"));
    assertFalse(getBooleanFieldValueView(instance, "isMethodBAfterException"));
    assertTrue(getBooleanFieldValueView(instance, "isMethodABeforeException"));
    assertTrue(getBooleanFieldValueView(instance, "isMethodAAfterException"));
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

  public static class FakeViewGroupA extends ViewGroup {

    public boolean isMethodABeforeException = false;
    public boolean isMethodAAfterException = false;

    public boolean throwsExceptionABefore = false;
    public boolean throwsExceptionAAfter = false;

    public FakeViewGroupA(Context context) {
      super(context);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      isMethodABeforeException = true;
      if (throwsExceptionABefore) {
        throw new RuntimeException("");
      }
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      if (throwsExceptionAAfter) {
        throw new RuntimeException("");
      }
      isMethodAAfterException = true;
    }
  }

  public static class FakeViewGroupB extends FakeViewGroupA {

    public boolean isMethodBBeforeException = false;
    public boolean isMethodBAfterException = false;

    public boolean throwsExceptionBBefore = false;
    public boolean throwsExceptionBAfter = false;

    public FakeViewGroupB(Context context) {
      super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      isMethodBBeforeException = true;
      if (throwsExceptionBBefore) {
        throw new RuntimeException("");
      }
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      if (throwsExceptionBAfter) {
        throw new RuntimeException("");
      }
      isMethodBAfterException = true;
    }
  }
}
