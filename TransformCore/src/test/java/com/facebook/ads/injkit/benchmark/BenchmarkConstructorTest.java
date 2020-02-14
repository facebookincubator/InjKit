// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.benchmark;

import com.facebook.ads.injkit.TransformationEnvironment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class BenchmarkConstructorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Parameterized.Parameter(0)
    public Class<?> testClass;

    @Parameterized.Parameter(1)
    public Class<?>[] constructorParamTypes;

    @Parameterized.Parameter(2)
    public Object[] constructorParams;

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { TestClass1.class, new Class<?>[0], new Object[0] },
                { TestClass2.class, new Class<?>[] { int.class }, new Object[] { 15 } },
                { TestClass3.class, new Class<?>[0], new Object[0] },
                { TestClass4.class, new Class<?>[0], new Object[0] },
                { TestClass5.class, new Class<?>[0], new Object[0] },
                { TestClass6.class, new Class<?>[] { int.class }, new Object[] { 15 } },
        });
    }

    @Test
    public void constructorIsBenchmarked() throws Exception {
        DummyBenchmarkReport.reset();

        new TransformationEnvironment(temporaryFolder)
                .addProcessingClass(testClass)
                .addProcessingClass(BenchmarkThis.class)
                .newLoadableConfigurationWriter()
                .enable(new BenchmarkConfigurationWriter.Factory<>())
                .benchmarkReceiver(DummyBenchmarkReport.class)
                .benchmarkAnnotation(BenchmarkThis.class)
                .done()
                .transformAndLoad()
                .loadClass(testClass.getName())
                .getConstructor(constructorParamTypes)
                .newInstance(constructorParams);

        assertThat(DummyBenchmarkReport.called).containsExactly("executed");
    }

    public static class TestClass1 {
        @BenchmarkThis
        public TestClass1() {}
    }

    public static class TestClass2 {
        @BenchmarkThis
        public TestClass2(int x) {if (x != 15) throw new AssertionError(); }
    }

    public static class TestClass3 {
        private int foo = 15;

        @BenchmarkThis
        public TestClass3() { if (foo != 15) throw new AssertionError(); }
    }

    public static class TestClass4 {
        private final int foo;

        @BenchmarkThis
        public TestClass4() { foo = 15; }
    }

    public static class TestClass5 extends TestClass2 {

        @BenchmarkThis
        public TestClass5() {
            super(15);
        }
    }

    public static class TestClass6 extends TestClass2 {

        @BenchmarkThis
        public TestClass6(int i) {
            super(i);
        }
    }
}
