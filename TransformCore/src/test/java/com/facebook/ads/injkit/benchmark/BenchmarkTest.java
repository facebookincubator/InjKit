// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.benchmark;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

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

    assertThat(DummyBenchmarkReport.called).containsExactly("executed");
    assertThat(DummyBenchmarkReport.owner).containsExactly(TestClass.class.getName());
    assertThat(DummyBenchmarkReport.methodName).containsExactly("myMethod");
    assertThat(DummyBenchmarkReport.methodDesc).containsExactly("(I)I");
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

    assertThat(DummyBenchmarkReport.called).containsExactly("executedWithWarning");
    assertThat(DummyBenchmarkReport.owner).containsExactly(TestClass.class.getName());
    assertThat(DummyBenchmarkReport.methodName).containsExactly("slowMethod");
    assertThat(DummyBenchmarkReport.methodDesc).containsExactly("(I)V");
    assertThat(DummyBenchmarkReport.timeNanos).hasSize(1);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isAtLeast(10_000_000L);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isLessThan(30_000_000L);
    assertThat(DummyBenchmarkReport.limitNanos).containsExactly(10_000_000L);
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

    assertThat(DummyBenchmarkReport.called).containsExactly("failed");
    assertThat(DummyBenchmarkReport.owner).containsExactly(TestClass.class.getName());
    assertThat(DummyBenchmarkReport.methodName).containsExactly("slowMethod");
    assertThat(DummyBenchmarkReport.methodDesc).containsExactly("(I)V");
    assertThat(DummyBenchmarkReport.timeNanos).hasSize(1);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isAtLeast(40_000_000L);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isLessThan(50_000_000L);
    assertThat(DummyBenchmarkReport.limitNanos).containsExactly(30_000_000L);
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

    try {
      cls.methodThatThrowsException();
      fail();
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("boom");
    }

    assertThat(DummyBenchmarkReport.called).containsExactly("thrown");
    assertThat(DummyBenchmarkReport.owner).containsExactly(TestClass.class.getName());
    assertThat(DummyBenchmarkReport.methodName).containsExactly("methodThatThrowsException");
    assertThat(DummyBenchmarkReport.methodDesc).containsExactly("()V");
    assertThat(DummyBenchmarkReport.timeNanos).hasSize(1);
    assertThat(DummyBenchmarkReport.timeNanos.get(0)).isAtLeast(1L);
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
