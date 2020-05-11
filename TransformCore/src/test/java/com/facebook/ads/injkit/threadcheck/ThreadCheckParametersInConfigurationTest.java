// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.threadcheck;

import static com.google.common.truth.Truth.assertThat;

import com.facebook.ads.injkit.AnnotationProcessorParseTestUtils;
import com.facebook.ads.injkit.FileUtils;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ThreadCheckParametersInConfigurationTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private static ThreadCheckConfiguration parse(File config) throws Exception {
    return AnnotationProcessorParseTestUtils.parse(config, new ThreadCheckConfigurationParser());
  }

  @Test
  public void parametersNotNeededIfNotEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder, ThreadCheckConfigurationConstants.ENABLED + " false");

    ThreadCheckConfiguration cfg = parse(configFile);

    assertThat(cfg.isEnabled()).isFalse();
    assertThat(cfg.getUiThreadAnnotationClass()).isNull();
    assertThat(cfg.getWorkerThreadAnnotationClass()).isNull();
    assertThat(cfg.getAnyThreadAnnotationClass()).isNull();
    assertThat(cfg.getViolationHandlerClass()).isNull();
  }

  @Test
  public void parametersCanBeProvidedIfNotEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            ThreadCheckConfigurationConstants.ENABLED + " false",
            ThreadCheckConfigurationConstants.UI_THREAD_ANNOTATION_CLASS + " a",
            ThreadCheckConfigurationConstants.WORKER_THREAD_ANNOTATION_CLASS + " b",
            ThreadCheckConfigurationConstants.ANY_THREAD_ANNOTATION_CLASS + " c",
            ThreadCheckConfigurationConstants.VIOLATION_HANDLER_CLASS + " d");

    ThreadCheckConfiguration cfg = parse(configFile);

    assertThat(cfg.isEnabled()).isFalse();
    assertThat(cfg.getUiThreadAnnotationClass()).isEqualTo("a");
    assertThat(cfg.getWorkerThreadAnnotationClass()).isEqualTo("b");
    assertThat(cfg.getAnyThreadAnnotationClass()).isEqualTo("c");
    assertThat(cfg.getViolationHandlerClass()).isEqualTo("d");
  }

  @Test
  public void parametersObtainedIfEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            ThreadCheckConfigurationConstants.ENABLED + " true",
            ThreadCheckConfigurationConstants.UI_THREAD_ANNOTATION_CLASS + " a",
            ThreadCheckConfigurationConstants.WORKER_THREAD_ANNOTATION_CLASS + " b",
            ThreadCheckConfigurationConstants.ANY_THREAD_ANNOTATION_CLASS + " c",
            ThreadCheckConfigurationConstants.VIOLATION_HANDLER_CLASS + " d");

    ThreadCheckConfiguration cfg = parse(configFile);

    assertThat(cfg.isEnabled()).isTrue();
    assertThat(cfg.getUiThreadAnnotationClass()).isEqualTo("a");
    assertThat(cfg.getWorkerThreadAnnotationClass()).isEqualTo("b");
    assertThat(cfg.getAnyThreadAnnotationClass()).isEqualTo("c");
    assertThat(cfg.getViolationHandlerClass()).isEqualTo("d");
  }

  @Test
  public void noParametersOkButNotEnabled() throws Exception {
    File configFile = FileUtils.createConfigurationFile(temporaryFolder);

    ThreadCheckConfiguration cfg = parse(configFile);

    assertThat(cfg.isEnabled()).isFalse();
    assertThat(cfg.getUiThreadAnnotationClass()).isNull();
    assertThat(cfg.getWorkerThreadAnnotationClass()).isNull();
    assertThat(cfg.getAnyThreadAnnotationClass()).isNull();
    assertThat(cfg.getViolationHandlerClass()).isNull();
  }
}
