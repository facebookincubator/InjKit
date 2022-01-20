// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClassFilesTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private TransformationEnvironment environment;

  @Before
  public void before() throws IOException {
    environment = new TransformationEnvironment(temporaryFolder);
  }

  @Test
  public void classIsNotTouched() throws Exception {
    environment.addProcessingClass(TestClass.class);
    ClassLoader result = environment.newLoadableConfigurationWriter().transformAndLoad();
    int valueReturned = (Integer) environment.invoke(result, TestClass.class, "getValue");
    assertThat(valueReturned).isEqualTo(3);
  }

  public static class TestClass {
    public int getValue() {
      return 3;
    }
  }
}
