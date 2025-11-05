/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.benchmark;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.facebook.ads.injkit.TransformationEnvironment;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BenchmarkReportCanThrow {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private TransformationEnvironment environment;

  @Parameterized.Parameter(0)
  public Integer sleepTime;

  @Parameterized.Parameter(1)
  public Exception throwException;

  @Parameterized.Parameter(2)
  public String expectedMessage;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {0, null, "Failed in executed"},
          {150, null, "Failed in executedWithWarning"},
          {250, null, "Failed in failed"},
          {0, new RuntimeException(), "Failed in thrown"},
        });
  }

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolder);

    DummyBenchmarkReport.reset();
  }

  @Test
  public void throwInExecuted() throws Exception {
    environment.addProcessingClass(TestClass.class);
    environment.addProcessingClass(BenchmarkThis.class);
    ClassLoader ldr =
        environment
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkThis.class)
            .benchmarkReceiver(Reporter.class)
            .done()
            .transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    assertThatThrownBy(() -> cls.callSleep(sleepTime, throwException))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(expectedMessage);
  }

  public interface CallTestClass {
    void callSleep(int ms, Exception t) throws Exception;
  }

  public static class TestClass implements CallTestClass {
    @Override
    @BenchmarkThis(warnAtMillis = 100, failAtMillis = 200)
    public void callSleep(int ms, Exception t) throws Exception {
      if (ms > 0) {
        Thread.sleep(ms);
      }

      if (t != null) {
        throw t;
      }
    }
  }

  public static class Reporter {
    public static void executed(String a, String b, String c, long d) {
      throw new RuntimeException("Failed in executed");
    }

    public static void executedWithWarning(String a, String b, String c, long d, long e) {
      throw new RuntimeException("Failed in executedWithWarning");
    }

    public static void failed(String a, String b, String c, long d, long e) {
      throw new RuntimeException("Failed in failed");
    }

    public static void thrown(String a, String b, String c, Throwable d, long e) {
      throw new RuntimeException("Failed in thrown");
    }
  }
}
