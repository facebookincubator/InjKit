// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.crashshield;

import static org.assertj.core.api.Assertions.assertThat;

import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AnnotationDefinitionTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private TransformationEnvironment environment;

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolder);
    environment.addProcessingClass(TestClass.class);
  }

  @Test
  public void annotationWithoutRetentionFails() throws Exception {
    try {
      environment
          .newLoadableConfigurationWriter()
          .enable(new CrashShieldConfigurationWriter.Factory<>())
          .handler(FakeExceptionHandler.class)
          .transformAnnotation(AnnotationWithoutRetention.class)
          .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
          .done()
          .transformAndLoad();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(AnnotationWithoutRetention.class.getName());
      assertThat(e).hasMessageContaining(Retention.class.getSimpleName());
      assertThat(e).hasMessageContaining(RetentionPolicy.CLASS.name());
    }
  }

  @Test
  public void annotationWithSourceRetentionFails() throws Exception {
    try {
      environment
          .newLoadableConfigurationWriter()
          .enable(new CrashShieldConfigurationWriter.Factory<>())
          .handler(FakeExceptionHandler.class)
          .transformAnnotation(AnnotationWithSourceRetention.class)
          .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
          .done()
          .transformAndLoad();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining(AnnotationWithSourceRetention.class.getName());
      assertThat(e).hasMessageContaining(Retention.class.getSimpleName());
      assertThat(e).hasMessageContaining(RetentionPolicy.CLASS.name());
    }
  }

  @Test
  public void annotationWithClassRetentionOk() throws Exception {
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .handler(FakeExceptionHandler.class)
        .transformAnnotation(AnnotationWithClassRetention.class)
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void annotationWithRuntimeRetentionOk() throws Exception {
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .handler(FakeExceptionHandler.class)
        .transformAnnotation(AnnotationWithRuntimeRetention.class)
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .done()
        .transformAndLoad();
  }

  public @interface AnnotationWithoutRetention {}

  @Retention(RetentionPolicy.SOURCE)
  public @interface AnnotationWithSourceRetention {}

  @Retention(RetentionPolicy.CLASS)
  public @interface AnnotationWithClassRetention {}

  @Retention(RetentionPolicy.RUNTIME)
  public @interface AnnotationWithRuntimeRetention {}

  @FakeDoNotHandleExceptionAnnotation
  public static class TestClass {}
}
