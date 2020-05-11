// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.threadcheck;

import static com.google.common.truth.Truth.assertThat;

import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.TransformationEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ThreadCheckClinitTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void before() {
    DummyThreadCheckViolationHandler.reset();
  }

  @Test
  public void staticInitFails() throws Exception {
    ThreadCheckInjector.androidLooper =
        AsmNameUtils.classJavaNameToInternalName(CustomLooper.class.getName());

    CustomLooper.myself = new CustomLooper();
    CustomLooper.main = new CustomLooper();

    new TransformationEnvironment(temporaryFolder)
        .addProcessingClass(UiAlways.class)
        .newLoadableConfigurationWriter()
        .enable(new ThreadCheckConfigurationWriter.Factory<>())
        .uiThreadAnnotationClass(Ui.class)
        .workerThreadAnnotationClass(Work.class)
        .anyThreadAnnotationClass(Any.class)
        .violationHandlerClass(DummyThreadCheckViolationHandler.class)
        .done()
        .transformAndLoad()
        .loadClass(UiAlways.class.getName())
        .newInstance();

    assertThat(DummyThreadCheckViolationHandler.violationsDetected()).isEqualTo(0);
  }

  public @interface Ui {}

  public @interface Work {}

  public @interface Any {}

  @Ui
  public static class UiAlways {
    static {
      doSomethingInStatic();
    }

    @Any
    public UiAlways() {}

    @Any
    private static void doSomethingInStatic() {}
  }
}
