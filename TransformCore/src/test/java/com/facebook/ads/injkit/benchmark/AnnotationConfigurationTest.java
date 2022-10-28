/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.benchmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.facebook.ads.injkit.AnnotationProcessingException;
import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AnnotationConfigurationTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameter(0)
  public Class<?> testClass;

  @Parameterized.Parameter(1)
  public Class<?> benchmarkClass;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {TestClass1.class, NoProperties.class},
          {TestClass2.class, NoWarnProperty.class},
          {TestClass3.class, NoFailProperty.class},
          {TestClass4.class, WarnPropertyHasWrongType.class},
          {TestClass5.class, FailPropertyHasWrongType.class},
        });
  }

  @Test
  public void runTest() throws Exception {
    try {
      new TransformationEnvironment(temporaryFolder)
          .addProcessingClass(testClass)
          .addProcessingClass(benchmarkClass)
          .newLoadableConfigurationWriter()
          .enable(new BenchmarkConfigurationWriter.Factory<>())
          .benchmarkAnnotation(benchmarkClass)
          .benchmarkReceiver(DummyBenchmarkReport.class)
          .done()
          .transformAndLoad();
      fail();
    } catch (AnnotationProcessingException e) {
      StringBuilder all = new StringBuilder();
      for (Throwable t = e; t != null; t = t.getCause()) {
        all.append('[');
        all.append(t.getMessage());
        all.append(']');
      }

      assertThat(all.toString())
          .contains(AsmNameUtils.classJavaNameToInternalName(benchmarkClass.getName()));
    }
  }

  @Retention(RetentionPolicy.CLASS)
  @interface NoProperties {}

  public static class TestClass1 {
    @NoProperties
    void foo() {}
  }

  @Retention(RetentionPolicy.CLASS)
  @interface NoWarnProperty {
    int failAtMillis() default 10;
  }

  public static class TestClass2 {
    @NoWarnProperty
    void foo() {}
  }

  @Retention(RetentionPolicy.CLASS)
  @interface NoFailProperty {
    int warnAtMillis() default 10;
  }

  public static class TestClass3 {
    @NoFailProperty
    void foo() {}
  }

  @Retention(RetentionPolicy.CLASS)
  @interface WarnPropertyHasWrongType {
    String warnAtMillis() default "foo";

    int failAtMillis() default 10;
  }

  public static class TestClass4 {
    @WarnPropertyHasWrongType
    void foo() {}
  }

  @Retention(RetentionPolicy.CLASS)
  @interface FailPropertyHasWrongType {
    int warnAtMillis() default 10;

    String failAtMillis() default "foo";
  }

  public static class TestClass5 {
    @FailPropertyHasWrongType
    void foo() {}
  }
}
