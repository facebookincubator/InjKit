// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;
import org.objectweb.asm.tree.ClassNode;

public interface Injector {
  void process(ClassNode clsNode, Model model) throws AnnotationProcessingException;
}
