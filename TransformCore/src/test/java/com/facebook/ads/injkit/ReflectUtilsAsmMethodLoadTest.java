/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@RunWith(Parameterized.class)
public class ReflectUtilsAsmMethodLoadTest {
  private static final String CONSTRUCTOR = "<init>";
  private static final String CLASS_INITIALIZER = "<clinit>";

  @Parameterized.Parameter(0)
  public String methodName;

  @Parameterized.Parameter(1)
  public String methodDesc;

  @Nullable
  @Parameterized.Parameter(2)
  public Class<?>[] argumentTypes;

  @Parameterized.Parameters
  public static List<Object[]> getParameters() {
    return Arrays.asList(
        new Object[][] {
          {"booleanMethod", "(Z)V", new Class<?>[] {boolean.class}},
          {"intMethod", "(BSJ)I", new Class<?>[] {byte.class, short.class, long.class}},
          {"floatMethod", "(DF)V", new Class<?>[] {double.class, float.class}},
          {"charMethod", "(CCI)C", new Class<?>[] {char.class, char.class, int.class}},
          {"arrayMethod1", "([I)[I", new Class<?>[] {int[].class}},
          {"arrayMethod2", "([[Ljava/lang/String;)V", new Class<?>[] {String[][].class}},
          {CONSTRUCTOR, "()V", new Class<?>[0]},
          {CLASS_INITIALIZER, "()V", new Class<?>[0]},
        });
  }

  @Test
  public void test() throws Exception {
    ClassNode classNode = new ClassNode();
    classNode.name = AsmNameUtils.classJavaNameToInternalName(TestClass.class.getName());
    MethodNode methodNode = new MethodNode();
    methodNode.name = methodName;
    methodNode.desc = methodDesc;

    Executable foundMethod;
    try {
      foundMethod =
          ReflectUtils.findMethod(classNode, methodNode, TestClass.class.getClassLoader());
      assertThat(foundMethod).isNotNull();
    } catch (AnnotationProcessingException e) {
      foundMethod = null;
    }

    if (argumentTypes == null) {
      assertThat(foundMethod).isNull();
    } else {
      Object expected;
      if (methodName.equals(CONSTRUCTOR)) {
        expected = TestClass.class.getConstructor(argumentTypes);
      } else if (methodName.equals(CLASS_INITIALIZER)) {
        expected = null;
      } else {
        expected = TestClass.class.getDeclaredMethod(methodName, argumentTypes);
      }

      assertThat(foundMethod).isEqualTo(expected);
    }
  }

  public static class TestClass {
    void booleanMethod(boolean b) {}

    int intMethod(byte b, short s, long l) {
      return 0;
    }

    void floatMethod(double f1, float f2) {}

    char charMethod(char c0, char c1, int c2) {
      return c0;
    }

    int[] arrayMethod1(int[] x) {
      return x;
    }

    void arrayMethod2(String[][] x) {}
  }
}
