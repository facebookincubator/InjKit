/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.benchmark;

import com.facebook.ads.injkit.TransformationEnvironment;
import java.io.PrintWriter;
import javax.annotation.Nullable;

class BenchmarkConfigurationWriter<T extends TransformationEnvironment.ConfigurationFileWriter<T>>
    implements TransformationEnvironment.ModuleConfigurationWriter<T> {
  private final T configurationFileWriter;
  private boolean enabled;
  @Nullable private String benchmarkAnnotationClassName;
  @Nullable private String benchmarkReceiverClassName;

  public BenchmarkConfigurationWriter(T configurationFileWriter) {
    this.configurationFileWriter = configurationFileWriter;
    enabled = true;
  }

  @Override
  public T done() {
    if (benchmarkAnnotationClassName == null) {
      throw new IllegalStateException("Benchmark annotation not defined");
    }

    if (enabled && benchmarkReceiverClassName == null) {
      throw new IllegalStateException("Benchmark receiver not defined");
    }

    return configurationFileWriter;
  }

  BenchmarkConfigurationWriter<T> disableBenchmarks() {
    enabled = false;
    return this;
  }

  BenchmarkConfigurationWriter<T> benchmarkAnnotation(String benchmarkAnnotationClassName) {
    this.benchmarkAnnotationClassName = benchmarkAnnotationClassName;
    return this;
  }

  BenchmarkConfigurationWriter<T> benchmarkAnnotation(Class<?> benchmarkAnnotation) {
    return benchmarkAnnotation(benchmarkAnnotation.getName());
  }

  BenchmarkConfigurationWriter<T> benchmarkReceiver(String benchmarkReceiverClassName) {
    this.benchmarkReceiverClassName = benchmarkReceiverClassName;
    return this;
  }

  BenchmarkConfigurationWriter<T> benchmarkReceiver(Class<?> cls) {
    return benchmarkReceiver(cls.getName());
  }

  @Override
  public void write(PrintWriter pw) {
    pw.print(BenchmarkConfigurationConstants.ENABLED);
    pw.print(" ");
    pw.println(enabled);

    pw.print(BenchmarkConfigurationConstants.ANNOTATION_CLASS);
    pw.print(" ");
    pw.println(benchmarkAnnotationClassName);

    if (benchmarkReceiverClassName != null) {
      pw.print(BenchmarkConfigurationConstants.RECEIVER_CLASS);
      pw.print(" ");
      pw.println(benchmarkReceiverClassName);
    }
  }

  static class Factory<T extends TransformationEnvironment.ConfigurationFileWriter<T>>
      implements TransformationEnvironment.ModuleConfigurationWriterFactory<
          T, BenchmarkConfigurationWriter<T>> {
    @Override
    public BenchmarkConfigurationWriter<T> make(T configurationFileWriter) {
      return new BenchmarkConfigurationWriter<>(configurationFileWriter);
    }
  }
}
