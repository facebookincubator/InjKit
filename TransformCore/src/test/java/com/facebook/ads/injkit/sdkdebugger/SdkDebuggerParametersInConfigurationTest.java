/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.sdkdebugger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
public class SdkDebuggerParametersInConfigurationTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private static SdkDebuggerConfiguration parse(File config) throws Exception {
    return AnnotationProcessorParseTestUtils.parse(config, new SdkDebuggerConfigurationParser());
  }

  @Test
  public void getParametersForCallLogging() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            SdkDebuggerConfigurationConstants.ENABLED + " true",
            SdkDebuggerConfigurationConstants.CALL_LOGGER + " a",
            SdkDebuggerConfigurationConstants.LOG_CALL_ANNOTATIONS + " b");

    SdkDebuggerConfiguration cfg = parse(configFile);

    assertThat(cfg.isEnabled()).isTrue();
    assertThat(cfg.getCallLoggerClass()).isEqualTo("a");
    assertThat(cfg.getLogCallAnnotationClass()).isEqualTo("b");
  }

  @Test
  public void parametersForCallLoggingOptionalIfNotEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder, SdkDebuggerConfigurationConstants.ENABLED + " false");

    SdkDebuggerConfiguration cfg = parse(configFile);

    assertThat(cfg.isEnabled()).isFalse();
    assertThat(cfg.getCallLoggerClass()).isNull();
    assertThat(cfg.getLogCallAnnotationClass()).isNull();
  }

  @Test
  public void callLoggingNotEnabledIfNotDefined() throws Exception {
    File configFile = FileUtils.createConfigurationFile(temporaryFolder);

    SdkDebuggerConfiguration cfg = parse(configFile);

    assertThat(cfg.isEnabled()).isFalse();
    assertThat(cfg.getCallLoggerClass()).isNull();
    assertThat(cfg.getLogCallAnnotationClass()).isNull();
  }

  @Test
  public void invalidCallLoggingValue() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            SdkDebuggerConfigurationConstants.ENABLED + " maybe",
            SdkDebuggerConfigurationConstants.CALL_LOGGER + " a",
            SdkDebuggerConfigurationConstants.LOG_CALL_ANNOTATIONS + " b");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining("maybe");
  }

  @Test
  public void emptyCallLogger() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            SdkDebuggerConfigurationConstants.ENABLED + " true",
            SdkDebuggerConfigurationConstants.CALL_LOGGER + "",
            SdkDebuggerConfigurationConstants.LOG_CALL_ANNOTATIONS + " b");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(SdkDebuggerConfigurationConstants.CALL_LOGGER);
  }

  @Test
  public void multipleLogCallAnnotationsValues() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            SdkDebuggerConfigurationConstants.ENABLED + " true",
            SdkDebuggerConfigurationConstants.CALL_LOGGER + " a",
            SdkDebuggerConfigurationConstants.LOG_CALL_ANNOTATIONS + " b p");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(SdkDebuggerConfigurationConstants.LOG_CALL_ANNOTATIONS);
  }
}
