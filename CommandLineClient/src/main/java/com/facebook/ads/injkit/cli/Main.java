/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.cli;

import com.facebook.ads.injkit.AnnotationProcessorConfigurationBuilder;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class Main {
  // --input=<input file>
  // --output=<output file>
  // --config=<config file>
  // --classpath=<files> (separated by classpath separator)
  public static void main(String[] args) throws Exception {
    CommandDescription commandDescription = CommandDescription.parse(args);
    new AnnotationProcessorConfigurationBuilder()
        .addInputOutputMap(commandDescription.getInputFile(), commandDescription.getOutputFile())
        .setConfigurationFile(commandDescription.getConfigFile())
        .addClasspathElements(commandDescription.getClasspath())
        .build()
        .process();
  }
}
