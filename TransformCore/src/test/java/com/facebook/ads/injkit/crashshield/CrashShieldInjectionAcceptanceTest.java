// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.crashshield;

import com.facebook.ads.injkit.TransformationEnvironment;
import com.facebook.ads.injkit.crashshield.annotated_with_fake.TestClasses;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CrashShieldInjectionAcceptanceTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private TransformationEnvironment environment;

  @Before
  public void before() throws Exception {
    environment = new TransformationEnvironment(temporaryFolder);
  }

  @Test
  public void acceptsIfUnannotatedMethodOrClassOrPackage() throws Exception {
    environment.addProcessingClass(ClassWithNonAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .processPackage(ClassWithNonAnnotatedMethod.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void acceptsIfAnnotatedMethodAndUnannotatedClassOrPackage() throws Exception {
    environment.addProcessingClass(ClassWithAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .processPackage(ClassWithAnnotatedMethod.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void acceptsIfAnnotatedClassAndUnannotatedMethodOrPackage() throws Exception {
    environment.addProcessingClass(AnnotatedClassWithNonAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .processPackage(AnnotatedClassWithNonAnnotatedMethod.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void acceptsIfAnnotatedClassAndMethodAndUnannotatedPackage() throws Exception {
    environment.addProcessingClass(AnnotatedClassWithAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .processPackage(AnnotatedClassWithAnnotatedMethod.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void acceptsIfAnnotatedPackageAndUnannotatedMethodOrClass() throws Exception {
    environment.addProcessingClass(TestClasses.ClassWithNonAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .processPackage(TestClasses.ClassWithNonAnnotatedMethod.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void acceptsIfAnnotatedPackageAndMethodAndUnannotatedClass() throws Exception {
    environment.addProcessingClass(TestClasses.ClassWithAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .processPackage(TestClasses.ClassWithAnnotatedMethod.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void acceptsIfAnnotatedPackageAndClassAndUnannotatedMethod() throws Exception {
    environment.addProcessingClass(TestClasses.AnnotatedClassWithNonAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .processPackage(TestClasses.AnnotatedClassWithNonAnnotatedMethod.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void acceptsIfAnnotatedPackageAndClassAndMethod() throws Exception {
    environment.addProcessingClass(TestClasses.AnnotatedClassWithAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .processPackage(TestClasses.AnnotatedClassWithAnnotatedMethod.class)
        .done()
        .transformAndLoad();
  }

  @Test
  public void acceptsIfUnannotatedMethodOrClassOrPackageButIsIgnored() throws Exception {
    environment.addProcessingClass(ClassWithNonAnnotatedMethod.class);
    environment
        .newLoadableConfigurationWriter()
        .enable(new CrashShieldConfigurationWriter.Factory<>())
        .noTransformAnnotation(FakeDoNotHandleExceptionAnnotation.class)
        .transformAnnotation(FakeHandleExceptionsAnnotation.class)
        .handler(FakeExceptionHandler.class)
        .done()
        .transformAndLoad();
  }

  public static class ClassWithNonAnnotatedMethod {
    public void someUnannotatedPublicMethod() {}
  }

  public static class ClassWithAnnotatedMethod {
    @FakeHandleExceptionsAnnotation
    public void annotatedMethod() {}
  }

  @FakeHandleExceptionsAnnotation
  public static class AnnotatedClassWithNonAnnotatedMethod {
    public void nonAnnotatedMethod() {}
  }

  @FakeHandleExceptionsAnnotation
  public static class AnnotatedClassWithAnnotatedMethod {
    @FakeHandleExceptionsAnnotation
    public void annotatedMethod() {}
  }
}
