/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

class KnownClass {
  private final int access;
  private final String iName;
  private final String superIName;
  private final List<String> interfaceINames;
  private final Set<KnownAnnotation> annotations;
  private final List<KnownMethod> methods;

  KnownClass(ClassNode node) {
    access = node.access;
    iName = node.name;
    superIName = node.superName;
    interfaceINames = copyList(node.interfaces);
    annotations = KnownAnnotation.from(node.visibleAnnotations, node.invisibleAnnotations);
    methods = copyList(node.methods).stream().map(KnownMethod::new).collect(Collectors.toList());
  }

  String getSuperIName() {
    return superIName;
  }

  List<String> getInterfaceNames() {
    return new ArrayList<>(interfaceINames);
  }

  boolean isAnnotation() {
    return (access & Opcodes.ACC_ANNOTATION) != 0;
  }

  Set<String> getAnnotationDescriptions() {
    return annotations.stream().map(KnownAnnotation::getDescription).collect(Collectors.toSet());
  }

  private KnownMethod find(String name, String desc, int access) {
    for (KnownMethod method : methods) {
      if (method.getName().equals(name)
          && method.getDesc().equals(desc)
          && (method.getAccess() & Opcodes.ACC_PRIVATE) == (access & Opcodes.ACC_PRIVATE)
          && (method.getAccess() & Opcodes.ACC_STATIC) == (access & Opcodes.ACC_STATIC)) {
        return method;
      }
    }

    return null;
  }

  boolean hasMethod(String name, String desc, int access) {
    return find(name, desc, access) != null;
  }

  KnownMethod getKnown(String name, String desc, int access) {
    KnownMethod found = find(name, desc, access);
    if (found == null) {
      throw new IllegalStateException(
          String.format(
              Locale.US,
              "Class '%s' does not contain method %s%s with access %d",
              iName,
              name,
              desc,
              access));
    }

    return found;
  }

  KnownAnnotation getKnownAnnotation(String desc) {
    for (KnownAnnotation ann : annotations) {
      if (ann.getDescription().equals(desc)) {
        return ann;
      }
    }

    throw new IllegalStateException(
        String.format(
            Locale.US, "Class '%s' does not have annotation with descriptor '%s'", iName, desc));
  }

  Object getPropertyDefaultValue(String property) {
    for (KnownMethod m : methods) {
      if (m.getName().equals(property)) {
        Object dv = m.getAnnotationDefaultValue();
        if (dv != null) {
          return dv;
        }
      }
    }

    return null;
  }

  Map<String, Type> annotationTypes() {
    return methods.stream()
        .collect(Collectors.toMap(KnownMethod::getName, m -> Type.getReturnType(m.getDesc())));
  }

  @SuppressWarnings("unchecked")
  private static <T> List<T> copyList(List<T> list) {
    if (list == null) {
      return Collections.EMPTY_LIST;
    } else {
      return new ArrayList<>(list);
    }
  }
}
