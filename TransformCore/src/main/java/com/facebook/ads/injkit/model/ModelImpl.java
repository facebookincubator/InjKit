// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.model;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

class ModelImpl implements Model {
  // Known classes mapped by internal name.
  private final Map<String, KnownClass> knownClasses = new HashMap<>();

  @Override
  public void update(ClassNode node) {
    if (knownClasses.get(node.name) != null) {
      // If we already know the class, ignore it.
      return;
    }

    knownClasses.put(node.name, new KnownClass(node));
  }

  @Override
  public boolean knowsClass(String iName) {
    return knownClasses.containsKey(iName);
  }

  @Override
  public String superClassOf(String iName) {
    return getKnown(iName).getSuperIName();
  }

  @Override
  public List<String> interfacesOf(String iName) {
    return getKnown(iName).getInterfaceNames();
  }

  @Override
  public Set<String> annotationsOfClass(String iName) {
    return getKnown(iName).getAnnotationDescriptions();
  }

  @Override
  public Set<String> annotationsOfMethod(String iName, String name, String desc, int access) {
    return getKnown(iName).getKnown(name, desc, access).getAnnotationDescriptions();
  }

  @Override
  public boolean hasMethod(String iName, String name, String desc, int access) {
    return getKnown(iName).hasMethod(name, desc, access);
  }

  @Override
  public Object annotationPropertyOfClass(String iName, String desc, String property) {
    return getKnown(iName).getKnownAnnotation(desc).getValue(property);
  }

  @Override
  public Object annotationPropertyOfMethod(
      String iName,
      String name,
      String methodDesc,
      int access,
      String annotationDesc,
      String property) {
    return getKnown(iName)
        .getKnown(name, methodDesc, access)
        .getKnownAnnotation(annotationDesc)
        .getValue(property);
  }

  @Override
  public boolean knowsAnnotation(String desc) {
    String iName = Type.getType(desc).getInternalName();
    return knowsClass(iName) && knownClasses.get(iName).isAnnotation();
  }

  @Override
  public Map<String, Type> annotationProperties(String desc) {
    return getKnownAnnotation(desc).annotationTypes();
  }

  @Override
  public Object annotationDefaultValue(String desc, String property) {
    return getKnownAnnotation(desc).getPropertyDefaultValue(property);
  }

  private KnownClass getKnown(String iName) {
    KnownClass cls = knownClasses.get(iName);
    if (cls == null) {
      throw new IllegalStateException(
          String.format(Locale.US, "Class '%s' is not known to the model", iName));
    }

    return cls;
  }

  private KnownClass getKnownAnnotation(String desc) {
    KnownClass cls = getKnown(Type.getType(desc).getInternalName());
    if (!cls.isAnnotation()) {
      throw new IllegalArgumentException(
          String.format(Locale.US, "Class %s is not an annotation", desc));
    }

    return cls;
  }
}
