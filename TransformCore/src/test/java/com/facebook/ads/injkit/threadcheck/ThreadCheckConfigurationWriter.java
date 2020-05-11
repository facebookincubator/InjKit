// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.threadcheck;

import com.facebook.ads.injkit.TransformationEnvironment;
import java.io.PrintWriter;

class ThreadCheckConfigurationWriter<T extends TransformationEnvironment.ConfigurationFileWriter<T>>
    implements TransformationEnvironment.ModuleConfigurationWriter<T> {
  private final T configurationFileWriter;
  private boolean enabled;
  private String uiThreadAnnotationClass;
  private String workerThreadAnnotationClass;
  private String anyThreadAnnotationClass;
  private String violationHandlerClass;

  public ThreadCheckConfigurationWriter(T configurationFileWriter) {
    this.configurationFileWriter = configurationFileWriter;
    enabled = true;
  }

  @Override
  public T done() {
    if (enabled && uiThreadAnnotationClass == null) {
      throw new IllegalStateException("UI thread annotation not defined");
    }

    if (enabled && workerThreadAnnotationClass == null) {
      throw new IllegalStateException("Worker thread annotation not defined");
    }

    if (enabled && anyThreadAnnotationClass == null) {
      throw new IllegalStateException("Any thread annotation not defined");
    }

    if (enabled && violationHandlerClass == null) {
      throw new IllegalStateException("Violation handler class not defined");
    }

    return configurationFileWriter;
  }

  ThreadCheckConfigurationWriter<T> disableThreadCheck(boolean disable) {
    if (disable) {
      enabled = false;
    }

    return this;
  }

  ThreadCheckConfigurationWriter<T> uiThreadAnnotationClass(String uiThreadAnnotationClass) {
    this.uiThreadAnnotationClass = uiThreadAnnotationClass;
    return this;
  }

  ThreadCheckConfigurationWriter<T> uiThreadAnnotationClass(Class<?> uiThreadAnnotationClass) {
    return uiThreadAnnotationClass(uiThreadAnnotationClass.getName());
  }

  ThreadCheckConfigurationWriter<T> workerThreadAnnotationClass(
      String workerThreadAnnotationClass) {
    this.workerThreadAnnotationClass = workerThreadAnnotationClass;
    return this;
  }

  ThreadCheckConfigurationWriter<T> workerThreadAnnotationClass(
      Class<?> workerThreadAnnotationClass) {
    return workerThreadAnnotationClass(workerThreadAnnotationClass.getName());
  }

  ThreadCheckConfigurationWriter<T> anyThreadAnnotationClass(String anyThreadAnnotationClass) {
    this.anyThreadAnnotationClass = anyThreadAnnotationClass;
    return this;
  }

  ThreadCheckConfigurationWriter<T> anyThreadAnnotationClass(Class<?> anyThreadAnnotationClass) {
    return anyThreadAnnotationClass(anyThreadAnnotationClass.getName());
  }

  ThreadCheckConfigurationWriter<T> violationHandlerClass(String violationHandlerClass) {
    this.violationHandlerClass = violationHandlerClass;
    return this;
  }

  ThreadCheckConfigurationWriter<T> violationHandlerClass(Class<?> violationHandlerClass) {
    return violationHandlerClass(violationHandlerClass.getName());
  }

  @Override
  public void write(PrintWriter pw) {
    pw.print(ThreadCheckConfigurationConstants.ENABLED);
    pw.print(" ");
    pw.println(enabled);

    if (uiThreadAnnotationClass != null) {
      pw.print(ThreadCheckConfigurationConstants.UI_THREAD_ANNOTATION_CLASS);
      pw.print(" ");
      pw.println(uiThreadAnnotationClass);
    }

    if (workerThreadAnnotationClass != null) {
      pw.print(ThreadCheckConfigurationConstants.WORKER_THREAD_ANNOTATION_CLASS);
      pw.print(" ");
      pw.println(workerThreadAnnotationClass);
    }

    if (anyThreadAnnotationClass != null) {
      pw.print(ThreadCheckConfigurationConstants.ANY_THREAD_ANNOTATION_CLASS);
      pw.print(" ");
      pw.println(anyThreadAnnotationClass);
    }

    if (violationHandlerClass != null) {
      pw.print(ThreadCheckConfigurationConstants.VIOLATION_HANDLER_CLASS);
      pw.print(" ");
      pw.println(violationHandlerClass);
    }
  }

  static class Factory<T extends TransformationEnvironment.ConfigurationFileWriter<T>>
      implements TransformationEnvironment.ModuleConfigurationWriterFactory<
          T, ThreadCheckConfigurationWriter<T>> {
    @Override
    public ThreadCheckConfigurationWriter<T> make(T configurationFileWriter) {
      return new ThreadCheckConfigurationWriter<>(configurationFileWriter);
    }
  }
}
