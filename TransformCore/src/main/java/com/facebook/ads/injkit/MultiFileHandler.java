// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

class MultiFileHandler {
    private final List<SingleFileHandler> fileHandlers;

    MultiFileHandler(
            List<FilePair> files,
            AnnotationProcessorConfiguration configuration,
            URLClassLoader classpath,
            Model model,
            ClassFileProcessorFactory classFileProcessorFactory)
            throws InvalidAnnotationProcessorConfigurationException {
        fileHandlers = new ArrayList<>();
        for (FilePair filePair : files) {
            for (FilePair ioPair : filePair.expandIfDirectory()) {
                fileHandlers.add(
                        new SingleFileHandler(
                                ioPair,
                                configuration,
                                classpath,
                                model,
                                classFileProcessorFactory));
            }
        }
    }

    public void process() throws IOException, AnnotationProcessingException {
        List<String> errors = new ArrayList<>();
        IOException first = null;

        for (SingleFileHandler fileHandler : fileHandlers) {
            try {
                fileHandler.process();
            } catch (IOException e) {
                errors.add(e.getMessage());
                if (first == null) {
                    first = e;
                }
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            errors.forEach(e -> {
                sb.append(System.lineSeparator());
                sb.append(e);
            });

            throw new IOException("Error running annotation processor:" + sb.toString(), first);
        }
    }
}
