/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;
import org.objectweb.asm.tree.ClassNode;

public class NopInjector extends BaseInjector {

  public NopInjector() {
    super((__) -> true);
  }

  @Override
  protected void processImpl(ClassNode clsNode, Model model) throws AnnotationProcessingException {}
}
