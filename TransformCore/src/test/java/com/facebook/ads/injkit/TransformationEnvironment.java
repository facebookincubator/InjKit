/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.google.common.base.StandardSystemProperty.PATH_SEPARATOR;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.rules.TemporaryFolder;

public class TransformationEnvironment {
  private final File inputDirectory;
  private final File outputDirectory;
  private final File configurationFile;

  public TransformationEnvironment(TemporaryFolder temporaryFolder) throws IOException {
    inputDirectory = temporaryFolder.newFolder();
    outputDirectory = temporaryFolder.newFolder();
    configurationFile = temporaryFolder.newFile();
  }

  public TransformationEnvironment addProcessingClass(Class<?> cls) throws IOException {
    if (cls == null) {
      return this;
    }

    ClassLoader ldr = ClassFilesTest.TestClass.class.getClassLoader();

    String clsPath = cls.getName().replace('.', '/') + ".class";
    String systemClsPath = clsPath.replaceAll("/", System.getProperty("file.separator"));

    // Hacky stuff: we assume the code is in a jar somewhere :) Find the jar containing the
    // class and add it to the input directory.

    ImmutableList.Builder<File> libraries = ImmutableList.builder();
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    if (ldr instanceof URLClassLoader) {
      for (URL url : ((URLClassLoader) ldr).getURLs()) {
        if (url.getProtocol().equals("file")) {
          String path = url.getPath();
          path = URLDecoder.decode(path, "utf-8");
          libraries.add(new File(path));
        }
      }
    } else {
      for (String path : Splitter.on(PATH_SEPARATOR.value()).split(JAVA_CLASS_PATH.value())) {
        if (!path.endsWith(".jar")) {
          continue;
        }
        libraries.add(new File(path));
      }
    }

    for (File pathFile : libraries.build()) {
      if (pathFile.isFile()) {
        try (ZipFile zf = new ZipFile(pathFile)) {
          ZipEntry entry = zf.getEntry(clsPath);
          if (entry == null) {
            continue;
          }

          File result = new File(inputDirectory, systemClsPath);
          result.getParentFile().mkdirs();

          try (FileOutputStream output = new FileOutputStream(result);
              InputStream input = zf.getInputStream(entry)) {
            byte[] buffer = new byte[4096];
            int r;
            while ((r = input.read(buffer)) > 0) {
              output.write(buffer, 0, r);
            }
          }

          return this;
        }
      } else if (pathFile.isDirectory()) {
        File source = new File(pathFile, systemClsPath);
        if (source.isFile()) {
          File result = new File(inputDirectory, systemClsPath);
          result.getParentFile().mkdirs();
          Files.copy(source, result);
          return this;
        }
      } else {
        // Not a file or directory. Ignore.
      }
    }

    throw new IOException("Could not find class '" + cls.getCanonicalName() + "'");
  }

  public Object invoke(ClassLoader loader, Class<?> cls, String name) throws Exception {
    Class<?> loaded = loader.loadClass(cls.getName());
    Method m = loaded.getDeclaredMethod(name);
    return m.invoke(loaded.newInstance());
  }

  static SimpleConfigurationFileWriter newConfigurationWriter() {
    return new SimpleConfigurationFileWriter();
  }

  public LoadableConfigurationFileWriter newLoadableConfigurationWriter() {
    return new LoadableConfigurationFileWriter();
  }

  public class LoadableConfigurationFileWriter
      extends ConfigurationFileWriter<LoadableConfigurationFileWriter> {
    public ClassLoader transformAndLoad() throws Exception {
      write(configurationFile);

      new AnnotationProcessorConfigurationBuilder()
          .addInputOutputMap(inputDirectory, outputDirectory)
          .setConfigurationFile(configurationFile)
          .processSystemPath(false)
          .build()
          .process();

      return new URLClassLoader(new URL[] {outputDirectory.toURI().toURL()}, null) {
        @Override
        protected Class<?> loadClass(String s, boolean b) throws ClassNotFoundException {
          try {
            return super.loadClass(s, b);
          } catch (ClassNotFoundException e) {
            return TransformationEnvironment.class.getClassLoader().loadClass(s);
          }
        }
      };
    }

    @Override
    LoadableConfigurationFileWriter thisAsT() {
      return this;
    }
  }

  static class SimpleConfigurationFileWriter
      extends ConfigurationFileWriter<SimpleConfigurationFileWriter> {
    @Override
    SimpleConfigurationFileWriter thisAsT() {
      return this;
    }
  }

  public interface ModuleConfigurationWriter<T extends ConfigurationFileWriter<T>> {
    T done();

    void write(PrintWriter pw);
  }

  public interface ModuleConfigurationWriterFactory<
      T extends ConfigurationFileWriter<T>, WriterT extends ModuleConfigurationWriter<T>> {
    WriterT make(T configurationFileWriter);
  }

  public abstract static class ConfigurationFileWriter<T extends ConfigurationFileWriter<T>> {
    private final Set<ModuleConfigurationWriter<T>> writers = new HashSet<>();

    public <WriterT extends ModuleConfigurationWriter<T>> WriterT enable(
        ModuleConfigurationWriterFactory<T, WriterT> factory) {
      WriterT newWriter = factory.make(thisAsT());
      writers.add(newWriter);
      return newWriter;
    }

    abstract T thisAsT();

    void write(File configurationFile) throws IOException {
      try (PrintWriter configPrinter = new PrintWriter(configurationFile)) {
        for (ModuleConfigurationWriter<T> writer : writers) {
          writer.write(configPrinter);
        }
      }
    }
  }
}
