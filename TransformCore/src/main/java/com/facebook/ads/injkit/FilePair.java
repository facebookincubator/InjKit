// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class FilePair {
  private final File input;
  private final File output;

  public FilePair(File input, File output) {
    this.input = input;
    this.output = output;
  }

  public File getInput() {
    return input;
  }

  public File getOutput() {
    return output;
  }

  public Set<FilePair> expandIfDirectory() {
    if (input.isFile()) {
      return Collections.singleton(this);
    }

    if (input.isDirectory()) {
      return Arrays.stream(input.list())
          .map(name -> new FilePair(new File(input, name), new File(output, name)))
          .map(FilePair::expandIfDirectory)
          .flatMap(Set::stream)
          .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }
}
