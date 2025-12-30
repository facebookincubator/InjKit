/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield

import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException
import com.facebook.ads.injkit.TransformationEnvironment
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AnnotationDefinitionTest {

  @JvmField @Rule var temporaryFolder = TemporaryFolder()
  private lateinit var environment: TransformationEnvironment

  @Before
  fun before() {
    environment = TransformationEnvironment(temporaryFolder)
    environment.addProcessingClass(TestClass::class.java)
  }

  @Test
  fun annotationWithoutRetentionFails() {
    try {
      environment
          .newLoadableConfigurationWriter()
          .enable(CrashShieldConfigurationWriter.Factory<Any>())
          .handler(FakeExceptionHandler::class.java)
          .transformAnnotation(AnnotationWithoutRetention::class.java)
          .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation::class.java)
          .done()
          .transformAndLoad()
    } catch (e: InvalidAnnotationProcessorConfigurationException) {
      assertThat(e).hasMessageContaining(AnnotationWithoutRetention::class.java.name)
      assertThat(e).hasMessageContaining(Retention::class.java.simpleName)
      assertThat(e).hasMessageContaining(RetentionPolicy.CLASS.name)
    }
  }

  @Test
  fun annotationWithSourceRetentionFails() {
    try {
      environment
          .newLoadableConfigurationWriter()
          .enable(CrashShieldConfigurationWriter.Factory<Any>())
          .handler(FakeExceptionHandler::class.java)
          .transformAnnotation(AnnotationWithSourceRetention::class.java)
          .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation::class.java)
          .done()
          .transformAndLoad()
    } catch (e: InvalidAnnotationProcessorConfigurationException) {
      assertThat(e).hasMessageContaining(AnnotationWithSourceRetention::class.java.name)
      assertThat(e).hasMessageContaining(Retention::class.java.simpleName)
      assertThat(e).hasMessageContaining(RetentionPolicy.CLASS.name)
    }
  }

  @Test
  fun annotationWithClassRetentionOk() {
    environment
        .newLoadableConfigurationWriter()
        .enable(CrashShieldConfigurationWriter.Factory<Any>())
        .handler(FakeExceptionHandler::class.java)
        .transformAnnotation(AnnotationWithClassRetention::class.java)
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation::class.java)
        .done()
        .transformAndLoad()
  }

  @Test
  fun annotationWithRuntimeRetentionOk() {
    environment
        .newLoadableConfigurationWriter()
        .enable(CrashShieldConfigurationWriter.Factory<Any>())
        .handler(FakeExceptionHandler::class.java)
        .transformAnnotation(AnnotationWithRuntimeRetention::class.java)
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation::class.java)
        .done()
        .transformAndLoad()
  }

  annotation class AnnotationWithoutRetention

  @Retention(RetentionPolicy.SOURCE) annotation class AnnotationWithSourceRetention

  @Retention(RetentionPolicy.CLASS) annotation class AnnotationWithClassRetention

  @Retention(RetentionPolicy.RUNTIME) annotation class AnnotationWithRuntimeRetention

  @FakeDoNotHandleExceptionAnnotation class TestClass
}
