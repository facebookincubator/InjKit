// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class SingleFileHandler {
    private final FilePair inputOutputPair;
    private final ClassFileProcessor classFileProcessor;

    SingleFileHandler(
            FilePair inputOutputPair,
            AnnotationProcessorConfiguration configuration,
            URLClassLoader classpath,
            Model model,
            ClassFileProcessorFactory classFileProcessorFactory)
            throws InvalidAnnotationProcessorConfigurationException {
        this.inputOutputPair = inputOutputPair;
        this.classFileProcessor = classFileProcessorFactory.make(configuration, classpath, model);
    }

    public void process() throws IOException, AnnotationProcessingException {
        File parent = inputOutputPair.getOutput().getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException(
                    String.format(
                            Locale.US,
                            "Failed to create directory '%s'",
                            parent.getCanonicalPath()));
        }

        ZipRecursionHandler.handle(
                inputOutputPair.getInput(),
                new ZipRecursionHandler.FileConsumer() {
            @Override
            public void consumeFile(boolean isClass, InputStream input)
                    throws IOException, AnnotationProcessingException {
                if (!isClass) {
                    // Copy only if the destination is not the source.
                    if (!inputOutputPair.getInput().equals(inputOutputPair.getOutput())) {
                        try (FileOutputStream output =
                                     new FileOutputStream(inputOutputPair.getOutput())) {
                            copyData(input, output);
                        }
                    }
                } else {
                    // Transform to memory because if destination is the same as source, streaming
                    // won't work.
                    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
                    transform(inputOutputPair.getInput().getCanonicalPath(), input, outputBytes);
                    Files.write(inputOutputPair.getOutput().toPath(), outputBytes.toByteArray());
                }
            }

            @Override
            public void consumeZip(ZipRecursionHandler.ZipHandler handler)
                    throws IOException, AnnotationProcessingException {
                File output;
                if (inputOutputPair.getInput().equals(inputOutputPair.getOutput())) {
                    executeActionWithTempFile(tempFile -> {
                        handleConsumeZip(handler, tempFile);
                        Files.copy(tempFile.toPath(), inputOutputPair.getOutput().toPath());
                    });
                } else {
                    handleConsumeZip(handler, inputOutputPair.getOutput());
                }
            }
        });
    }

    private void handleConsumeZip(ZipRecursionHandler.ZipHandler handler, File zipOutput)
            throws IOException, AnnotationProcessingException {
        try (FileOutputStream fileOutput = new FileOutputStream(zipOutput);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutput)) {
            handler.handleZip((String path, boolean isClass, InputStream input) -> {
                ZipEntry nextOutputEntry = new ZipEntry(path);
                zipOutputStream.putNextEntry(nextOutputEntry);

                try {
                    transform(path, input, zipOutputStream);
                } catch (IOException e) {
                    throw new IOException(
                            String.format(
                                    Locale.US,
                                    "Failed to process zip entry '%s'",
                                    path),
                            e);
                } catch (AnnotationProcessingException e) {
                    throw new AnnotationProcessingException(
                            String.format(
                                    Locale.US,
                                    "Failed to process zip entry '%s'",
                                    path),
                            e);
                }
            });
        }
    }

    private void transform(String name, InputStream input, OutputStream output)
            throws IOException, AnnotationProcessingException {
        ClassFileDetectorStream inputStreamWithClassDetector =
                new ClassFileDetectorStream(name, input);
        try {
            if (inputStreamWithClassDetector.isClass()) {
                transformClass(inputStreamWithClassDetector, output);
            } else {
                copyData(inputStreamWithClassDetector, output);
            }
        } catch (AnnotationProcessingException e) {
            throw new AnnotationProcessingException(
                    String.format(Locale.US, "Failed to transform file '%s'", name), e);
        } catch (Exception e) {
            throw new IOException(
                    String.format(Locale.US, "Failed to transform file '%s'", name), e);
        }
    }

    private void transformClass(InputStream input, OutputStream output)
            throws IOException, AnnotationProcessingException {
        classFileProcessor.process(input, output);
    }

    private static void copyData(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int r;
        while ((r = input.read(buffer)) > 0) {
            output.write(buffer, 0, r);
        }
    }

    private static void executeActionWithTempFile(ActionWithTempFile action)
            throws IOException, AnnotationProcessingException {
        File temp = File.createTempFile("anpr", "zip");
        action.execute(temp);
        if (!temp.delete()) {
            throw new IOException(
                    String.format(Locale.US, "Failed to delete file '%s'", temp.getAbsolutePath()));
        }
    }

    private interface ActionWithTempFile {
        void execute(File tempFile) throws IOException, AnnotationProcessingException;
    }
}
