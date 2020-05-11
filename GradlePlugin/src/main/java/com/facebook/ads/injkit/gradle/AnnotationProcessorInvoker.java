// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.gradle;

import com.facebook.ads.injkit.AnnotationProcessorConfigurationBuilder;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;

public class AnnotationProcessorInvoker {
  private AnnotationProcessorInvoker() {}

  public static void invoke(Task compileTask, File configurationFile) throws Exception {
    if (!AnnotationProcessorInvoker.class
        .getClassLoader()
        .getClass()
        .getName()
        .equals(LocalClassLoader.class.getName())) {
      URLClassLoader currentClassLoader =
          (URLClassLoader) AnnotationProcessorInvoker.class.getClassLoader();

      LocalClassLoader localLoader =
          new LocalClassLoader(currentClassLoader.getURLs(), currentClassLoader.getParent());
      Class<?> invokerClass = localLoader.loadClass(AnnotationProcessorInvoker.class.getName());
      Method invokeMethod = invokerClass.getDeclaredMethod("invoke", Task.class, File.class);
      invokeMethod.invoke(null, compileTask, configurationFile);
      return;
    }

    AnnotationProcessorConfigurationBuilder builder = new AnnotationProcessorConfigurationBuilder();
    compileTask.getOutputs().getFiles().getFiles().forEach(builder::addFileToTransform);
    builder.addClasspathElement(getDestinationDir(compileTask));
    builder.addClasspathElements(getClasspath(compileTask));
    builder.addClasspathElements(getBootstrapClasspath(compileTask));
    builder.setConfigurationFile(configurationFile).build().process();
  }

  private static Object invokeGetter(Object object, String getter) throws Exception {
    return object.getClass().getMethod(getter).invoke(object);
  }

  private static File getDestinationDir(Task compileTask) throws Exception {
    return (File) invokeGetter(compileTask, "getDestinationDir");
  }

  private static Iterable<File> getClasspath(Task compileTask) throws Exception {
    return ((FileCollection) invokeGetter(compileTask, "getClasspath")).getFiles();
  }

  private static Iterable<File> getBootstrapClasspath(Task compileTask) throws Exception {
    return ((FileCollection)
            invokeGetter(invokeGetter(compileTask, "getOptions"), "getBootstrapClasspath"))
        .getFiles();
  }
}
