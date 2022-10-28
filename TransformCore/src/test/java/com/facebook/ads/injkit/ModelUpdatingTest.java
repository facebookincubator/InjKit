/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ModelUpdatingTest {

  @Rule public final TemporaryFolder temporaryFolder = new TemporaryFolder();

  private File configurationFile;

  @Before
  public void before() throws Exception {
    configurationFile = temporaryFolder.newFile();
  }

  private File copyResourceToNewFile(String resourceName) throws IOException {
    File output = temporaryFolder.newFile(resourceName);
    InputStream rsrc = ModelUpdatingTest.class.getResourceAsStream("/" + resourceName);
    assertThat(rsrc).isNotNull();
    Files.copy(rsrc, output.toPath(), StandardCopyOption.REPLACE_EXISTING);
    return output;
  }

  @Test
  public void classesAreAddedToModelBeforeProcessing() throws Exception {
    File in = copyResourceToNewFile("A.class");
    File out = temporaryFolder.newFile();

    AtomicBoolean knewA = new AtomicBoolean();

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(configurationFile)
        .processSystemPath(false)
        .setClassFileProcessorFactory(
            (configuration, classLoader, model) ->
                new ClassFileProcessorImpl(configuration, classLoader, model) {
                  @Override
                  public void process(InputStream input, OutputStream output) {
                    knewA.set(model.knowsClass("a/A"));
                  }
                })
        .build()
        .process();

    assertThat(knewA.get()).isTrue();
  }

  @Test
  public void classpathElementsAreAddedToModelBeforeProcessing() throws Exception {
    File cp = copyResourceToNewFile("b.jar");
    File in = copyResourceToNewFile("A.class");
    File out = temporaryFolder.newFile();

    AtomicBoolean knewB = new AtomicBoolean();

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .addClasspathElement(cp)
        .setConfigurationFile(configurationFile)
        .processSystemPath(false)
        .setClassFileProcessorFactory(
            (configuration, classLoader, model) ->
                new ClassFileProcessorImpl(configuration, classLoader, model) {
                  @Override
                  public void process(InputStream input, OutputStream output) {
                    knewB.set(model.knowsClass("b/B"));
                  }
                })
        .build()
        .process();

    assertThat(knewB.get()).isTrue();
  }

  @Test
  public void baseClasspathElementsAreAddedToModelBeforeProcessing() throws Exception {
    File in = copyResourceToNewFile("A.class");
    File out = temporaryFolder.newFile();

    AtomicBoolean knewMyself = new AtomicBoolean();

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .setConfigurationFile(configurationFile)
        .setClassFileProcessorFactory(
            (configuration, classLoader, model) ->
                new ClassFileProcessorImpl(configuration, classLoader, model) {
                  @Override
                  public void process(InputStream input, OutputStream output) {
                    knewMyself.set(
                        model.knowsClass(
                            AsmNameUtils.classJavaNameToInternalName(
                                ModelUpdatingTest.class.getName())));
                  }
                })
        .build()
        .process();

    assertThat(knewMyself.get()).isTrue();
  }

  @Test
  public void classpathDirectoriesElementsAreAddedToModelBeforeProcessing() throws Exception {
    File in = copyResourceToNewFile("b.jar");
    File cpDir = temporaryFolder.newFolder();
    File aclass = copyResourceToNewFile("A.class");
    Files.copy(aclass.toPath(), new File(cpDir, "A.class").toPath());
    File out = temporaryFolder.newFile();

    AtomicBoolean knewA = new AtomicBoolean();

    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(in, out)
        .addClasspathElement(cpDir)
        .setConfigurationFile(configurationFile)
        .processSystemPath(false)
        .setClassFileProcessorFactory(
            (configuration, classLoader, model) ->
                new ClassFileProcessorImpl(configuration, classLoader, model) {
                  @Override
                  public void process(InputStream input, OutputStream output) {
                    knewA.set(model.knowsClass("a/A"));
                  }
                })
        .build()
        .process();

    assertThat(knewA.get()).isTrue();
  }
}
