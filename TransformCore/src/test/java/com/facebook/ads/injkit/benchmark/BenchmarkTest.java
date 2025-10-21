/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.benchmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BenchmarkTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private TransformationEnvironment environment;

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolder);
    environment.addProcessingClass(BenchmarkThis.class);

    DummyBenchmarkReport.reset();
  }

  private Class<? extends Annotation> getBenchmarkThis(ClassLoader loader) throws Exception {
    @SuppressWarnings("unchecked")
    Class<? extends Annotation> cls =
        (Class<? extends Annotation>) loader.loadClass(BenchmarkThis.class.getName());
    return cls;
  }

  @Test
  public void benchmarkedMethodIsCalled() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader ldr =
        environment
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkThis.class)
            .benchmarkReceiver(DummyBenchmarkReport.class)
            .done()
            .transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    int r = cls.myMethod(5);

    assertThat(r).isEqualTo(6);
  }

  @Test
  public void executedIsCalledIfFast() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader ldr =
        environment
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkThis.class)
            .benchmarkReceiver(DummyBenchmarkReport.class)
            .done()
            .transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    cls.myMethod(5);

    assertThat(DummyBenchmarkReport.called).containsOnly("executed");
    assertThat(DummyBenchmarkReport.owner).containsOnly(TestClass.class.getName());
    assertThat(DummyBenchmarkReport.methodName).containsOnly("myMethod");
    assertThat(DummyBenchmarkReport.methodDesc).containsOnly("(I)I");
    assertThat(DummyBenchmarkReport.timeNanos).hasSize(1);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isLessThan(1_000_000L);
    assertThat(DummyBenchmarkReport.limitNanos).isEmpty();
  }

  @Test
  public void executedWithWarningIsCalledIfThreshold() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader ldr =
        environment
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkThis.class)
            .benchmarkReceiver(DummyBenchmarkReport.class)
            .done()
            .transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    cls.slowMethod(20);

    assertThat(DummyBenchmarkReport.called).containsOnly("executedWithWarning");
    assertThat(DummyBenchmarkReport.owner).containsOnly(TestClass.class.getName());
    assertThat(DummyBenchmarkReport.methodName).containsOnly("slowMethod");
    assertThat(DummyBenchmarkReport.methodDesc).containsOnly("(I)V");
    assertThat(DummyBenchmarkReport.timeNanos).hasSize(1);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isGreaterThanOrEqualTo(10_000_000L);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isLessThan(30_000_000L);
    assertThat(DummyBenchmarkReport.limitNanos).containsOnly(10_000_000L);
  }

  @Test
  public void failedIsCalledIfThreshold() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader ldr =
        environment
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkThis.class)
            .benchmarkReceiver(DummyBenchmarkReport.class)
            .done()
            .transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    cls.slowMethod(40);

    assertThat(DummyBenchmarkReport.called).containsOnly("failed");
    assertThat(DummyBenchmarkReport.owner).containsOnly(TestClass.class.getName());
    assertThat(DummyBenchmarkReport.methodName).containsOnly("slowMethod");
    assertThat(DummyBenchmarkReport.methodDesc).containsOnly("(I)V");
    assertThat(DummyBenchmarkReport.timeNanos).hasSize(1);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isGreaterThanOrEqualTo(40_000_000L);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isLessThan(50_000_000L);
    assertThat(DummyBenchmarkReport.limitNanos).containsOnly(30_000_000L);
  }

  @Test
  public void benchmarkAnnotationIsRemoved() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader ldr =
        environment
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkThis.class)
            .benchmarkReceiver(DummyBenchmarkReport.class)
            .done()
            .transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    Method slowMethod = cls.getClass().getMethod("slowMethod", int.class);
    Object found = slowMethod.getAnnotation(getBenchmarkThis(ldr));

    assertThat(found).isNull();
  }

  @Test
  public void benchmarkAnnotationIsRemovedEvenIfProcessingIsDisabled() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader ldr =
        environment
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkThis.class)
            .disableBenchmarks()
            .done()
            .transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    Method slowMethod = cls.getClass().getMethod("slowMethod", int.class);
    Object found = slowMethod.getAnnotation(getBenchmarkThis(ldr));

    assertThat(found).isNull();
  }

  @Test
  public void benchmarkAnnotationIsNotRemovedIfProcessingIsDisabledIfNotDefined() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader ldr = environment.newLoadableConfigurationWriter().transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    Method slowMethod = cls.getClass().getMethod("slowMethod", int.class);
    Object found = slowMethod.getAnnotation(getBenchmarkThis(ldr));

    assertThat(found).isNotNull();
  }

  @Test
  public void methodsThatFailWithExceptionAreBenchmarked() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader ldr =
        environment
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkAnnotation(BenchmarkThis.class)
            .benchmarkReceiver(DummyBenchmarkReport.class)
            .done()
            .transformAndLoad();
    CallTestClass cls = (CallTestClass) ldr.loadClass(TestClass.class.getName()).newInstance();

    assertThatThrownBy(() -> cls.methodThatThrowsException())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("boom");

    assertThat(DummyBenchmarkReport.called).containsOnly("thrown");
    assertThat(DummyBenchmarkReport.owner).containsOnly(TestClass.class.getName());
    assertThat(DummyBenchmarkReport.methodName).containsOnly("methodThatThrowsException");
    assertThat(DummyBenchmarkReport.methodDesc).containsOnly("()V");
    assertThat(DummyBenchmarkReport.timeNanos).hasSize(1);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isGreaterThanOrEqualTo(1L);
    assertThat(DummyBenchmarkReport.exceptions).hasSize(1);
    assertThat(DummyBenchmarkReport.exceptions.get(0)).isInstanceOf(RuntimeException.class);
    assertThat(DummyBenchmarkReport.exceptions.get(0).getMessage()).isEqualTo("boom");
  }

  public interface CallTestClass {
    int myMethod(int i);

    void slowMethod(int sleepMillis);

    void methodThatThrowsException();
  }

  public static class TestClass implements CallTestClass {
    @Override
    @BenchmarkThis
    public int myMethod(int i) {
      return i + 1;
    }

    @Override
    @BenchmarkThis(warnAtMillis = 10, failAtMillis = 30)
    public void slowMethod(int sleepMillis) {
      try {
        Thread.sleep(sleepMillis);
      } catch (InterruptedException e) {
        // Ignore.
      }
    }

    @Override
    @BenchmarkThis
    public void methodThatThrowsException() {
      throw new RuntimeException("boom");
    }
  }
}
