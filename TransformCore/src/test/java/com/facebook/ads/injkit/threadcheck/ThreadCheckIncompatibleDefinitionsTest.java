// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.threadcheck;

import com.facebook.ads.injkit.AnnotationProcessingException;
import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.TransformationEnvironment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class ThreadCheckIncompatibleDefinitionsTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Parameterized.Parameter(0)
    public Class<?> testClass0;

    @Parameterized.Parameter(1)
    public Class<?> testClass1;

    @Parameterized.Parameter(2)
    public Class<?> testClass2;

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { UiInterface.class, WorkFromUi.class, null },
                { UiInterface.class, AnyFromUi.class, null },
                { WorkInterface.class, UiFromWork.class, null },
                { WorkInterface.class, AnyFromWork.class, null },
                { AnyInterface.class, UiFromAny.class, null },
                { AnyInterface.class, WorkFromAny.class, null },
                { UiInterface.class, WorkInterface.class, FromUiAndWork.class },
                { UiInterface.class, AnyInterface.class, FromUiAndAny.class },
                { WorkInterface.class, AnyInterface.class, FromWorkAndAny.class },
                { UiInterfaceM.class, WorkFromUiM.class, null },
                { UiInterfaceM.class, AnyFromUiM.class, null },
                { WorkInterfaceM.class, UiFromWorkM.class, null },
                { WorkInterfaceM.class, AnyFromWorkM.class, null },
                { AnyInterfaceM.class, UiFromAnyM.class, null },
                { AnyInterfaceM.class, WorkFromAnyM.class, null },
                { UiInterfaceM.class, WorkInterfaceM.class, FromUiAndWorkM.class },
                { UiInterfaceM.class, AnyInterfaceM.class, FromUiAndAnyM.class },
                { WorkInterfaceM.class, AnyInterfaceM.class, FromWorkAndAnyM.class },
        });
    }

    @Test
    public void test() throws Exception {
        try {
            new TransformationEnvironment(temporaryFolder)
                    .addProcessingClass(testClass0)
                    .addProcessingClass(testClass1)
                    .addProcessingClass(testClass2)
                    .addProcessingClass(DummyThreadCheckViolationHandler.class)
                    .newLoadableConfigurationWriter()
                    .enable(new ThreadCheckConfigurationWriter.Factory<>())
                    .uiThreadAnnotationClass(Ui.class)
                    .workerThreadAnnotationClass(Work.class)
                    .anyThreadAnnotationClass(Any.class)
                    .violationHandlerClass(DummyThreadCheckViolationHandler.class)
                    .done()
                    .transformAndLoad();
            fail();
        } catch (AnnotationProcessingException e) {
            StringBuilder all = new StringBuilder();
            for (Throwable t = e; t != null; t = t.getCause()) {
                all.append("[");
                all.append(t.getMessage());
                all.append("]");
            }

            String allText = all.toString();

            assertThat(allText).contains("Inconsistent annotations in class hierarchy");
            if (testClass0 != null) {
                assertThat(allText)
                        .contains(AsmNameUtils.classJavaNameToInternalName(testClass0.getName()));
            }

            if (testClass1 != null) {
                assertThat(allText)
                        .contains(AsmNameUtils.classJavaNameToInternalName(testClass1.getName()));
            }

            if (testClass2 != null) {
                assertThat(allText)
                        .contains(AsmNameUtils.classJavaNameToInternalName(testClass2.getName()));
            }
        }
    }

    @Retention(RetentionPolicy.CLASS)
    @interface Ui {}

    @Retention(RetentionPolicy.CLASS)
    @interface Work {}

    @Retention(RetentionPolicy.CLASS)
    @interface Any {}

    @Ui interface UiInterface {}
    @Work public static class WorkFromUi implements UiInterface {}
    @Any public static class AnyFromUi implements UiInterface {}

    @Work interface WorkInterface {}
    @Ui public static class UiFromWork implements WorkInterface {}
    @Any public static class AnyFromWork implements WorkInterface {}

    @Any interface AnyInterface {}
    @Ui public static class UiFromAny implements AnyInterface {}
    @Work public static class WorkFromAny implements AnyInterface {}

    public static class FromUiAndWork implements UiInterface, WorkInterface {}
    public static class FromUiAndAny implements UiInterface, AnyInterface {}
    public static class FromWorkAndAny implements WorkInterface, AnyInterface {}

    interface UiInterfaceM {
        @Ui void foo();
    }

    public static class WorkFromUiM implements UiInterfaceM {
        @Override @Work public void foo() {}
    }

    public static class AnyFromUiM implements UiInterfaceM {
        @Override @Any public void foo() {}
    }

    interface WorkInterfaceM {
        @Work void foo();
    }

    public static class UiFromWorkM implements WorkInterfaceM {
        @Override @Ui public void foo() {}
    }

    public static class AnyFromWorkM implements WorkInterfaceM {
        @Override @Any public void foo() {}
    }

    interface AnyInterfaceM {
        @Any void foo();
    }

    public static class UiFromAnyM implements AnyInterfaceM {
        @Override @Ui public void foo() {}
    }

    public static class WorkFromAnyM implements AnyInterfaceM {
        @Override @Work public void foo() {}
    }

    public static class FromUiAndWorkM implements UiInterfaceM, WorkInterfaceM {
        @Override public void foo() {}
    }

    public static class FromUiAndAnyM implements UiInterfaceM, AnyInterfaceM {
        @Override public void foo() {}
    }

    public static class FromWorkAndAnyM implements WorkInterfaceM, AnyInterfaceM {
        @Override public void foo() {}
    }
}
