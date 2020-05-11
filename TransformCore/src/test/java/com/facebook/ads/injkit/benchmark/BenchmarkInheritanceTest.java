// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.benchmark;

import static com.google.common.truth.Truth.assertThat;

import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BenchmarkInheritanceTest {
  private static final int UNIT_MS = 10;
  private static final int PASS_DEFAULT_MS = 0;
  private static final int WARN_DEFAULT_MS = PASS_DEFAULT_MS + UNIT_MS;
  private static final int FAIL_DEFAULT_MS = WARN_DEFAULT_MS + UNIT_MS;
  private static final int PASS_BASE_MS = FAIL_DEFAULT_MS;
  private static final int WARN_BASE_MS = PASS_BASE_MS + UNIT_MS;
  private static final int FAIL_BASE_MS = WARN_BASE_MS + UNIT_MS;
  private static final int PASS_SUB_MS = FAIL_BASE_MS;
  private static final int WARN_SUB_MS = PASS_SUB_MS + UNIT_MS;
  private static final int FAIL_SUB_MS = WARN_SUB_MS + UNIT_MS;

  @Parameterized.Parameter(0)
  public String benchmarkedMethodName;

  @Parameterized.Parameter(1)
  public int methodDelay;

  @Parameterized.Parameter(2)
  public ExpectedOutput expectedOutput;

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {"benchmarkedMethod1", PASS_BASE_MS, ExpectedOutput.SUCCESS},
          {"benchmarkedMethod1", FAIL_BASE_MS, ExpectedOutput.WARNING},
          {"benchmarkedMethod2", PASS_DEFAULT_MS, ExpectedOutput.SUCCESS},
          {"benchmarkedMethod2", FAIL_DEFAULT_MS, ExpectedOutput.WARNING},
          {"benchmarkedMethod3", PASS_BASE_MS, ExpectedOutput.SUCCESS},
          {"benchmarkedMethod3", FAIL_BASE_MS, ExpectedOutput.WARNING},
          {"benchmarkedMethod4", PASS_DEFAULT_MS, ExpectedOutput.SUCCESS},
          {"benchmarkedMethod4", FAIL_DEFAULT_MS, ExpectedOutput.WARNING},
          {"benchmarkedMethod5", PASS_SUB_MS, ExpectedOutput.SUCCESS},
          {"benchmarkedMethod5", FAIL_SUB_MS, ExpectedOutput.WARNING},
          {"benchmarkedMethod6", PASS_SUB_MS, ExpectedOutput.SUCCESS},
          {"benchmarkedMethod6", FAIL_SUB_MS, ExpectedOutput.WARNING},
        });
  }

  @Before
  public void before() {
    DummyBenchmarkReport.reset();
  }

  @Test
  public void benchmarkTest() throws Exception {
    Class<?> subcls =
        new TransformationEnvironment(temporaryFolder)
            .addProcessingClass(BenchmarkAnn.class)
            .addProcessingClass(BaseClass.class)
            .addProcessingClass(SubClass.class)
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkAnn.class)
            .benchmarkReceiver(DummyBenchmarkReport.class)
            .done()
            .transformAndLoad()
            .loadClass(SubClass.class.getName());
    Method method = subcls.getMethod(benchmarkedMethodName, int.class);
    method.invoke(subcls.newInstance(), methodDelay);

    switch (expectedOutput) {
      case SUCCESS:
        assertThat(DummyBenchmarkReport.called).containsExactly("executed");
        break;
      case WARNING:
        assertThat(DummyBenchmarkReport.called).containsExactly("executedWithWarning");
        break;
      default:
        throw new AssertionError();
    }
  }

  @Retention(RetentionPolicy.CLASS)
  @interface BenchmarkAnn {
    int warnAtMillis() default WARN_DEFAULT_MS;

    int failAtMillis() default 10 * UNIT_MS;
  }

  static class BaseClass {
    static void doSleep(int sleepMs) {
      if (sleepMs > 0) {
        try {
          Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
          // We don't care.
        }
      }
    }

    @BenchmarkAnn(warnAtMillis = WARN_BASE_MS)
    public void benchmarkedMethod1(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn
    public void benchmarkedMethod2(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn(warnAtMillis = WARN_BASE_MS)
    public void benchmarkedMethod3(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn
    public void benchmarkedMethod4(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn(warnAtMillis = WARN_BASE_MS)
    public void benchmarkedMethod5(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn
    public void benchmarkedMethod6(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }
  }

  public static class SubClass extends BaseClass {
    public void benchmarkedMethod1(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    public void benchmarkedMethod2(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn
    public void benchmarkedMethod3(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn
    public void benchmarkedMethod4(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn(warnAtMillis = WARN_SUB_MS)
    public void benchmarkedMethod5(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }

    @BenchmarkAnn(warnAtMillis = WARN_SUB_MS)
    public void benchmarkedMethod6(int sleepMs) {
      BaseClass.doSleep(sleepMs);
    }
  }

  enum ExpectedOutput {
    SUCCESS,
    WARNING
  }
}
