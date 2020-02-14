// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.threadcheck;

import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.TransformationEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class ThreadCheckInterfaceAnnotationInheritanceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private MarkerInterface intf;

    @Before
    public void before() throws Exception {
        ThreadCheckInjector.androidLooper =
                AsmNameUtils.classJavaNameToInternalName(CustomLooper.class.getName());

        intf =
                (MarkerInterface) new TransformationEnvironment(temporaryFolder)
                        .addProcessingClass(TestClass.class)
                        .addProcessingClass(InheritedInterface.class)
                        .newLoadableConfigurationWriter()
                        .enable(new ThreadCheckConfigurationWriter.Factory<>())
                        .anyThreadAnnotationClass(Any.class)
                        .uiThreadAnnotationClass(Ui.class)
                        .workerThreadAnnotationClass(Worker.class)
                        .violationHandlerClass(DummyThreadCheckViolationHandler.class)
                        .done()
                        .transformAndLoad()
                        .loadClass(TestClass.class.getName())
                        .newInstance();

        CustomLooper.main = new CustomLooper();

        DummyThreadCheckViolationHandler.reset();
    }

    @Test
    public void uiMethodCalledInUi() {
        CustomLooper.myself = CustomLooper.main;

        intf.uiMethod();

        assertThat(DummyThreadCheckViolationHandler.violationsDetected()).isEqualTo(0);
    }

    @Test
    public void uiMethodCalledInNonUi() {
        CustomLooper.myself = new CustomLooper();

        intf.uiMethod();

        assertThat(DummyThreadCheckViolationHandler.workerThreadViolationsDetected()).isEqualTo(0);
        assertThat(DummyThreadCheckViolationHandler.uiViolationsAre(
                TestClass.class,
                "uiMethod",
                "()V"))
                .isTrue();
    }

    @Test
    public void workerMethodCalledInBg() {
        CustomLooper.myself = new CustomLooper();

        intf.workerMethod();

        assertThat(DummyThreadCheckViolationHandler.violationsDetected()).isEqualTo(0);
    }

    @Test
    public void workerMethodCalledInUi() {
        CustomLooper.myself = CustomLooper.main;

        intf.workerMethod();

        assertThat(DummyThreadCheckViolationHandler.uiThreadViolationsDetected()).isEqualTo(0);
        assertThat(DummyThreadCheckViolationHandler.workerViolationsAre(
                TestClass.class,
                "workerMethod",
                "()V"))
                .isTrue();
    }

    @Test
    public void anyMethodCalledInBg() {
        CustomLooper.myself = new CustomLooper();

        intf.anyMethod();

        assertThat(DummyThreadCheckViolationHandler.violationsDetected()).isEqualTo(0);
    }

    @Test
    public void anyMethodCalledInUi() {
        CustomLooper.myself = CustomLooper.main;

        intf.anyMethod();

        assertThat(DummyThreadCheckViolationHandler.violationsDetected()).isEqualTo(0);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Ui {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface Worker {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface Any {}

    public interface MarkerInterface {
        void uiMethod();
        void workerMethod();
        void anyMethod();
    }

    @Ui
    public interface InheritedInterface extends MarkerInterface {
        void uiMethod();

        @Worker
        void workerMethod();

        @Any
        void anyMethod();
    }

    public static class TestClass implements InheritedInterface {
        @Any
        public TestClass() {}

        @Override
        public void uiMethod() {}

        @Override
        public void workerMethod() {}

        @Override
        public void anyMethod() {}
    }
}
