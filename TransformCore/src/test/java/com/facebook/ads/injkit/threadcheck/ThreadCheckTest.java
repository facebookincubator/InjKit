// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.threadcheck;

import static org.assertj.core.api.Assertions.assertThat;

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
public class ThreadCheckTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameter(0)
  public boolean enabled;

  @Parameterized.Parameter(1)
  public Class<?> testClass;

  @Parameterized.Parameter(2)
  public CallerThread callerThread;

  @Parameterized.Parameter(3)
  public Call call;

  @Parameterized.Parameter(4)
  public boolean shouldFail;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          // Tests on methods without any class annotation.

          // Enabled: UI method called from UI is fine.
          {true, TestClass1.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          // Enabled: UI method called from non-UI fails.
          {true, TestClass1.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, true},
          // Enabled: Worker method called from UI fails.
          {true, TestClass1.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, true},
          // Enabled: Worker method called from non-UI is fine.
          {true, TestClass1.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},

          // Disabled: always fine.
          {false, TestClass1.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {false, TestClass1.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, false},
          {false, TestClass1.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass1.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},

          // Tests on methods when the class is UI by default (same as above).

          {true, TestClass2.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {true, TestClass2.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, true},
          {true, TestClass2.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, true},
          {true, TestClass2.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass2.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {false, TestClass2.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, false},
          {false, TestClass2.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass2.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},

          // Tests on methods when the class is worker by default (same as above).

          {true, TestClass3.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {true, TestClass3.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, true},
          {true, TestClass3.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, true},
          {true, TestClass3.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass3.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {false, TestClass3.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, false},
          {false, TestClass3.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass3.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},

          // Tests on methods when the class is any by default (same as above).

          {true, TestClass4.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {true, TestClass4.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, true},
          {true, TestClass4.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, true},
          {true, TestClass4.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass4.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {false, TestClass4.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, false},
          {false, TestClass4.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass4.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},

          // Test on methods when inherited from methods (should perform as first group).

          {true, TestClass5.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {true, TestClass5.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, true},
          {true, TestClass5.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, true},
          {true, TestClass5.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass5.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {false, TestClass5.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, false},
          {false, TestClass5.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass5.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},

          // Test on methods when inherited from methods and classes (should perform as second
          // group).

          {true, TestClass6.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {true, TestClass6.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, true},
          {true, TestClass6.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, true},
          {true, TestClass6.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass6.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {false, TestClass6.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, false},
          {false, TestClass6.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass6.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},

          // Test on methods when inherited from methods and classes (should perform as third
          // group).

          {true, TestClass7.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {true, TestClass7.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, true},
          {true, TestClass7.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, true},
          {true, TestClass7.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass7.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {false, TestClass7.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, false},
          {false, TestClass7.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass7.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},

          // Test on methods when inherited from methods and classes (should perform as fourth
          // group).

          {true, TestClass8.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {true, TestClass8.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, true},
          {true, TestClass8.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, true},
          {true, TestClass8.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass8.class, CallerThread.IS_MAIN, Call.UI_METHOD, false},
          {false, TestClass8.class, CallerThread.IS_NOT_MAIN, Call.UI_METHOD, false},
          {false, TestClass8.class, CallerThread.IS_MAIN, Call.WORKER_METHOD, false},
          {false, TestClass8.class, CallerThread.IS_NOT_MAIN, Call.WORKER_METHOD, false},
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
    ClassLoader ldr =
        new TransformationEnvironment(temporaryFolder)
            .addProcessingClass(TestClass1.class)
            .addProcessingClass(TestClass2.class)
            .addProcessingClass(TestClass3.class)
            .addProcessingClass(TestClass4.class)
            .addProcessingClass(TestClass5.class)
            .addProcessingClass(TestClass6.class)
            .addProcessingClass(TestClass7.class)
            .addProcessingClass(TestClass8.class)
            .newLoadableConfigurationWriter()
            .enable(new ThreadCheckConfigurationWriter.Factory<>())
            .disableThreadCheck(!enabled)
            .uiThreadAnnotationClass(Ui.class)
            .workerThreadAnnotationClass(Worker.class)
            .anyThreadAnnotationClass(Any.class)
            .violationHandlerClass(DummyThreadCheckViolationHandler.class)
            .done()
            .transformAndLoad();

    ToCall toCall;

    // Always call the constructor in both threads.
    if (callerThread == CallerThread.IS_MAIN) {
      CustomLooper.myself = new CustomLooper();
      CustomLooper.main = new CustomLooper();

      ldr.loadClass(testClass.getName()).newInstance();

      CustomLooper.main = CustomLooper.myself;

      toCall = (ToCall) ldr.loadClass(testClass.getName()).newInstance();
    } else {
      CustomLooper.myself = new CustomLooper();
      CustomLooper.main = CustomLooper.myself;

      ldr.loadClass(testClass.getName()).newInstance();

      CustomLooper.main = new CustomLooper();

      toCall = (ToCall) ldr.loadClass(testClass.getName()).newInstance();
    }

    if (call == Call.UI_METHOD) {
      toCall.uiMethod();
    } else {
      toCall.workerMethod();
    }

    assertThat(DummyThreadCheckViolationHandler.violationsDetected()).isEqualTo(shouldFail ? 1 : 0);
  }

  @Retention(RetentionPolicy.CLASS)
  @interface Ui {}

  @Retention(RetentionPolicy.CLASS)
  @interface Worker {}

  @Retention(RetentionPolicy.CLASS)
  @interface Any {}

  public interface ToCall {
    void uiMethod();

    void workerMethod();
  }

  public static class TestClass1 implements ToCall {
    @Any
    public TestClass1() {}

    @Override
    @Ui
    public void uiMethod() {}

    @Override
    @Worker
    public void workerMethod() {}
  }

  @Ui
  public static class TestClass2 implements ToCall {
    @Any
    public TestClass2() {}

    @Override
    public void uiMethod() {}

    @Override
    @Worker
    public void workerMethod() {}
  }

  @Worker
  public static class TestClass3 implements ToCall {
    @Any
    public TestClass3() {}

    @Override
    @Ui
    public void uiMethod() {}

    @Override
    public void workerMethod() {}
  }

  @Any
  public static class TestClass4 implements ToCall {
    public TestClass4() {}

    @Override
    @Ui
    public void uiMethod() {}

    @Override
    @Worker
    public void workerMethod() {}
  }

  public static class TestClass5 extends TestClass1 {
    public TestClass5() {}

    @Override
    public void uiMethod() {}

    @Override
    public void workerMethod() {}
  }

  public static class TestClass6 extends TestClass2 {
    @Any
    public TestClass6() {}

    @Override
    public void uiMethod() {}

    @Override
    public void workerMethod() {}
  }

  public static class TestClass7 extends TestClass3 {
    @Any
    public TestClass7() {}

    @Override
    public void uiMethod() {}

    @Override
    public void workerMethod() {}
  }

  public static class TestClass8 extends TestClass4 {
    @Any
    public TestClass8() {}

    @Override
    public void uiMethod() {}

    @Override
    public void workerMethod() {}
  }

  enum CallerThread {
    IS_MAIN,
    IS_NOT_MAIN
  }

  enum Call {
    UI_METHOD,
    WORKER_METHOD
  }
}
