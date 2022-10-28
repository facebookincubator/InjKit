/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class TestClassFileProcessor implements ClassFileProcessor {
  final List<byte[]> data = new ArrayList<>();
  final Stack<byte[]> outs = new Stack<>();

  @Override
  public void process(InputStream input, OutputStream output)
      throws IOException, AnnotationProcessingException {
    ByteArrayOutputStream inputData = new ByteArrayOutputStream();
    ByteStreams.copy(input, inputData);
    data.add(inputData.toByteArray());
    if (!outs.empty()) {
      output.write(outs.pop());
    } else {
      output.write(inputData.toByteArray());
    }
  }

  @Override
  public void updateModel(InputStream input, Model model)
      throws IOException, AnnotationProcessingException {}

  ClassFileProcessorFactory factoryOfMyself() {
    return (AnnotationProcessorConfiguration configuration,
        URLClassLoader classLoader,
        Model model) -> this;
  }
}
