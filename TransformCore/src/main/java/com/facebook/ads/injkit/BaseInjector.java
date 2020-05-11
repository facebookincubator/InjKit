// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;
import java.util.function.Predicate;
import org.objectweb.asm.tree.ClassNode;

public abstract class BaseInjector implements Injector {
  private final Predicate<String> isPackageIgnored;

  protected BaseInjector(Predicate<String> isPackageIgnored) {
    this.isPackageIgnored = isPackageIgnored;
  }

  @Override
  public final void process(ClassNode clsNode, Model model) throws AnnotationProcessingException {
    if (isPackageIgnored.test(
        AsmNameUtils.packageJavaNameFromClassJavaName(
            AsmNameUtils.classInternalNameToJavaName(clsNode.name)))) {
      return;
    }

    if (shouldSkipClass(clsNode)) {
      return;
    }

    processImpl(clsNode, model);
  }

  protected abstract void processImpl(ClassNode clsNode, Model model)
      throws AnnotationProcessingException;

  private static boolean shouldSkipClass(ClassNode classNode) {
    return AsmNameUtils.classInternalNameToJavaName(classNode.name).endsWith(".package-info");
  }
}
