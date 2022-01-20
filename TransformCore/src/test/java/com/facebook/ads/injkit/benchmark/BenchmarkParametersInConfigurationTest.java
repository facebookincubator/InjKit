// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.benchmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.facebook.ads.injkit.AnnotationProcessorParseTestUtils;
import com.facebook.ads.injkit.FileUtils;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BenchmarkParametersInConfigurationTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private static BenchmarkConfiguration parse(File config) throws Exception {
    return AnnotationProcessorParseTestUtils.parse(config, new BenchmarkConfigurationParser());
  }

  @Test
  public void benchmarkParametersNotNeededIfBenchmarkNotEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder, BenchmarkConfigurationConstants.ENABLED + " false");

    BenchmarkConfiguration cfg = parse(configFile);

    assertThat(cfg.isBenchmarkEnabled()).isFalse();
    assertThat(cfg.getBenchmarkAnnotationClass()).isNull();
    assertThat(cfg.getBenchmarkReceiverClass()).isNull();
  }

  @Test
  public void benchmarkParametersObtainedIfBenchmarkEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ENABLED + " true",
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k",
            BenchmarkConfigurationConstants.RECEIVER_CLASS + " l");

    BenchmarkConfiguration cfg = parse(configFile);

    assertThat(cfg.isBenchmarkEnabled()).isTrue();
    assertThat(cfg.getBenchmarkAnnotationClass()).isEqualTo("k");
    assertThat(cfg.getBenchmarkReceiverClass()).isEqualTo("l");
  }

  @Test
  public void benchmarkEnabledNotSetMeansNotEnabled() throws Exception {
    File configFile = FileUtils.createConfigurationFile(temporaryFolder);

    BenchmarkConfiguration cfg = parse(configFile);

    assertThat(cfg.isBenchmarkEnabled()).isFalse();
    assertThat(cfg.getBenchmarkAnnotationClass()).isNull();
    assertThat(cfg.getBenchmarkReceiverClass()).isNull();
  }

  @Test
  public void duplicateBenchmarkEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k",
            BenchmarkConfigurationConstants.RECEIVER_CLASS + " l",
            BenchmarkConfigurationConstants.ENABLED + " true",
            BenchmarkConfigurationConstants.ENABLED + " true");

    try {
      parse(configFile);
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(BenchmarkConfigurationConstants.ENABLED);
    }
  }

  @Test
  public void invalidBenchmarkEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k",
            BenchmarkConfigurationConstants.ENABLED + " foo foo");

    try {
      parse(configFile);
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(BenchmarkConfigurationConstants.ENABLED);
    }
  }

  @Test
  public void missingBenchmarkAnnotationClass() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ENABLED + " true",
            BenchmarkConfigurationConstants.RECEIVER_CLASS + " l");

    try {
      parse(configFile);
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(BenchmarkConfigurationConstants.ANNOTATION_CLASS);
    }
  }

  @Test
  public void duplicateBenchmarkAnnotationClass() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ENABLED + " true",
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k",
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k",
            BenchmarkConfigurationConstants.RECEIVER_CLASS + " l");

    try {
      parse(configFile);
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(BenchmarkConfigurationConstants.ANNOTATION_CLASS);
    }
  }

  @Test
  public void invalidBenchmarkAnnotationClass() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ENABLED + " true",
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k k",
            BenchmarkConfigurationConstants.RECEIVER_CLASS + " l");

    try {
      parse(configFile);
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(BenchmarkConfigurationConstants.ANNOTATION_CLASS);
    }
  }

  @Test
  public void missingBenchmarkReceiverClass() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ENABLED + " true",
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k");

    try {
      parse(configFile);
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(BenchmarkConfigurationConstants.RECEIVER_CLASS);
    }
  }

  @Test
  public void duplicateBenchmarkReceiverClass() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ENABLED + " true",
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k",
            BenchmarkConfigurationConstants.RECEIVER_CLASS + " l",
            BenchmarkConfigurationConstants.RECEIVER_CLASS + " l");

    try {
      parse(configFile);
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(BenchmarkConfigurationConstants.RECEIVER_CLASS);
    }
  }

  @Test
  public void invalidBenchmarkReceiverClass() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            BenchmarkConfigurationConstants.ENABLED + " true",
            BenchmarkConfigurationConstants.ANNOTATION_CLASS + " k",
            BenchmarkConfigurationConstants.RECEIVER_CLASS + " l l");

    try {
      parse(configFile);
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(BenchmarkConfigurationConstants.RECEIVER_CLASS);
    }
  }
}
