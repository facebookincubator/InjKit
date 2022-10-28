/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class KeepsTrackOfClassAnnotationPropertiesTest {
  private final Model model = ModelFactory.defaultFactory().make();

  @Before
  public void before() {
    model.update(
        AsmCreationUtils.makeClassWithAnnotations(
            "my/Cls1",
            "java/lang/Object",
            Arrays.asList(
                AsmCreationUtils.makeAnnotation("Lmy/StringAnn;", "foo", "bar"),
                AsmCreationUtils.makeAnnotation("Lmy/IntAnn;", "foo", 42)),
            Collections.emptyList()));
    model.update(
        AsmCreationUtils.makeClassWithAnnotations(
            "my/Cls2",
            "my/Cls1",
            Collections.singletonList(
                AsmCreationUtils.makeAnnotation("Lmy/StringAnn;", "foo", "bazz")),
            Collections.emptyList()));
    model.update(
        AsmCreationUtils.makeClassWithAnnotations(
            "my/Cls3",
            "my/Cls1",
            Collections.singletonList(AsmCreationUtils.makeAnnotation("Lmy/StringAnn;")),
            Collections.emptyList()));
  }

  @Test
  public void findStringProperty() {
    assertThat(model.annotationPropertyOfClass("my/Cls1", "Lmy/StringAnn;", "foo"))
        .isEqualTo("bar");
  }

  @Test
  public void findIntegerProperty() {
    assertThat(model.annotationPropertyOfClass("my/Cls1", "Lmy/IntAnn;", "foo")).isEqualTo(42);
  }

  @Test
  public void obtainingNonExistentProperty() {
    assertThat(model.annotationPropertyOfClass("my/Cls1", "Lmy/StringAnn;", "bar")).isNull();
  }

  @Test
  public void obtainingPropertyOfNonExistingAnnotation() {
    try {
      model.annotationPropertyOfClass("my/Cls1", "LUnexistingAnnotation;", "foo");
      fail();
    } catch (IllegalStateException e) {
      // Expected.
    }
  }

  @Test
  public void closureWithAnnotationProperties() {
    assertThat(model.closureWithAnnotationFilterAndValue("my/Cls2", "Lmy/StringAnn;", "foo"))
        .containsOnly(entry("my/Cls1", "bar"), entry("my/Cls2", "bazz"));
  }

  @Test
  public void closureWithAnnotationWithMissingProperties() {
    assertThat(model.closureWithAnnotationFilterAndValue("my/Cls3", "Lmy/StringAnn;", "foo"))
        .containsOnly(entry("my/Cls1", "bar"));
  }
}
