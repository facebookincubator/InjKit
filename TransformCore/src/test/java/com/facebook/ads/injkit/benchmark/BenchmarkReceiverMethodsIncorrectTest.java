// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.benchmark;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
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
public class BenchmarkReceiverMethodsIncorrectTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private TransformationEnvironment environment;

  @Parameterized.Parameter(0)
  public Class<?> reporterClass;

  @Parameterized.Parameter(1)
  public Object failureText;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {
            MissingExecutedMethod.class,
            "executed(java.lang.String, java.lang.String, java.lang.String, long)"
          },
          {
            IncorrectExecutedMethod.class,
            "executed(java.lang.String, java.lang.String, java.lang.String, long)"
          },
          {NonPublicExecutedMethod.class, new String[] {"executed", "public"}},
          {NonStaticExecutedMethod.class, new String[] {"executed", "static"}},
          {
            MissingExecutedWarningMethod.class,
            "executedWithWarning(java.lang.String, java.lang.String, java.lang.String, "
                + "long, long)"
          },
          {
            IncorrectExecutedWarningMethod.class,
            "executedWithWarning(java.lang.String, java.lang.String, java.lang.String, "
                + "long, long)"
          },
          {NonPublicExecutedWarningMethod.class, new String[] {"executedWithWarning", "public"}},
          {NonStaticExecutedWarningMethod.class, new String[] {"executedWithWarning", "static"}},
          {
            MissingFailedMethod.class,
            "failed(java.lang.String, java.lang.String, java.lang.String, long, long)"
          },
          {
            IncorrectFailedMethod.class,
            "failed(java.lang.String, java.lang.String, java.lang.String, long, long)"
          },
          {NonPublicFailedMethod.class, new String[] {"failed", "public"}},
          {NonStaticFailedMethod.class, new String[] {"failed", "static"}},
          {
            MissingThrownMethod.class,
            "thrown(java.lang.String, java.lang.String, java.lang.String, "
                + "java.lang.Throwable, long)"
          },
          {
            IncorrectThrownMethod.class,
            "thrown(java.lang.String, java.lang.String, java.lang.String, "
                + "java.lang.Throwable, long)"
          },
          {NonPublicThrownMethod.class, new String[] {"thrown", "public"}},
          {NonStaticThrownMethod.class, new String[] {"thrown", "static"}},
        });
  }

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolder);

    DummyBenchmarkReport.reset();
  }

  @Test
  public void missingExecutedMethod() throws Exception {
    environment.addProcessingClass(reporterClass);

    try {
      environment
          .newLoadableConfigurationWriter()
          .enable(new BenchmarkConfigurationWriter.Factory<>())
          .benchmarkAnnotation(BenchmarkThis.class)
          .benchmarkReceiver(reporterClass)
          .done()
          .transformAndLoad();
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e.getMessage()).contains(reporterClass.getName());
      if (failureText instanceof String) {
        assertThat(e.getMessage()).contains((String) failureText);
      } else {
        for (String failure : (String[]) failureText) {
          assertThat(e.getMessage()).contains(failure);
        }
      }
    }
  }

  public static class MissingExecutedMethod {
    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class IncorrectExecutedMethod {
    public static void executed(String a, String b, String c) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class NonPublicExecutedMethod {
    static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class NonStaticExecutedMethod {
    public void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class MissingExecutedWarningMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class IncorrectExecutedWarningMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class NonPublicExecutedWarningMethod {
    public static void executed(String a, String b, String c, long d) {}

    static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class NonStaticExecutedWarningMethod {
    public static void executed(String a, String b, String c, long d) {}

    public void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class MissingFailedMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class IncorrectFailedMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class NonPublicFailedMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class NonStaticFailedMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class MissingThrownMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}
  }

  public static class IncorrectThrownMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public static void thrown(String a, String b, Throwable d, long e) {}
  }

  public static class NonPublicThrownMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    static void thrown(String a, String b, String c, Throwable d, long e) {}
  }

  public static class NonStaticThrownMethod {
    public static void executed(String a, String b, String c, long d) {}

    public static void executedWithWarning(String a, String b, String c, long d, long e) {}

    public static void failed(String a, String b, String c, long d, long e) {}

    public void thrown(String a, String b, String c, Throwable d, long e) {}
  }
}
