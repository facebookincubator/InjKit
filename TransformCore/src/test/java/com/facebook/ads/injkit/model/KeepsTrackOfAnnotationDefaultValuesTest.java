/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@RunWith(JUnit4.class)
public class KeepsTrackOfAnnotationDefaultValuesTest {
  private final Model model = ModelFactory.defaultFactory().make();

  @Before
  public void before() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeAnnotationClass("my/Ann"),
            AsmCreationUtils.makeMethod(
                "aString",
                "()Ljava/lang/String;",
                Opcodes.ACC_PUBLIC,
                Collections.emptyList(),
                Collections.emptyList(),
                "bar"),
            AsmCreationUtils.makeMethod(
                "aInt",
                "()I",
                Opcodes.ACC_PUBLIC,
                Collections.emptyList(),
                Collections.emptyList(),
                42)));
    model.update(AsmCreationUtils.makeClass("my/Cls", "java/lang/Object"));
  }

  @Test
  public void knowsPresentedAnnotation() {
    assertThat(model.knowsAnnotation("Lmy/Ann;")).isTrue();
  }

  @Test
  public void doesNotKnowNonPresentedAnnotation() {
    assertThat(model.knowsAnnotation("Lmy/AnnX;")).isFalse();
  }

  @Test
  public void obtainsStringValueOfPropertyOfPresentedAnnotationThatHasValue() {
    assertThat(model.annotationDefaultValue("Lmy/Ann;", "aString")).isEqualTo("bar");
  }

  @Test
  public void obtainsIntValueOfPropertyOfPresentedAnnotationThatHasValue() {
    assertThat(model.annotationDefaultValue("Lmy/Ann;", "aInt")).isEqualTo(42);
  }

  @Test
  public void canTryToGetNonExistentPropertyValueOfAnnotationThatIsKnown() {
    assertThat(model.annotationDefaultValue("Lmy/Ann;", "aaa")).isNull();
  }

  @Test
  public void cannotTryToGetPropertyValueOfNotKnownAnnotation() {
    assertThatThrownBy(() -> model.annotationPropertyOfClass("my/Cls", "Lmy/AnnX;", "aString"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void cannotTryToGetPropertyValueOfClassThatIsNotAnnotation() {
    assertThatThrownBy(() -> model.annotationDefaultValue("Lmy/Cls;", "aString"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void obtainsAnnotationPropertyTypes() {
    assertThat(model.annotationProperties("Lmy/Ann;"))
        .containsOnly(
            entry("aString", Type.getType("Ljava/lang/String;")), entry("aInt", Type.INT_TYPE));
  }

  @Test
  public void cannotObtainAnnotationPropertyTypesIfAnnotationNotKnown() {
    assertThatThrownBy(() -> model.annotationProperties("Lmy/AnnX;"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void cannotObtainAnnotationPropertyTypesIfClassIsNotAnnotation() {
    assertThatThrownBy(() -> model.annotationProperties("Lmy/cls;"))
        .isInstanceOf(IllegalStateException.class);
  }
}
