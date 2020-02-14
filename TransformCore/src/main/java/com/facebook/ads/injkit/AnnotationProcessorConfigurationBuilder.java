// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.ModelFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnnotationProcessorConfigurationBuilder {
    private File configurationFile;
    private final List<FilePair> files = new ArrayList<>();
    private final List<File> classpathElements = new ArrayList<>();
    private ClassFileProcessorFactory classFileProcessorFactory =
            ClassFileProcessorFactory.getDefault();
    private boolean processSystemPath = true;

    public AnnotationProcessorConfigurationBuilder addFileToTransform(File file) {
        return addInputOutputMap(file, file);
    }

    public AnnotationProcessorConfigurationBuilder addInputOutputMap(File input, File output) {
        files.add(new FilePair(input, output));
        return this;
    }

    public AnnotationProcessorConfigurationBuilder setConfigurationFile(File configurationFile) {
        this.configurationFile = configurationFile;
        return this;
    }

    public AnnotationProcessorConfigurationBuilder addClasspathElement(File element) {
        classpathElements.add(element);
        return this;
    }

    public AnnotationProcessorConfigurationBuilder addClasspathElements(Iterable<File> elements) {
        for (File element : elements) {
            addClasspathElement(element);
        }

        return this;
    }

    AnnotationProcessorConfigurationBuilder setClassFileProcessorFactory(
            ClassFileProcessorFactory factory) {
        this.classFileProcessorFactory = factory;
        return this;
    }

    // Setting this to false makes the model builder not look into the system path for classes.
    // This prevents us from finding standard annotations, but makes annotation processing much
    // faster, something we probably want in unit tests.
    AnnotationProcessorConfigurationBuilder processSystemPath(boolean processSystemPath) {
        this.processSystemPath = processSystemPath;
        return this;
    }

    public AnnotationProcessor build()
            throws IOException, InvalidAnnotationProcessorConfigurationException {
        if (configurationFile == null) {
            throw new IllegalArgumentException("configurationFile not defined");
        }

        AnnotationProcessorConfiguration configuration =
                AnnotationProcessorConfiguration.parse(
                        configurationFile,
                        AnnotationProcessorModules.getModules());

        return new AnnotationProcessorImpl(
                files,
                configuration,
                classpathElements,
                ModelFactory.defaultFactory().make(),
                classFileProcessorFactory,
                processSystemPath);
    }
}
