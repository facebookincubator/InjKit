// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConfigurationFileTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void emptyConfigurationFileAccepted() throws Exception {
    File config = FileUtils.createConfigurationFile(temporaryFolder);
    AnnotationProcessorConfiguration.parse(config, Collections.emptySet());
  }

  @Test
  public void commentsAndEmptyLinesIgnored() throws Exception {
    File config =
        FileUtils.createConfigurationFile(
            temporaryFolder, "", "# Comment", "  # comment  ", "", "#");

    AnnotationProcessorConfiguration.parse(config, Collections.emptySet());
  }

  @Test
  public void invalidDirectiveReported() throws Exception {
    File config = FileUtils.createConfigurationFile(temporaryFolder, "foo bar");

    try {
      AnnotationProcessorConfiguration.parse(config, Collections.emptySet());
      fail();
    } catch (InvalidAnnotationProcessorConfigurationException e) {
      assertThat(e).hasMessageContaining("foo");
    }
  }
}
