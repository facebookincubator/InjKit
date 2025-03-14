/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.model;

import com.facebook.infer.annotation.Nullsafe;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.objectweb.asm.tree.MethodNode;

@Nullsafe(Nullsafe.Mode.LOCAL)
class KnownMethod {
  private final String name;
  private final String desc;
  private final int access;
  private final Set<KnownAnnotation> annotations;
  private final Object annotationDefaultValue;

  KnownMethod(MethodNode methodNode) {
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    name = methodNode.name;
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    desc = methodNode.desc;
    access = methodNode.access;
    annotations =
        KnownAnnotation.from(methodNode.visibleAnnotations, methodNode.invisibleAnnotations);
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    annotationDefaultValue = methodNode.annotationDefault;
  }

  String getName() {
    return name;
  }

  String getDesc() {
    return desc;
  }

  int getAccess() {
    return access;
  }

  Set<String> getAnnotationDescriptions() {
    return annotations.stream().map(KnownAnnotation::getDescription).collect(Collectors.toSet());
  }

  Object getAnnotationDefaultValue() {
    return annotationDefaultValue;
  }

  KnownAnnotation getKnownAnnotation(String desc) {
    for (KnownAnnotation ann : annotations) {
      if (ann.getDescription().equals(desc)) {
        return ann;
      }
    }

    throw new IllegalStateException(
        String.format(
            Locale.US,
            "Method '%s%s' (access %d) does not have annotation with descriptor '%s'",
            name,
            this.desc,
            access,
            desc));
  }
}
