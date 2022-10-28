/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import com.facebook.ads.injkit.TransformationEnvironment;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BenchmarkAbstractMethodsTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameter(0)
  public Class<?> testClass;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {TestClass1.class}, {TestClass2.class},
        });
  }

  @Test
  public void benchmarkAbstractMethods() throws Exception {
    DummyBenchmarkReport.reset();

    Object obj =
        new TransformationEnvironment(temporaryFolder)
            .addProcessingClass(TestInterface.class)
            .addProcessingClass(AbstractClass.class)
            .addProcessingClass(testClass)
            .addProcessingClass(BenchmarkThis.class)
            .newLoadableConfigurationWriter()
            .enable(new BenchmarkConfigurationWriter.Factory<>())
            .benchmarkReceiver(DummyBenchmarkReport.class)
            .benchmarkAnnotation(BenchmarkThis.class)
            .done()
            .transformAndLoad()
            .loadClass(testClass.getName())
            .newInstance();
    obj.getClass().getMethod("foo").invoke(obj);

    assertThat(DummyBenchmarkReport.called).containsExactlyInAnyOrder("executed");
  }

  public interface TestInterface {
    @BenchmarkThis
    void foo();
  }

  public static class TestClass1 implements TestInterface {
    @Override
    public void foo() {}
  }

  public abstract static class AbstractClass {
    @BenchmarkThis
    public abstract void foo();
  }

  public static class TestClass2 extends AbstractClass {
    @Override
    public void foo() {}
  }
}
