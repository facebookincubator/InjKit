/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.threadcheck;

import static com.facebook.ads.injkit.threadcheck.ThreadCheckConfigurationConstants.ANY_THREAD_ANNOTATION_CLASS;
import static com.facebook.ads.injkit.threadcheck.ThreadCheckConfigurationConstants.ENABLED;
import static com.facebook.ads.injkit.threadcheck.ThreadCheckConfigurationConstants.UI_THREAD_ANNOTATION_CLASS;
import static com.facebook.ads.injkit.threadcheck.ThreadCheckConfigurationConstants.VIOLATION_HANDLER_CLASS;
import static com.facebook.ads.injkit.threadcheck.ThreadCheckConfigurationConstants.WORKER_THREAD_ANNOTATION_CLASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.facebook.ads.injkit.AnnotationProcessorParseTestUtils;
import com.facebook.ads.injkit.FileUtils;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ThreadCheckIncorrectParametersInConfigurationTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameter(0)
  public String enabledValue;

  @Parameterized.Parameter(1)
  public String uiThreadAnnotationClassValue;

  @Parameterized.Parameter(2)
  public String workerThreadAnnotationClassValue;

  @Parameterized.Parameter(3)
  public String anyThreadAnnotationClassValue;

  @Parameterized.Parameter(4)
  public String violationHandlerClassValue;

  @Parameterized.Parameter(5)
  public String extra;

  @Parameterized.Parameter(6)
  public String expectedMessage;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          // Invalid value tests:
          {"foo", "a", "b", "c", "d", null, "foo"},

          // Duplicate parameter tests:
          {"true", "a", "b", "c", "d", ENABLED + " true", ENABLED},
          {
            "true",
            "a",
            "b",
            "c",
            "d",
            UI_THREAD_ANNOTATION_CLASS + " x",
            UI_THREAD_ANNOTATION_CLASS
          },
          {
            "true",
            "a",
            "b",
            "c",
            "d",
            WORKER_THREAD_ANNOTATION_CLASS + " x",
            WORKER_THREAD_ANNOTATION_CLASS
          },
          {
            "true",
            "a",
            "b",
            "c",
            "d",
            ANY_THREAD_ANNOTATION_CLASS + " x",
            ANY_THREAD_ANNOTATION_CLASS
          },
          {"true", "a", "b", "c", "d", VIOLATION_HANDLER_CLASS + " x", VIOLATION_HANDLER_CLASS},

          // Missing parameter tests:
          {"true", null, "b", "c", "d", null, UI_THREAD_ANNOTATION_CLASS},
          {"true", "a", null, "c", "d", null, WORKER_THREAD_ANNOTATION_CLASS},
          {"true", "a", "b", null, "d", null, ANY_THREAD_ANNOTATION_CLASS},
          {"true", "a", "b", "c", null, null, VIOLATION_HANDLER_CLASS},
        });
  }

  private static String buildValue(String param, String value) {
    if (value == null) {
      return null;
    }

    return param + " " + value;
  }

  @Test
  public void test() throws Exception {
    File config =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            buildValue(ThreadCheckConfigurationConstants.ENABLED, enabledValue),
            buildValue(UI_THREAD_ANNOTATION_CLASS, uiThreadAnnotationClassValue),
            buildValue(WORKER_THREAD_ANNOTATION_CLASS, workerThreadAnnotationClassValue),
            buildValue(ANY_THREAD_ANNOTATION_CLASS, anyThreadAnnotationClassValue),
            buildValue(VIOLATION_HANDLER_CLASS, violationHandlerClassValue),
            extra);

    try {
      AnnotationProcessorParseTestUtils.parse(config, new ThreadCheckConfigurationParser());
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(expectedMessage);
    }
  }
}
