// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class KeepsTrackOfMethodAnnotationsTest {

  private final Model model = new ModelImpl();

  @Before
  public void before() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass(
                "a/B",
                "c/D",
                Collections.singletonList("Lmy/Ann3;"),
                Collections.singletonList("Lmy/Ann4;")),
            AsmCreationUtils.makeMethod(
                "foo",
                "()V",
                Opcodes.ACC_PUBLIC,
                Collections.singletonList("Lmy/Ann1;"),
                Collections.singletonList("Lmy/Ann2;")),
            AsmCreationUtils.makeMethod(
                "bar",
                "(I)I",
                Opcodes.ACC_PRIVATE,
                Collections.singletonList("Lmy/Ann9;"),
                Collections.singletonList("Lmy/Ann10;")),
            AsmCreationUtils.makeMethod(
                "bazz",
                "(Z)Z",
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                Collections.singletonList("Lmy/Ann13;"),
                Collections.singletonList("Lmy/Ann14;"))));
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("e/F", "a/B", "g/H"),
            AsmCreationUtils.makeMethod(
                "foo",
                "()V",
                Opcodes.ACC_PUBLIC,
                Collections.singletonList("Lmy/Ann5;"),
                Collections.singletonList("Lmy/Ann6;")),
            AsmCreationUtils.makeMethod(
                "bar",
                "(I)I",
                Opcodes.ACC_PUBLIC,
                Collections.singletonList("Lmy/Ann11;"),
                Collections.singletonList("Lmy/Ann12;")),
            AsmCreationUtils.makeMethod("bazz", "(Z)Z", Opcodes.ACC_PUBLIC)));
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("g/H", "java/lang/Object"),
            AsmCreationUtils.makeMethod(
                "foo",
                "()V",
                Opcodes.ACC_PUBLIC,
                Collections.singletonList("Lmy/Ann7;"),
                Collections.singletonList("Lmy/Ann8;")),
            AsmCreationUtils.makeMethod(
                "bazz",
                "(Z)Z",
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                Collections.singletonList("Lmy/Ann15;"),
                Collections.singletonList("Lmy/Ann16;"))));
  }

  @Test
  public void findsVisibleAnnotationInMethod() {
    assertThat(model.annotationsOfMethod("a/B", "foo", "()V", Opcodes.ACC_PUBLIC))
        .contains("Lmy/Ann1;");
  }

  @Test
  public void findsInvisibleAnnotationInMethod() {
    assertThat(model.annotationsOfMethod("a/B", "foo", "()V", Opcodes.ACC_PUBLIC))
        .contains("Lmy/Ann2;");
  }

  @Test
  public void doesNotFindNonExistentAnnotationInMethod() {
    assertThat(model.annotationsOfMethod("a/B", "foo", "()V", Opcodes.ACC_PUBLIC))
        .doesNotContain("LXXX;");
  }

  @Test
  public void classAnnotationsNotReportedInMethod() {
    assertThat(model.annotationsOfMethod("a/B", "foo", "()V", Opcodes.ACC_PUBLIC))
        .doesNotContain("Lmy/Ann3;");
    assertThat(model.annotationsOfMethod("a/B", "foo", "()V", Opcodes.ACC_PUBLIC))
        .doesNotContain("Lmy/Ann4;");
  }

  @Test
  public void overriddenMethodAnnotationsFromSuperClassAnnotationsReportedInClosure() {
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "foo", "()V", Opcodes.ACC_PUBLIC, "Lmy/Ann1;"))
        .containsOnly("a/B");
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "foo", "()V", Opcodes.ACC_PUBLIC, "Lmy/Ann2;"))
        .containsOnly("a/B");
  }

  @Test
  public void overriddenMethodAnnotationsFromInterfaceAnnotationsReportedInClosure() {
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "foo", "()V", Opcodes.ACC_PUBLIC, "Lmy/Ann7;"))
        .containsOnly("g/H");
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "foo", "()V", Opcodes.ACC_PUBLIC, "Lmy/Ann8;"))
        .containsOnly("g/H");
  }

  @Test
  public void privateMethodsFromSuperClassNotReportedInClosure() {
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "bar", "(I)I", Opcodes.ACC_PUBLIC, "Lmy/Ann9;"))
        .isEmpty();
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "bar", "(I)I", Opcodes.ACC_PUBLIC, "Lmy/Ann10;"))
        .isEmpty();
  }

  @Test
  public void staticMethodsFromSuperClassNotReportedInClosure() {
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "bazz", "(Z)Z", Opcodes.ACC_PUBLIC, "Lmy/Ann13;"))
        .isEmpty();
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "bazz", "(Z)Z", Opcodes.ACC_PUBLIC, "Lmy/Ann14;"))
        .isEmpty();
  }

  @Test
  public void staticMethodsFromInterfaceNotReportedInClosure() {
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "bazz", "(Z)Z", Opcodes.ACC_PUBLIC, "Lmy/Ann15;"))
        .isEmpty();
    assertThat(
            model.methodClosureWithAnnotationFilter(
                "e/F", "bazz", "(Z)Z", Opcodes.ACC_PUBLIC, "Lmy/Ann16;"))
        .isEmpty();
  }

  @Test
  public void cannotGetAnnotationsOfUnknownMethod() {
    try {
      model.annotationsOfMethod("a/B", "noMethod", "()V", Opcodes.ACC_PROTECTED);
      fail();
    } catch (IllegalStateException e) {
      // Expected.
    }
  }
}
