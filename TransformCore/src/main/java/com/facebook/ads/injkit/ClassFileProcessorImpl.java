/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;
import com.facebook.infer.annotation.Nullsafe;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

@Nullsafe(Nullsafe.Mode.LOCAL)
class ClassFileProcessorImpl implements ClassFileProcessor {
  private final URLClassLoader classLoader;
  private final Collection<Injector> injectors;
  private final Model model;

  ClassFileProcessorImpl(
      AnnotationProcessorConfiguration configuration, URLClassLoader classLoader, Model model)
      throws InvalidAnnotationProcessorConfigurationException {
    this.classLoader = classLoader;
    this.injectors = new ArrayList<>(configuration.makeInjectors(classLoader));
    this.model = model;
  }

  @Override
  public void process(InputStream input, OutputStream output)
      throws IOException, AnnotationProcessingException {
    ClassReader classReader = new ClassReader(input);
    ClassNode node = new ClassNode();
    classReader.accept(node, 0);

    transformClassNode(node);

    ClassWriter classWriter =
        new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
          @Override
          protected ClassLoader getClassLoader() {
            return classLoader;
          }

          @Override
          protected String getCommonSuperClass(String type1, String type2) {
            Class<?> class1;
            try {
              class1 = classLoader.loadClass(AsmNameUtils.classInternalNameToJavaName(type1));
            } catch (Exception e) {
              throw new TypeNotPresentException(type1, e);
            }
            Class<?> class2;
            try {
              class2 = classLoader.loadClass(AsmNameUtils.classInternalNameToJavaName(type2));
            } catch (Exception e) {
              throw new TypeNotPresentException(type2, e);
            }
            if (class1.isAssignableFrom(class2)) {
              return type1;
            }
            if (class2.isAssignableFrom(class1)) {
              return type2;
            }
            if (class1.isInterface() || class2.isInterface()) {
              return "java/lang/Object";
            } else {
              do {
                // NULLSAFE_FIXME[Not Vetted Third-Party]
                class1 = class1.getSuperclass();
              } while (!class1.isAssignableFrom(class2));

              return AsmNameUtils.classJavaNameToInternalName(class1.getName());
            }
          }
        };
    node.accept(classWriter);
    output.write(classWriter.toByteArray());
  }

  @Override
  public void updateModel(InputStream input, Model model)
      throws IOException, AnnotationProcessingException {
    ClassReader classReader = new ClassReader(input);
    ClassNode node = new ClassNode();
    classReader.accept(node, 0);

    model.update(node);
  }

  private void transformClassNode(ClassNode node) throws AnnotationProcessingException {
    for (Injector injector : injectors) {
      injector.process(node, model);
    }
  }

  private void preprocessClassNode(ClassNode node) {
    model.update(node);
  }
}
