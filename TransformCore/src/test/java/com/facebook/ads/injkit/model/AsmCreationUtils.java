/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

class AsmCreationUtils {
  private AsmCreationUtils() {}

  static ClassNode makeClass(String iname, String superName, String... interfaces) {
    return makeClass(iname, superName, new ArrayList<>(), new ArrayList<>(), interfaces);
  }

  static ClassNode makeClass(
      String iname,
      String superName,
      List<String> visibleAnnotations,
      List<String> invisibleAnnotations,
      String... interfaces) {
    return makeClassWithAnnotations(
        iname,
        superName,
        visibleAnnotations.stream()
            .map(AsmCreationUtils::makeAnnotation)
            .collect(Collectors.toList()),
        invisibleAnnotations.stream()
            .map(AsmCreationUtils::makeAnnotation)
            .collect(Collectors.toList()),
        interfaces);
  }

  static ClassNode makeClassWithAnnotations(
      String iname,
      String superName,
      List<AnnotationNode> visibleAnnotations,
      List<AnnotationNode> invisibleAnnotations,
      String... interfaces) {
    ClassNode node = new ClassNode();
    node.name = iname;
    node.superName = superName;
    if (interfaces.length > 0) {
      node.interfaces = Arrays.asList(interfaces);
    }

    for (AnnotationNode ann : visibleAnnotations) {
      if (node.visibleAnnotations == null) {
        node.visibleAnnotations = new ArrayList<>();
      }

      node.visibleAnnotations.add(ann);
    }

    for (AnnotationNode ann : invisibleAnnotations) {
      if (node.invisibleAnnotations == null) {
        node.invisibleAnnotations = new ArrayList<>();
      }

      node.invisibleAnnotations.add(ann);
    }

    return node;
  }

  static ClassNode makeAnnotationClass(String iName) {
    ClassNode n = makeClass(iName, "java/lang/Object");
    n.access |= Opcodes.ACC_ANNOTATION;
    return n;
  }

  static ClassNode addMethods(ClassNode classNode, MethodNode... methods) {
    if (classNode.methods == null) {
      classNode.methods = new ArrayList<>();
    }

    classNode.methods.addAll(Arrays.asList(methods));
    return classNode;
  }

  static MethodNode makeMethod(String name, String desc, int access) {
    return makeMethod(name, desc, access, new ArrayList<>(), new ArrayList<>());
  }

  static MethodNode makeMethod(
      String name,
      String desc,
      int access,
      List<String> visibleAnnotations,
      List<String> invisibleAnnotations) {
    return makeMethod(name, desc, access, visibleAnnotations, invisibleAnnotations, null);
  }

  static MethodNode makeMethod(
      String name,
      String desc,
      int access,
      List<String> visibleAnnotations,
      List<String> invisibleAnnotations,
      Object defaultValue) {
    return makeMethodWithAnnotations(
        name,
        desc,
        access,
        visibleAnnotations.stream()
            .map(AsmCreationUtils::makeAnnotation)
            .collect(Collectors.toList()),
        invisibleAnnotations.stream()
            .map(AsmCreationUtils::makeAnnotation)
            .collect(Collectors.toList()),
        defaultValue);
  }

  static MethodNode makeMethodWithAnnotations(
      String name,
      String desc,
      int access,
      List<AnnotationNode> visibleAnnotations,
      List<AnnotationNode> invisibleAnnotations,
      Object defaultValue) {
    MethodNode mn = new MethodNode(access, name, desc, "", null);
    mn.annotationDefault = defaultValue;

    for (AnnotationNode ann : visibleAnnotations) {
      if (mn.visibleAnnotations == null) {
        mn.visibleAnnotations = new ArrayList<>();
      }

      mn.visibleAnnotations.add(ann);
    }

    for (AnnotationNode ann : invisibleAnnotations) {
      if (mn.invisibleAnnotations == null) {
        mn.invisibleAnnotations = new ArrayList<>();
      }

      mn.invisibleAnnotations.add(ann);
    }

    return mn;
  }

  static AnnotationNode makeAnnotation(String desc, Object... properties) {
    AnnotationNode node = new AnnotationNode(desc);
    if (properties != null && properties.length > 0) {
      node.values = Arrays.asList(properties);
    }

    return node;
  }
}
