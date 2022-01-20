// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class KeepsTrackOfClassAnnotationsTest {

  private final Model model = new ModelImpl();

  @Before
  public void before() {
    model.update(
        AsmCreationUtils.makeClass(
            "foo/Bar",
            "java/lang/Object",
            Collections.singletonList("Lmy/Ann1;"),
            Collections.singletonList("Lmy/Ann2;")));
    model.update(
        AsmCreationUtils.makeClass(
            "foo/Intf",
            "java/lang/Object",
            Collections.singletonList("Lmy/AnnI1;"),
            Collections.singletonList("Lmy/AnnI2;")));
    model.update(AsmCreationUtils.makeClass("foo/Drink", "foo/Bar", "foo/Intf"));
  }

  @Test
  public void findsVisibleAnnotationInClass() {
    assertThat(model.annotationsOfClass("foo/Bar")).contains("Lmy/Ann1;");
  }

  @Test
  public void findsInvisibleAnnotationInClass() {
    assertThat(model.annotationsOfClass("foo/Bar")).contains("Lmy/Ann2;");
  }

  @Test
  public void doesNotFindNonExistentAnnotationInClass() {
    assertThat(model.annotationsOfClass("foo/Bar")).doesNotContain("my/Ann3");
  }

  @Test
  public void superClassAnnotationsNotReported() {
    assertThat(model.annotationsOfClass("foo/Drink")).doesNotContain("Lmy/Ann1;");
  }

  @Test
  public void superClassVisibleAnnotationsFoundWhenLookingInClosure() {
    assertThat(model.closureWithAnnotationFilter("foo/Drink", "Lmy/Ann1;")).containsOnly("foo/Bar");
  }

  @Test
  public void superClassInvisibleAnnotationsFoundWhenLookingInClosure() {
    assertThat(model.closureWithAnnotationFilter("foo/Drink", "Lmy/Ann2;")).containsOnly("foo/Bar");
  }

  @Test
  public void interfaceVisibleAnnotationsFoundWhenLookingInClosure() {
    assertThat(model.closureWithAnnotationFilter("foo/Drink", "Lmy/AnnI1;"))
        .containsOnly("foo/Intf");
  }

  @Test
  public void interfaceInvisibleAnnotationsFoundWhenLookingInClosure() {
    assertThat(model.closureWithAnnotationFilter("foo/Drink", "Lmy/AnnI2;"))
        .containsOnly("foo/Intf");
  }

  @Test
  public void nonExistentAnnotationsNotFoundWhenLookingInClosure() {
    assertThat(model.closureWithAnnotationFilter("foo/Drink", "my/Ann3")).isEmpty();
  }
}
