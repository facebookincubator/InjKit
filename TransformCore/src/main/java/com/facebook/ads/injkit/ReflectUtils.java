/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ReflectUtils {
  private ReflectUtils() {}

  public static Class<?> checkClassExistsAndIsPublic(ClassLoader loader, String className)
      throws InvalidAnnotationProcessorConfigurationException {
    Class<?> cls;
    try {
      cls = loader.loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(Locale.US, "Class '%s' was not found", className));
    }

    if (!Modifier.isPublic(cls.getModifiers())) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(Locale.US, "Class '%s' is not public", className));
    }

    return cls;
  }

  public static void checkMethodIsPublicStatic(Class<?> cls, String name, Class<?>... params)
      throws InvalidAnnotationProcessorConfigurationException {
    StringBuilder paramsStr = new StringBuilder();
    for (Class<?> param : params) {
      if (paramsStr.length() > 0) {
        paramsStr.append(", ");
      }

      paramsStr.append(param.getName());
    }

    try {
      Method handleThrowable = cls.getDeclaredMethod(name, params);
      if (!Modifier.isStatic(handleThrowable.getModifiers())
          || !Modifier.isPublic(handleThrowable.getModifiers())) {
        throw new InvalidAnnotationProcessorConfigurationException(
            String.format(
                Locale.US,
                "Method void %s(%s) of class '%s' is not public static",
                name,
                paramsStr.toString(),
                cls.getName()));
      }
    } catch (NoSuchMethodException e) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(
              Locale.US,
              "Class '%s' does not have method 'void %s(%s)'",
              cls.getName(),
              name,
              paramsStr.toString()));
    }
  }

  static Executable mapMethod(Executable method, Class<?> inClass) {
    if (inClass == null) {
      return null;
    }

    try {
      if (Modifier.isStatic(method.getModifiers())) {
        return null;
      }

      if (method instanceof Method) {
        Method m = inClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (Modifier.isPrivate(m.getModifiers()) || Modifier.isStatic(m.getModifiers())) {
          return null;
        }

        return m;
      } else {
        // Constructors are not mapped.
        return null;
      }

    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  static Class<?> findClass(ClassNode node, ClassLoader loader)
      throws AnnotationProcessingException {
    try {
      return loader.loadClass(AsmNameUtils.classInternalNameToJavaName(node.name));
    } catch (ClassNotFoundException e) {
      throw new AnnotationProcessingException(
          String.format(Locale.US, "Failed to load class '%s'", node.name));
    }
  }

  public static Executable findMethod(
      ClassNode classNode, MethodNode methodNode, ClassLoader loader)
      throws AnnotationProcessingException {
    Class<?> cls = findClass(classNode, loader);

    try {
      if (methodNode.name.equals(AsmNameUtils.INIT)) {
        return cls.getConstructor(methodArgumentTypes(methodNode, loader));
      }

      return cls.getDeclaredMethod(methodNode.name, methodArgumentTypes(methodNode, loader));
    } catch (NoSuchMethodException e) {
      throw new AnnotationProcessingException(
          String.format(
              Locale.US,
              "Failed to find method '%s%s' in class '%s'",
              methodNode.name,
              methodNode.desc,
              classNode.name),
          e);
    }
  }

  private static Class<?>[] methodArgumentTypes(MethodNode method, ClassLoader loader)
      throws AnnotationProcessingException {
    List<Class<?>> types = new ArrayList<>();
    for (Type type : Type.getArgumentTypes(method.desc)) {
      switch (type.getSort()) {
        case Type.BOOLEAN:
          types.add(boolean.class);
          break;
        case Type.BYTE:
          types.add(byte.class);
          break;
        case Type.CHAR:
          types.add(char.class);
          break;
        case Type.DOUBLE:
          types.add(double.class);
          break;
        case Type.FLOAT:
          types.add(float.class);
          break;
        case Type.INT:
          types.add(int.class);
          break;
        case Type.LONG:
          types.add(long.class);
          break;
        case Type.SHORT:
          types.add(short.class);
          break;
        case Type.ARRAY:
        case Type.OBJECT:
          try {
            types.add(Class.forName(type.getInternalName().replace('/', '.'), false, loader));
          } catch (ClassNotFoundException e) {
            throw new AnnotationProcessingException(
                String.format(Locale.US, "Failed to load type '%s'", type.getInternalName()), e);
          }

          break;
        default:
          throw new AnnotationProcessingException(
              String.format(Locale.US, "Unknown type: %d", type.getSort()));
      }
    }

    return types.toArray(new Class<?>[0]);
  }

  public static Class<? extends Annotation> loadAnnotation(String className, ClassLoader loader)
      throws InvalidAnnotationProcessorConfigurationException {
    Class<?> cls;

    try {
      cls = loader.loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(Locale.US, "Class '%s' not found", className));
    }

    if (!Annotation.class.isAssignableFrom(cls)) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(Locale.US, "Class '%s' is not an annotation", className));
    }

    @SuppressWarnings("unchecked") // Guaranteed by condition above.
    Class<? extends Annotation> annCls = (Class<? extends Annotation>) cls;
    return annCls;
  }
}
