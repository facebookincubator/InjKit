// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.threadcheck;

import static com.google.common.truth.Truth.assertThat;

import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ThreadCheckerAbstractMethodsTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameter(0)
  public Class<?> cls1;

  @Parameterized.Parameter(1)
  public Class<?> cls2;

  @Parameterized.Parameter(2)
  public RunOn runOn;

  @Parameterized.Parameter(3)
  public boolean checkShouldFail;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {Intf.class, null, null, false},
          {AbsCls1.class, null, null, false},
          {Cls2.class, AbsCls2.class, RunOn.UI_THREAD, false},
          {Cls2.class, AbsCls2.class, RunOn.WORKER_THREAD, true},
        });
  }

  @Before
  public void before() {
    DummyThreadCheckViolationHandler.reset();
  }

  @Test
  public void test() throws Exception {
    ThreadCheckInjector.androidLooper =
        AsmNameUtils.classJavaNameToInternalName(CustomLooper.class.getName());

    Class<?> loaded =
        new TransformationEnvironment(temporaryFolder)
            .addProcessingClass(cls1)
            .addProcessingClass(cls2)
            .newLoadableConfigurationWriter()
            .enable(new ThreadCheckConfigurationWriter.Factory<>())
            .uiThreadAnnotationClass(Ui.class)
            .workerThreadAnnotationClass(Work.class)
            .anyThreadAnnotationClass(Any.class)
            .violationHandlerClass(DummyThreadCheckViolationHandler.class)
            .done()
            .transformAndLoad()
            .loadClass(cls1.getName());

    CustomLooper.main = new CustomLooper();
    if (runOn != null) {
      if (runOn == RunOn.UI_THREAD) {
        CustomLooper.myself = CustomLooper.main;
      } else {
        CustomLooper.myself = new CustomLooper();
      }

      loaded.getMethod("foo").invoke(loaded.newInstance());

      assertThat(DummyThreadCheckViolationHandler.violationsDetected())
          .isEqualTo(checkShouldFail ? 1 : 0);
    }
  }

  @Retention(RetentionPolicy.CLASS)
  @interface Ui {}

  @Retention(RetentionPolicy.CLASS)
  @interface Work {}

  @Retention(RetentionPolicy.CLASS)
  @interface Any {}

  interface Intf {
    @Ui
    void foo();
  }

  public abstract static class AbsCls1 {
    @Ui
    public abstract void foo();
  }

  public abstract static class AbsCls2 {
    @Ui
    public void foo() {}

    public abstract void bar();
  }

  public static class Cls2 extends AbsCls2 {
    @Override
    public void bar() {}
  }

  enum RunOn {
    UI_THREAD,
    WORKER_THREAD,
  }
}
