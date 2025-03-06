/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.model;

import com.facebook.infer.annotation.Nullsafe;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AnnotationNode;

@Nullsafe(Nullsafe.Mode.LOCAL)
class KnownAnnotation {
  private final String desc;
  private final Map<String, Object> values;

  private KnownAnnotation(AnnotationNode node) {
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    desc = node.desc;
    values = new HashMap<>();
    for (int i = 0; node.values != null && i < (node.values.size() - 1); i += 2) {
      values.put((String) node.values.get(i), node.values.get(i + 1));
    }
  }

  String getDescription() {
    return desc;
  }

  Object getValue(String key) {
    // NULLSAFE_FIXME[Return Not Nullable]
    return values.get(key);
  }

  static Set<KnownAnnotation> from(
      @Nullable List<AnnotationNode> visible, @Nullable List<AnnotationNode> invisible) {
    Set<KnownAnnotation> annotations = new HashSet<>();
    if (visible != null) {
      for (AnnotationNode annotationNode : visible) {
        annotations.add(new KnownAnnotation(annotationNode));
      }
    }

    if (invisible != null) {
      for (AnnotationNode annotationNode : invisible) {
        annotations.add(new KnownAnnotation(annotationNode));
      }
    }

    return annotations;
  }
}
