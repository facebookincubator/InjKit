/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.threadcheck;

import com.facebook.ads.injkit.AnnotationProcessingException;
import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.BaseInjector;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.ReflectUtils;
import com.facebook.ads.injkit.model.Model;
import com.facebook.infer.annotation.Nullsafe;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@Nullsafe(Nullsafe.Mode.LOCAL)
class ThreadCheckInjector extends BaseInjector {
  private static final String ANDROID_LOOPER = "android/os/Looper";
  private static final String GET_CURRENT_LOOPER = "myLooper";
  private static final String GET_MAIN_LOOPER = "getMainLooper";
  private static final String WORKER_VIOLATION_METHOD_NAME = "workerThreadViolationDetected";
  private static final String UI_VIOLATION_METHOD_NAME = "uiThreadViolationDetected";
  private static final String VIOLATION_METHOD_DESC =
      "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)V";

  // Overridable for tests.
  static String androidLooper = ANDROID_LOOPER;

  private final ThreadCheckConfiguration config;

  ThreadCheckInjector(ThreadCheckConfiguration config, ClassLoader applicationCode)
      throws InvalidAnnotationProcessorConfigurationException {
    super(__ -> false);

    this.config = config;

    if (config.isEnabled()) {
      Class violationHandlerClass =
          ReflectUtils.checkClassExistsAndIsPublic(
              applicationCode, config.getViolationHandlerClass());

      ReflectUtils.checkMethodIsPublicStatic(
          violationHandlerClass, UI_VIOLATION_METHOD_NAME, Class.class, String.class, String.class);

      ReflectUtils.checkMethodIsPublicStatic(
          violationHandlerClass,
          WORKER_VIOLATION_METHOD_NAME,
          Class.class,
          String.class,
          String.class);
    }
  }

  @Override
  protected void processImpl(ClassNode clsNode, Model model) throws AnnotationProcessingException {
    if (clsNode.methods == null) {
      return;
    }

    boolean isInterface = ((clsNode.access & Opcodes.ACC_INTERFACE) != 0);

    Checking defaultChecking = computeClassChecking(clsNode, model);
    removeChecking(clsNode);

    for (MethodNode method : clsNode.methods) {
      boolean isMethodAbstract = ((method.access & Opcodes.ACC_ABSTRACT) != 0);
      processMethod(clsNode, method, defaultChecking, !isInterface && !isMethodAbstract, model);
    }
  }

  private void processMethod(
      ClassNode classNode,
      MethodNode method,
      Checking defaultChecking,
      boolean allowCodeInjection,
      Model model)
      throws AnnotationProcessingException {
    // Skip class initialization methods because we cannot override their thread check
    // parameters.
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    if (method.name.equals(AsmNameUtils.CL_INIT)) {
      return;
    }

    // Remove the annotations even if disabled.
    removeChecking(method);

    Checking checking = computeMethodChecking(classNode, method, model).orDefault(defaultChecking);
    if (checking == Checking.NOT_DEFINED
        || checking == Checking.IS_ANY
        || !config.isEnabled()
        || !allowCodeInjection) {
      return;
    }

    addLooperCheck(classNode, method, checking == Checking.IS_UI);
  }

  private void removeChecking(ClassNode classNode) {
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    removeChecking(classNode.visibleAnnotations, classNode.invisibleAnnotations);
  }

  private void removeChecking(MethodNode methodNode) {
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    removeChecking(methodNode.visibleAnnotations, methodNode.invisibleAnnotations);
  }

  private void removeChecking(List<AnnotationNode> visible, List<AnnotationNode> invisible) {
    removeAnnotation(visible, invisible, config.getUiThreadAnnotationClass());
    removeAnnotation(visible, invisible, config.getWorkerThreadAnnotationClass());
    removeAnnotation(visible, invisible, config.getAnyThreadAnnotationClass());
  }

  private static void removeAnnotation(
      List<AnnotationNode> visible, List<AnnotationNode> invisible, String annotationClassName) {
    if (annotationClassName == null) {
      return;
    }

    removeAnnotation(visible, AsmNameUtils.classJavaNameToDescriptor(annotationClassName));
    removeAnnotation(invisible, AsmNameUtils.classJavaNameToDescriptor(annotationClassName));
  }

  private static void removeAnnotation(List<AnnotationNode> annotations, String desc) {
    if (annotations == null) {
      return;
    }

    for (Iterator<AnnotationNode> it = annotations.iterator(); it.hasNext(); ) {
      AnnotationNode node = it.next();
      if (desc.equals(node.desc)) {
        it.remove();
        return;
      }
    }
  }

  private void addLooperCheck(
      ClassNode classNode, MethodNode method, boolean checkForPositiveLooperMatch) {
    if (method.instructions == null) {
      method.instructions = new InsnList();
    }

    InsnList insns = new InsnList();

    invokeStatic(insns, androidLooper, GET_CURRENT_LOOPER, "()L" + androidLooper + ";");
    invokeStatic(insns, androidLooper, GET_MAIN_LOOPER, "()L" + androidLooper + ";");

    LabelNode conditionOk = new LabelNode(new Label());
    insns.add(
        new JumpInsnNode(
            checkForPositiveLooperMatch ? Opcodes.IF_ACMPEQ : Opcodes.IF_ACMPNE, conditionOk));

    // Condition not OK:

    insns.add(new LdcInsnNode(Type.getType("L" + classNode.name + ";")));
    insns.add(new LdcInsnNode(method.name));
    insns.add(new LdcInsnNode(method.desc));
    invokeStatic(
        insns,
        AsmNameUtils.classJavaNameToInternalName(config.getViolationHandlerClass()),
        checkForPositiveLooperMatch ? UI_VIOLATION_METHOD_NAME : WORKER_VIOLATION_METHOD_NAME,
        VIOLATION_METHOD_DESC);

    // Condition OK:

    insns.add(conditionOk);

    method.instructions.insert(insns);
  }

  private static void invokeStatic(InsnList list, String iname, String method, String desc) {
    list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, iname, method, desc, false));
  }

  private static Set<String> annotationClosure(String className, ClassNode node, Model model) {
    if (className == null) {
      return new HashSet<>();
    }

    return model.closureWithAnnotationFilter(
        // NULLSAFE_FIXME[Not Vetted Third-Party]
        node.name, AsmNameUtils.classJavaNameToDescriptor(className));
  }

  private Checking computeClassChecking(ClassNode node, Model model)
      throws AnnotationProcessingException {
    Set<String> ui = annotationClosure(config.getUiThreadAnnotationClass(), node, model);
    Set<String> work = annotationClosure(config.getWorkerThreadAnnotationClass(), node, model);
    Set<String> any = annotationClosure(config.getAnyThreadAnnotationClass(), node, model);

    if ((ui.isEmpty() ? 0 : 1) + (work.isEmpty() ? 0 : 1) + (any.isEmpty() ? 0 : 1) > 1) {
      throw new AnnotationProcessingException(
          String.format(
              Locale.US,
              "Inconsistent annotations in class hierarchy: [%s] have UI annotation "
                  + "(%s), [%s] have worker annotation (%s) and [%s] have any "
                  + "annotation (%s)",
              String.join(",", ui),
              config.getUiThreadAnnotationClass(),
              String.join(",", work),
              config.getWorkerThreadAnnotationClass(),
              String.join(",", any),
              config.getAnyThreadAnnotationClass()));
    }

    if (!ui.isEmpty()) {
      return Checking.IS_UI;
    }

    if (!work.isEmpty()) {
      return Checking.IS_WORKER;
    }

    if (!any.isEmpty()) {
      return Checking.IS_ANY;
    }

    return Checking.NOT_DEFINED;
  }

  private static Set<String> methodClosure(
      String className, ClassNode node, MethodNode method, Model model) {
    if (className == null) {
      return new HashSet<>();
    }

    return model.methodClosureWithAnnotationFilter(
        // NULLSAFE_FIXME[Not Vetted Third-Party]
        node.name,
        // NULLSAFE_FIXME[Not Vetted Third-Party]
        method.name,
        // NULLSAFE_FIXME[Not Vetted Third-Party]
        method.desc,
        method.access,
        AsmNameUtils.classJavaNameToDescriptor(className));
  }

  private Checking computeMethodChecking(ClassNode node, MethodNode method, Model model)
      throws AnnotationProcessingException {
    Set<String> ui = methodClosure(config.getUiThreadAnnotationClass(), node, method, model);
    Set<String> work = methodClosure(config.getWorkerThreadAnnotationClass(), node, method, model);
    Set<String> any = methodClosure(config.getAnyThreadAnnotationClass(), node, method, model);

    if ((ui.isEmpty() ? 0 : 1) + (work.isEmpty() ? 0 : 1) + (any.isEmpty() ? 0 : 1) > 1) {
      throw new AnnotationProcessingException(
          String.format(
              Locale.US,
              "Inconsistent annotations in class hierarchy when analyzing method "
                  + " %s%s (access %d): [%s] have UI annotation "
                  + "(%s), [%s] have worker annotation (%s) and [%s] have any "
                  + "annotation (%s)",
              method.name,
              method.desc,
              method.access,
              String.join(",", ui),
              config.getUiThreadAnnotationClass(),
              String.join(",", work),
              config.getWorkerThreadAnnotationClass(),
              String.join(",", any),
              config.getAnyThreadAnnotationClass()));
    }

    if (!ui.isEmpty()) {
      return Checking.IS_UI;
    }

    if (!work.isEmpty()) {
      return Checking.IS_WORKER;
    }

    if (!any.isEmpty()) {
      return Checking.IS_ANY;
    }

    return Checking.NOT_DEFINED;
  }

  private enum Checking {
    IS_UI,
    IS_WORKER,
    IS_ANY,
    NOT_DEFINED;

    Checking orDefault(Checking other) {
      if (this == NOT_DEFINED) {
        return other;
      }

      return this;
    }
  }
}
