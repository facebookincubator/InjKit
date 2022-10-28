/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.tree.ClassNode;

@RunWith(JUnit4.class)
public class ReflectUtilsAsmClassLoadTest {

  @Test
  public void loadJavaClass() throws Exception {
    ClassNode node = new ClassNode();
    node.name = AsmNameUtils.classJavaNameToInternalName(TestClass.class.getName());
    Class<?> cls =
        ReflectUtils.findClass(node, ReflectUtilsAsmClassLoadTest.class.getClassLoader());

    assertThat(cls).isSameAs(TestClass.class);
  }

  @Test
  public void loadNonExistentJavaClass() throws Exception {
    ClassNode node = new ClassNode();
    node.name = AsmNameUtils.classJavaNameToInternalName(TestClass.class.getName()) + "_";

    try {
      ReflectUtils.findClass(node, ReflectUtilsAsmClassLoadTest.class.getClassLoader());
      fail();
    } catch (AnnotationProcessingException e) {
      assertThat(e.getMessage()).contains(node.name);
    }
  }

  static class TestClass {}
}
