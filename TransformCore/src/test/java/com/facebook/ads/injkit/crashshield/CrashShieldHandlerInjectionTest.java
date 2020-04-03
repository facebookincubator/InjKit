// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.crashshield;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.facebook.ads.injkit.TransformationEnvironment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@FakeHandleExceptionsAnnotation
@RunWith(RobolectricTestRunner.class)
@SuppressLint("CatchGeneralException")
public class CrashShieldHandlerInjectionTest {

    @Rule
    public TemporaryFolder temporaryFolderRule = new TemporaryFolder();

    private TransformationEnvironment environment;

    @Before
    public void before() throws Exception {
        environment = new TransformationEnvironment(temporaryFolderRule);
    }

    @Test
    public void handle_throwsException_shouldCatchException() throws Exception {
        ClassLoader classLoader = processClasses(FakeHandler.class);
        Class<?> cls = classLoader.loadClass(FakeHandler.class.getName());

        Handler instance = (Handler) cls.getConstructor().newInstance();

        try {
            instance.handleMessage(Message.obtain());
        } catch (Throwable t) {
            fail("Should catch exception.");
        }

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

    public static boolean getBooleanFieldValue(Object instance, String name) {
        try {
            Field field = instance.getClass().getField(name);
            return (Boolean) field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("ClownyBooleanExpression")
    public static class FakeHandler extends Handler {

        public boolean isBeforeException = false;
        public boolean isAfterException = false;

        @Override
        public void handleMessage(Message msg) {
            isBeforeException = true;
            if (true) {
                throw new RuntimeException("handle() exception");
            }
            isAfterException = true;
        }
    }

}
