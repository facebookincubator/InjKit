// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.threadcheck;

import com.facebook.ads.injkit.TransformationEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.google.common.truth.Truth.assertThat;

public class ThreadCheckAnnotationsAreRemovedTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static Object annotation(
            ClassLoader ldr,
            String method,
            Class<? extends Annotation> annotation)
            throws Exception {
        return ldr.loadClass(TestClass.class.getName()).getMethod(method).getAnnotation(annotation);
    }

    private static Object classAnnotation(
            ClassLoader ldr,
            Class<?> cls,
            Class<? extends Annotation> annotation)
            throws Exception {
        return ldr.loadClass(cls.getName()).getAnnotation(annotation);
    }

    @Before
    public void before() {
        DummyThreadCheckViolationHandler.reset();
    }

    @Test
    public void annotationsAreRemovedIfEnabled() throws Exception {
        ClassLoader ldr =
                new TransformationEnvironment(temporaryFolder)
                        .addProcessingClass(TestClass.class)
                        .addProcessingClass(UiClass.class)
                        .addProcessingClass(WorkerClass.class)
                        .addProcessingClass(AnyClass.class)
                        .newLoadableConfigurationWriter()
                        .enable(new ThreadCheckConfigurationWriter.Factory<>())
                        .uiThreadAnnotationClass(Ui.class)
                        .workerThreadAnnotationClass(Worker.class)
                        .anyThreadAnnotationClass(Any.class)
                        .violationHandlerClass(DummyThreadCheckViolationHandler.class)
                        .done()
                        .transformAndLoad();

        assertThat(annotation(ldr, "uiMethod", Ui.class)).isNull();
        assertThat(annotation(ldr, "workerMethod", Worker.class)).isNull();
        assertThat(annotation(ldr, "anyMethod", Any.class)).isNull();
        assertThat(classAnnotation(ldr, UiClass.class, Ui.class)).isNull();
        assertThat(classAnnotation(ldr, WorkerClass.class, Worker.class)).isNull();
        assertThat(classAnnotation(ldr, AnyClass.class, Any.class)).isNull();
    }

    @Test
    public void annotationsAreRemovedIfDisabled() throws Exception {
        ClassLoader ldr =
                new TransformationEnvironment(temporaryFolder)
                        .addProcessingClass(TestClass.class)
                        .addProcessingClass(UiClass.class)
                        .addProcessingClass(WorkerClass.class)
                        .addProcessingClass(AnyClass.class)
                        .newLoadableConfigurationWriter()
                        .enable(new ThreadCheckConfigurationWriter.Factory<>())
                        .disableThreadCheck(true)
                        .uiThreadAnnotationClass(Ui.class)
                        .workerThreadAnnotationClass(Worker.class)
                        .anyThreadAnnotationClass(Any.class)
                        .done()
                        .transformAndLoad();

        assertThat(annotation(ldr, "uiMethod", Ui.class)).isNull();
        assertThat(annotation(ldr, "workerMethod", Worker.class)).isNull();
        assertThat(annotation(ldr, "anyMethod", Any.class)).isNull();
        assertThat(classAnnotation(ldr, UiClass.class, Ui.class)).isNull();
        assertThat(classAnnotation(ldr, WorkerClass.class, Worker.class)).isNull();
        assertThat(classAnnotation(ldr, AnyClass.class, Any.class)).isNull();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Ui {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface Worker {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface Any {}

    public static class TestClass {
        @Ui
        public void uiMethod() {}

        @Worker
        public void workerMethod() {}

        @Any
        public void anyMethod() {}
    }

    @Ui
    public static class UiClass {}

    @Worker
    public static class WorkerClass {}

    @Any
    public static class AnyClass {}
}
