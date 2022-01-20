// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.Opcodes;

@RunWith(JUnit4.class)
public class KeepsTrackOfMethodsTest {

  private static final int PUB = Opcodes.ACC_PUBLIC;
  private static final int PRO = Opcodes.ACC_PROTECTED;
  private static final int DEF = 0;
  private static final int PRI = Opcodes.ACC_PRIVATE;
  private static final int ST = Opcodes.ACC_STATIC;
  private static final int PUB_ST = PUB | ST;
  private static final int PRO_ST = PRO | ST;
  private static final int DEF_ST = DEF | ST;
  private static final int PRI_ST = PRI | ST;

  private final Model model = ModelFactory.defaultFactory().make();

  @Before
  public void before() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("foo/Foo", "bar/Bar"),
            AsmCreationUtils.makeMethod("pubMet", "()V", PUB),
            AsmCreationUtils.makeMethod("pubSMet", "()V", PUB_ST),
            AsmCreationUtils.makeMethod("proMet", "()V", PRO),
            AsmCreationUtils.makeMethod("proSMet", "()V", PRO_ST),
            AsmCreationUtils.makeMethod("defMet", "()V", DEF),
            AsmCreationUtils.makeMethod("defSMet", "()V", DEF_ST),
            AsmCreationUtils.makeMethod("priMet", "()V", PRI),
            AsmCreationUtils.makeMethod("priSMet", "()V", PRI_ST)));
  }

  @Test
  public void doesNotFindMethodWithDifferentName() {
    assertThat(model.hasMethod("foo/Foo", "pubmet", "()V", PUB)).isFalse();
  }

  @Test
  public void doesNotFindMethodWithDifferentSignature() {
    assertThat(model.hasMethod("foo/Foo", "pubMet", "()I", PUB)).isFalse();
  }

  @Test
  public void findsPublicStaticMethod() {
    assertThat(model.hasMethod("foo/Foo", "pubSMet", "()V", PUB_ST)).isTrue();
  }

  @Test
  public void findsPrivateStaticMethod() {
    assertThat(model.hasMethod("foo/Foo", "priSMet", "()V", PRI_ST)).isTrue();
  }

  @Test
  public void findsPublicNonStaticMethod() {
    assertThat(model.hasMethod("foo/Foo", "pubMet", "()V", PUB)).isTrue();
  }

  @Test
  public void findsPrivateNonStaticMethod() {
    assertThat(model.hasMethod("foo/Foo", "priMet", "()V", PRI)).isTrue();
  }

  @Test
  public void doesNotFindMethodWithDifferentVisibility() {
    assertThat(model.hasMethod("foo/Foo", "pubMet", "()V", PRI)).isFalse();
    assertThat(model.hasMethod("foo/Foo", "priMet", "()V", PUB)).isFalse();
  }

  @Test
  public void doesNotFindMethodWithDifferentStatic() {
    assertThat(model.hasMethod("foo/Foo", "pubMet", "()V", PUB_ST)).isFalse();
    assertThat(model.hasMethod("foo/Foo", "pubSMet", "()V", PUB)).isFalse();
  }

  @Test
  public void methodClosureFindProtectedOverriddenByPublic() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sup", "java/lang/Object"),
            AsmCreationUtils.makeMethod("proMet", "()V", Opcodes.ACC_PROTECTED)));
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sub", "Sup"),
            AsmCreationUtils.makeMethod("proMet", "()V", Opcodes.ACC_PUBLIC)));

    assertThat(model.hierarchicalMethodClosure("Sub", "proMet", "()V", Opcodes.ACC_PUBLIC))
        .containsOnly("Sup", "Sub");
  }

  @Test
  public void methodClosureFindPackageOverriddenByPublic() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sup", "java/lang/Object"),
            AsmCreationUtils.makeMethod("pacMet", "()V", 0)));
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sub", "Sup"),
            AsmCreationUtils.makeMethod("pacMet", "()V", Opcodes.ACC_PUBLIC)));

    assertThat(model.hierarchicalMethodClosure("Sub", "pacMet", "()V", Opcodes.ACC_PUBLIC))
        .containsOnly("Sup", "Sub");
  }

  @Test
  public void methodClosureFindPackageOverriddenByProtected() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sup", "java/lang/Object"),
            AsmCreationUtils.makeMethod("pacMet", "()V", 0)));
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sub", "Sup"),
            AsmCreationUtils.makeMethod("pacMet", "()V", Opcodes.ACC_PROTECTED)));

    assertThat(model.hierarchicalMethodClosure("Sub", "pacMet", "()V", Opcodes.ACC_PROTECTED))
        .containsOnly("Sup", "Sub");
  }

  @Test
  public void methodClosureNotFindPrivateOverriddenByPublic() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sup", "java/lang/Object"),
            AsmCreationUtils.makeMethod("priMet", "()V", Opcodes.ACC_PRIVATE)));
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sub", "Sup"),
            AsmCreationUtils.makeMethod("priMet", "()V", Opcodes.ACC_PUBLIC)));

    assertThat(model.hierarchicalMethodClosure("Sub", "priMet", "()V", Opcodes.ACC_PUBLIC))
        .containsOnly("Sub");
  }

  @Test
  public void methodClosureNotFindPrivateOverriddenByPackage() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sup", "java/lang/Object"),
            AsmCreationUtils.makeMethod("priMet", "()V", Opcodes.ACC_PRIVATE)));
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sub", "Sup"),
            AsmCreationUtils.makeMethod("priMet", "()V", 0)));

    assertThat(model.hierarchicalMethodClosure("Sub", "priMet", "()V", 0)).containsOnly("Sub");
  }

  @Test
  public void methodClosureNotFindPrivateOverriddenByProtected() {
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sup", "java/lang/Object"),
            AsmCreationUtils.makeMethod("priMet", "()V", Opcodes.ACC_PRIVATE)));
    model.update(
        AsmCreationUtils.addMethods(
            AsmCreationUtils.makeClass("Sub", "Sup"),
            AsmCreationUtils.makeMethod("priMet", "()V", Opcodes.ACC_PROTECTED)));

    assertThat(model.hierarchicalMethodClosure("Sub", "priMet", "()V", Opcodes.ACC_PROTECTED))
        .containsOnly("Sub");
  }
}
