/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public interface Model {
  void update(ClassNode node);

  boolean knowsClass(String iName);

  String superClassOf(String iName);

  List<String> interfacesOf(String iName);

  Set<String> annotationsOfClass(String iName);

  Object annotationPropertyOfClass(String iName, String desc, String property);

  boolean hasMethod(String iName, String name, String desc, int access);

  Set<String> annotationsOfMethod(String iName, String name, String desc, int access);

  Object annotationPropertyOfMethod(
      String iName,
      String name,
      String methodDesc,
      int access,
      String annotationDesc,
      String property);

  boolean knowsAnnotation(String desc);

  Map<String, Type> annotationProperties(String desc);

  Object annotationDefaultValue(String desc, String property);

  default Set<String> hierarchicalClosure(String iName) {
    Set<String> done = new HashSet<>();
    Set<String> pending = new HashSet<>();
    pending.add(iName);

    while (!pending.isEmpty()) {
      String currentIName = pending.iterator().next();
      done.add(currentIName);

      if (knowsClass(currentIName)) {
        pending.add(superClassOf(currentIName));
        pending.addAll(interfacesOf(currentIName));
      }

      pending.removeAll(done);
    }

    return done;
  }

  default Set<String> hierarchicalMethodClosure(
      String iName, String name, String desc, int access) {
    return hierarchicalClosure(iName).stream()
        .filter(i -> knowsClass(i) && hasMethod(i, name, desc, access))
        .collect(Collectors.toSet());
  }

  default Set<String> closureWithAnnotationFilter(String iName, String desc) {
    return hierarchicalClosure(iName).stream()
        .filter(i -> knowsClass(i) && annotationsOfClass(i).contains(desc))
        .collect(Collectors.toSet());
  }

  default Set<String> methodClosureWithAnnotationFilter(
      String iName, String name, String methodDesc, int access, String annotationDesc) {
    return hierarchicalMethodClosure(iName, name, methodDesc, access).stream()
        .filter(i -> annotationsOfMethod(i, name, methodDesc, access).contains(annotationDesc))
        .collect(Collectors.toSet());
  }

  default Map<String, Object> closureWithAnnotationFilterAndValue(
      String iName, String desc, String property) {
    return closureWithAnnotationFilter(iName, desc).stream()
        .map(i -> new Object[] {i, annotationPropertyOfClass(i, desc, property)})
        .filter(i -> i[1] != null)
        .collect(Collectors.toMap(i -> (String) i[0], i -> i[1]));
  }

  default Map<String, Object> methodClosureWithAnnotationFilterAndValue(
      String iName,
      String name,
      String methodDesc,
      int access,
      String annotationDesc,
      String property) {
    return methodClosureWithAnnotationFilter(iName, name, methodDesc, access, annotationDesc)
        .stream()
        .map(
            i ->
                new Object[] {
                  i,
                  annotationPropertyOfMethod(i, name, methodDesc, access, annotationDesc, property)
                })
        .filter(i -> i[1] != null)
        .collect(Collectors.toMap(i -> (String) i[0], i -> i[1]));
  }
}
