/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield;

import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.TransformationEnvironment;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CrashShieldConfigurationWriter<
        T extends TransformationEnvironment.ConfigurationFileWriter<T>>
    implements TransformationEnvironment.ModuleConfigurationWriter<T> {
  private final T configurationFileWriter;
  private String handlerClassName;
  private String transformAnnotationClassName;
  private String noTransformAnnotationClassName;
  private final List<String> processablePackages = new ArrayList<>();
  private boolean shouldProcessConstructor;
  private boolean shouldProcessViews;

  public CrashShieldConfigurationWriter(T configurationFileWriter) {
    this.configurationFileWriter = configurationFileWriter;
  }

  public T done() {
    if (handlerClassName == null) {
      throw new IllegalStateException("handler not defined");
    }

    if (transformAnnotationClassName == null) {
      throw new IllegalStateException("Transform annotation not defined");
    }

    if (noTransformAnnotationClassName == null) {
      throw new IllegalStateException("No transform annotation not defined");
    }

    return configurationFileWriter;
  }

  public CrashShieldConfigurationWriter<T> handler(String handlerClassName) {
    this.handlerClassName = handlerClassName;
    return this;
  }

  public CrashShieldConfigurationWriter<T> shouldProcessConstructor(boolean shouldProcessConstr) {
    this.shouldProcessConstructor = shouldProcessConstr;
    return this;
  }

  public CrashShieldConfigurationWriter<T> shouldProcessViews(boolean shouldProcessViews) {
    this.shouldProcessViews = shouldProcessViews;
    return this;
  }

  public CrashShieldConfigurationWriter<T> handler(Class<?> cls) {
    return handler(cls.getName());
  }

  CrashShieldConfigurationWriter<T> transformAnnotation(String transformAnnotationClassName) {
    this.transformAnnotationClassName = transformAnnotationClassName;
    return this;
  }

  public CrashShieldConfigurationWriter<T> transformAnnotation(Class<?> transformAnnotation) {
    return transformAnnotation(transformAnnotation.getName());
  }

  CrashShieldConfigurationWriter<T> noTransformAnnotation(String noTransformAnnotationClassName) {
    this.noTransformAnnotationClassName = noTransformAnnotationClassName;
    return this;
  }

  public CrashShieldConfigurationWriter<T> noTransformAnnotation(Class<?> noTransformAnnotation) {
    return noTransformAnnotation(noTransformAnnotation.getName());
  }

  public CrashShieldConfigurationWriter<T> processPackage(String pkg) {
    processablePackages.add(pkg);
    return this;
  }

  public CrashShieldConfigurationWriter<T> processPackage(Class<?> cls) {
    processablePackages.add(AsmNameUtils.packageJavaNameFromClassJavaName(cls.getName()));
    return this;
  }

  public CrashShieldConfigurationWriter<T> processPackage(Class<?>... classes) {
    for (Class<?> cls : classes) {
      processPackage(cls);
    }

    return this;
  }

  @Override
  public void write(PrintWriter pw) {
    pw.print(CrashShieldConfigurationConstants.ENABLED);
    pw.println(" true");
    pw.println();

    pw.print(CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS);
    pw.print(" ");
    pw.println(handlerClassName);

    pw.print(CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS);
    pw.print(" ");
    pw.println(transformAnnotationClassName);

    pw.print(CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS);
    pw.print(" ");
    pw.println(noTransformAnnotationClassName);

    pw.print(CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR);
    pw.print(" ");
    pw.println(shouldProcessConstructor);

    pw.print(CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS);
    pw.print(" ");
    pw.println(shouldProcessViews);
  }

  public static class Factory<T extends TransformationEnvironment.ConfigurationFileWriter<T>>
      implements TransformationEnvironment.ModuleConfigurationWriterFactory<
          T, CrashShieldConfigurationWriter<T>> {
    @Override
    public CrashShieldConfigurationWriter<T> make(T configurationFileWriter) {
      return new CrashShieldConfigurationWriter<>(configurationFileWriter);
    }
  }
}
