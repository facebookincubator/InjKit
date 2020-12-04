// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.google.common.base.StandardSystemProperty.PATH_SEPARATOR;

import com.facebook.ads.injkit.model.Model;
import com.google.common.base.Splitter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class AnnotationProcessorImpl implements AnnotationProcessor {
  private final MultiFileHandler multiFileHandler;
  private final List<File> classpathElements;
  private final List<File> inputFiles;
  private final Model model;
  private final ClassFileProcessor classFileProcessor;
  private final boolean processSystemPath;

  AnnotationProcessorImpl(
      List<FilePair> files,
      AnnotationProcessorConfiguration configuration,
      List<File> classpathElements,
      Model model,
      ClassFileProcessorFactory classFileProcessorFactory,
      boolean processSystemPath)
      throws IOException, InvalidAnnotationProcessorConfigurationException {

    files =
        files.stream()
            .map(FilePair::expandIfDirectory)
            .flatMap(Set::stream)
            .collect(Collectors.toList());

    URL[] urls = new URL[classpathElements.size()];
    for (int i = 0; i < classpathElements.size(); i++) {
      urls[i] = classpathElements.get(i).toURI().toURL();
    }

    URLClassLoader classpathLoader = new URLClassLoader(urls);

    this.classpathElements = classpathElements;
    this.inputFiles = files.stream().map(FilePair::getInput).collect(Collectors.toList());
    this.model = model;
    this.classFileProcessor = classFileProcessorFactory.make(configuration, classpathLoader, model);
    this.processSystemPath = processSystemPath;

    multiFileHandler =
        new MultiFileHandler(
            files, configuration, classpathLoader, model, classFileProcessorFactory);
  }

  @Override
  public void process() throws IOException, AnnotationProcessingException {
    if (processSystemPath) {
      updateModelWithBaseClasspathElements();
    }

    updateModelWithClasspathElements();
    updateModelWithFiles();

    multiFileHandler.process();
  }

  private void updateModelWithBaseClasspathElements()
      throws IOException, AnnotationProcessingException {
    List<ClassLoader> ldrs = new ArrayList<>();
    for (ClassLoader ldr = AnnotationProcessorImpl.class.getClassLoader();
        ldr != null;
        ldr = ldr.getParent()) {
      ldrs.add(ldr);
    }

    // Process the loaders in inverse order to be more likely consistent with class precedence.
    Collections.reverse(ldrs);
    List<File> files = new ArrayList<>();
    for (ClassLoader ldr : ldrs) {
      if (ldr instanceof URLClassLoader) {
        URL[] urls = ((URLClassLoader) ldr).getURLs();
        for (URL url : urls) {
          if ("file".equals(url.getProtocol()) || url.getProtocol() == null) {
            File file = new File(URLDecoder.decode(url.getFile(), Charset.defaultCharset().name()));
            addRecursive(file, files);
          }
        }
      } else {
        for (String path : Splitter.on(PATH_SEPARATOR.value()).split(JAVA_CLASS_PATH.value())) {
          if (!path.endsWith(".jar")) {
            continue;
          }
          File file = new File(path);
          addRecursive(file, files);
        }
      }
    }

    updateModel(files);
  }

  private void updateModelWithClasspathElements()
      throws IOException, AnnotationProcessingException {
    updateModel(classpathElements);
  }

  private void updateModelWithFiles() throws IOException, AnnotationProcessingException {
    updateModel(inputFiles);
  }

  private void updateModel(List<File> files) throws IOException, AnnotationProcessingException {
    for (File file : files) {
      if (file.isDirectory()) {
        updateModel(Arrays.asList(file.listFiles()));
        return;
      }

      ZipRecursionHandler.handle(
          file,
          new ZipRecursionHandler.FileConsumer() {
            @Override
            public void consumeFile(boolean isClass, InputStream input)
                throws IOException, AnnotationProcessingException {
              if (isClass) {
                classFileProcessor.updateModel(input, model);
              }
            }

            @Override
            public void consumeZip(ZipRecursionHandler.ZipHandler handler)
                throws IOException, AnnotationProcessingException {
              handler.handleZip(
                  (String path, boolean isClass, InputStream input) -> {
                    if (isClass) {
                      classFileProcessor.updateModel(input, model);
                    }
                  });
            }
          });
    }
  }

  private static void addRecursive(File file, List<File> files) {
    if (file.isFile()) {
      files.add(file);
    } else if (file.isDirectory()) {
      File[] contents = file.listFiles();
      if (contents != null) {
        for (File f : contents) {
          addRecursive(f, files);
        }
      }
    }
  }
}
