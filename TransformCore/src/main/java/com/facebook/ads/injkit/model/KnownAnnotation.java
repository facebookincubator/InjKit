// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.model;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class KnownAnnotation {
    private final String desc;
    private final Map<String, Object> values;

    private KnownAnnotation(AnnotationNode node) {
        desc = node.desc;
        values = new HashMap<>();
        for (int i = 0; node.values != null && i < (node.values.size() - 1); i+= 2) {
            values.put((String) node.values.get(i), node.values.get(i + 1));
        }
    }

    String getDescription() {
        return desc;
    }

    Object getValue(String key) {
        return values.get(key);
    }

    static Set<KnownAnnotation> from(
            List<AnnotationNode> visible,
            List<AnnotationNode> invisible) {
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
