/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.sdkdebugger;

import com.facebook.ads.injkit.TransformationEnvironment;
import java.io.PrintWriter;
import javax.annotation.Nullable;

public class SdkDebuggerConfigurationWriter<
        T extends TransformationEnvironment.ConfigurationFileWriter<T>>
    implements TransformationEnvironment.ModuleConfigurationWriter<T> {
  private final T configurationFileWriter;
  @Nullable private String logCallAnnotationName;
  @Nullable private String loggerClassName;

  public SdkDebuggerConfigurationWriter(T configurationFileWriter) {
    this.configurationFileWriter = configurationFileWriter;
  }

  @Override
  public T done() {
    if (loggerClassName == null) {
      throw new IllegalStateException("Logger class not defined");
    }

    if (logCallAnnotationName == null) {
      throw new IllegalStateException("Log call annotation not defined");
    }

    return configurationFileWriter;
  }

  SdkDebuggerConfigurationWriter<T> logCallAnnotation(String logCallAnnotationName) {
    this.logCallAnnotationName = logCallAnnotationName;
    return this;
  }

  SdkDebuggerConfigurationWriter<T> logCallAnnotation(Class<?> cls) {
    return logCallAnnotation(cls.getName());
  }

  SdkDebuggerConfigurationWriter<T> logger(String loggerClassName) {
    this.loggerClassName = loggerClassName;
    return this;
  }

  SdkDebuggerConfigurationWriter<T> logger(Class<?> cls) {
    return logger(cls.getName());
  }

  @Override
  public void write(PrintWriter pw) {
    pw.print(SdkDebuggerConfigurationConstants.ENABLED);
    pw.println(" true");

    pw.print(SdkDebuggerConfigurationConstants.LOG_CALL_ANNOTATIONS);
    pw.print(" ");
    pw.println(logCallAnnotationName);

    pw.print(SdkDebuggerConfigurationConstants.CALL_LOGGER);
    pw.print(" ");
    pw.println(loggerClassName);
  }

  public static class Factory<T extends TransformationEnvironment.ConfigurationFileWriter<T>>
      implements TransformationEnvironment.ModuleConfigurationWriterFactory<
          T, SdkDebuggerConfigurationWriter<T>> {
    @Override
    public SdkDebuggerConfigurationWriter<T> make(T configurationFileWriter) {
      return new SdkDebuggerConfigurationWriter<>(configurationFileWriter);
    }
  }
}
