/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;
import com.facebook.infer.annotation.Nullsafe;
import java.util.function.Predicate;
import org.objectweb.asm.tree.ClassNode;

@Nullsafe(Nullsafe.Mode.LOCAL)
public abstract class BaseInjector implements Injector {
  private final Predicate<String> isPackageIgnored;

  protected BaseInjector(Predicate<String> isPackageIgnored) {
    this.isPackageIgnored = isPackageIgnored;
  }

  @Override
  public final void process(ClassNode clsNode, Model model) throws AnnotationProcessingException {
    if (isPackageIgnored.test(
        AsmNameUtils.packageJavaNameFromClassJavaName(
            // NULLSAFE_FIXME[Not Vetted Third-Party]
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
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    return AsmNameUtils.classInternalNameToJavaName(classNode.name).endsWith(".package-info");
  }
}
