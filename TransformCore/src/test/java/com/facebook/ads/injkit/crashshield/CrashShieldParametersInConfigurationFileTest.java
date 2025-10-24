/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield;

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
public class CrashShieldParametersInConfigurationFileTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private static CrashShieldConfiguration parse(File config) throws Exception {
    return AnnotationProcessorParseTestUtils.parse(config, new CrashShieldConfigurationParser());
  }

  @Test
  public void exceptionHandlingDirectivesParsed() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true");

    CrashShieldConfiguration config = parse(configFile);

    assertThat(config.isEnabled()).isTrue();
    assertThat(config.getDisableAnnotationClass()).isEqualTo("a");
    assertThat(config.getEnableAnnotationClass()).isEqualTo("b");
    assertThat(config.getExceptionHandlerClass()).isEqualTo("c");
    assertThat(config.isShouldProcessConstructors()).isTrue();
    assertThat(config.isShouldProcessViews()).isTrue();
  }

  @Test
  public void exceptionHandlingDirectivesNotNeededIfDisabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder, CrashShieldConfigurationConstants.ENABLED + " false");

    CrashShieldConfiguration config = parse(configFile);

    assertThat(config.isEnabled()).isFalse();
    assertThat(config.getDisableAnnotationClass()).isNull();
    assertThat(config.getEnableAnnotationClass()).isNull();
    assertThat(config.getExceptionHandlerClass()).isNull();
    assertThat(config.isShouldProcessConstructors()).isFalse();
    assertThat(config.isShouldProcessViews()).isFalse();
  }

  @Test
  public void exceptionHandlingDisabledByDefault() throws Exception {
    File configFile = FileUtils.createConfigurationFile(temporaryFolder);

    CrashShieldConfiguration config = parse(configFile);

    assertThat(config.isEnabled()).isFalse();
    assertThat(config.getDisableAnnotationClass()).isNull();
    assertThat(config.getEnableAnnotationClass()).isNull();
    assertThat(config.getExceptionHandlerClass()).isNull();
    assertThat(config.isShouldProcessConstructors()).isFalse();
    assertThat(config.isShouldProcessViews()).isFalse();
  }

  @Test
  public void missingDoNotAutoHandleAnnotation() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS);
  }

  @Test
  public void duplicateDoNotAutoHandleAnnotation() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " d",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS);
  }

  @Test
  public void invalidDoNotAutoHandleAnnotation() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS);
  }

  @Test
  public void missingAutoHandleAnnotation() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS);
  }

  @Test
  public void duplicateAutoHandleAnnotation() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " d",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS);
  }

  @Test
  public void invalidAutoHandleAnnotation() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS);
  }

  @Test
  public void missingAutoHandler() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS);
  }

  @Test
  public void duplicateAutoHandler() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " d",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS);
  }

  @Test
  public void invalidAutoHandler() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS);
  }

  @Test
  public void duplicateExceptionHandlingEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.ENABLED);
  }

  @Test
  public void invalidExceptionHandlingEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.ENABLED);
  }

  @Test
  public void invalidBooleanInExceptionHandlingEnabled() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " xx",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining("xx");
  }

  @Test
  public void duplicateProcessConstructors() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " false");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR);
  }

  @Test
  public void invalidProcessConstructors() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " false true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR);
  }

  @Test
  public void invalidBooleanProcessConstructors() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " abc");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining("abc");
  }

  @Test
  public void duplicateProcessViews() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " false");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS);
  }

  @Test
  public void invalidProcessViews() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " true false",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining(CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS);
  }

  @Test
  public void invalidBooleanProcessViews() throws Exception {
    File configFile =
        FileUtils.createConfigurationFile(
            temporaryFolder,
            CrashShieldConfigurationConstants.ENABLED + " true",
            CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS + " a",
            CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS + " b",
            CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS + " c",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS + " ffff",
            CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR + " true");

    assertThatThrownBy(() -> parse(configFile))
        .isInstanceOf(InvalidAnnotationProcessorConfigurationException.class)
        .hasMessageContaining("ffff");
  }
}
