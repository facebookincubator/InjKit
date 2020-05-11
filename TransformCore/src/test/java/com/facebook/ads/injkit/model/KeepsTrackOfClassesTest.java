// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.model;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class KeepsTrackOfClassesTest {

  private final Model model = ModelFactory.defaultFactory().make();

  @Test
  public void findAddedClass() {
    model.update(AsmCreationUtils.makeClass("foo/Bar", "java/lang/Object"));

    assertThat(model.knowsClass("foo/Bar")).isTrue();
    assertThat(model.superClassOf("foo/Bar")).isEqualTo("java/lang/Object");
  }

  @Test
  public void doesNotKnowNonAddedClass() {
    model.update(AsmCreationUtils.makeClass("foo/Bar", "java/lang/Object"));

    assertThat(model.knowsClass("java/lang/Object")).isFalse();
  }

  @Test
  public void hasInterfacesOfClassesWith0Interfaces() {
    model.update(AsmCreationUtils.makeClass("a", "b"));

    assertThat(model.interfacesOf("a")).isEmpty();
  }

  @Test
  public void hasInterfacesOfClassesWith2Interfaces() {
    model.update(AsmCreationUtils.makeClass("a", "b", "c", "d"));

    assertThat(model.interfacesOf("a")).containsExactly("c", "d");
  }

  @Test
  public void closureReturnClassAndSuperClasses() {
    model.update(AsmCreationUtils.makeClass("a", "xxx"));
    model.update(AsmCreationUtils.makeClass("b", "a"));
    model.update(AsmCreationUtils.makeClass("c", "b"));
    model.update(AsmCreationUtils.makeClass("d", "c"));
    model.update(AsmCreationUtils.makeClass("e", "yyy"));

    assertThat(model.hierarchicalClosure("c")).containsExactly("a", "b", "c", "xxx");
  }

  @Test
  public void closureReturnsInterfaces() {
    model.update(AsmCreationUtils.makeClass("a", "b", "c", "d", "e"));
    model.update(AsmCreationUtils.makeClass("c", "o", "x", "z"));
    model.update(AsmCreationUtils.makeClass("d", "o", "y"));

    assertThat(model.hierarchicalClosure("a"))
        .containsExactly("a", "b", "c", "d", "e", "o", "x", "y", "z");
  }

  @Test
  public void canUpdateSameClassTwice() {
    model.update(AsmCreationUtils.makeClass("a", "b"));
    model.update(AsmCreationUtils.makeClass("a", "b"));
  }

  @Test
  public void cannotGetSuperOnUnknownClass() {
    try {
      model.superClassOf("a");
      fail();
    } catch (IllegalStateException e) {
      // Expected.
    }
  }
}
