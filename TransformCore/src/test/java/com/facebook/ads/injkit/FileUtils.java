// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

public class FileUtils {

    private FileUtils() {}

    public static File createConfigurationFile(
            TemporaryFolder temporaryFolder,
            String... lines) throws Exception {
        File file = temporaryFolder.newFile();
        try (FileWriter fw = new FileWriter(file)) {
            for (String line : lines) {
                if (line != null) {
                    fw.write(line);
                    fw.write(System.lineSeparator());
                }
            }
        }

        return file;
    }
}
