/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.sdkdebugger;

import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.BaseInjector;
import com.facebook.ads.injkit.InvalidAnnotationFormatException;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.NopInjector;
import com.facebook.ads.injkit.ReflectUtils;
import com.facebook.ads.injkit.model.Model;
import com.facebook.infer.annotation.Nullsafe;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@Nullsafe(Nullsafe.Mode.LOCAL)
class SdkDebuggerInjector extends BaseInjector {
  private static final String LOG_CALL_METHOD_NAME = "logCall";
  private static final String METHOD_DESCRIPTION_PARAM = "description";

  private final String loggerIName;
  private final Class<? extends Annotation> logCallAnnotationClass;

  private SdkDebuggerInjector(
      String loggerIName, Class<? extends Annotation> logCallAnnotationClass) {
    super(__ -> false);
    this.loggerIName = loggerIName;
    this.logCallAnnotationClass = logCallAnnotationClass;
  }

  private static Class<? extends Annotation> loadAnnotation(ClassLoader classLoader, String name)
      throws InvalidAnnotationProcessorConfigurationException {
    try {
      Class<?> cls = classLoader.loadClass(name);
      if (!Annotation.class.isAssignableFrom(cls)) {
        throw new InvalidAnnotationProcessorConfigurationException(
            String.format(
                Locale.US,
                "'%s' is not a subclass of '%s'",
                cls.getName(),
                Annotation.class.getName()));
      }

      @SuppressWarnings("unchecked")
      Class<? extends Annotation> clsAnnotation = (Class<? extends Annotation>) cls;

      if (!checkIfMethodExists(METHOD_DESCRIPTION_PARAM, cls)) {
        throw new InvalidAnnotationFormatException(
            String.format(
                Locale.US,
                "Annotation '%s' must declare a method %s",
                name,
                METHOD_DESCRIPTION_PARAM));
      }

      Retention retention = clsAnnotation.getAnnotation(Retention.class);
      if (retention == null || retention.value() == RetentionPolicy.RUNTIME) {
        throw new InvalidAnnotationProcessorConfigurationException(
            String.format(
                Locale.US,
                "Annotation '%s' must have @Retention with RetentionPolicy.CLASS "
                    + "or RetentionPolicy.RUNTIME",
                name));
      }

      return clsAnnotation;
    } catch (ClassNotFoundException e) {
      throw new InvalidAnnotationProcessorConfigurationException(
          String.format(Locale.US, "Failed to load class '%s'", name), e);
    }
  }

  public static BaseInjector make(URLClassLoader applicationCode, SdkDebuggerConfiguration config)
      throws InvalidAnnotationProcessorConfigurationException {
    if (!config.isEnabled()) {
      return new NopInjector();
    }

    Class<?> handler =
        ReflectUtils.checkClassExistsAndIsPublic(applicationCode, config.getCallLoggerClass());
    validateHandler(handler);

    return new SdkDebuggerInjector(
        // NULLSAFE_FIXME[Not Vetted Third-Party]
        Type.getInternalName(handler),
        loadAnnotation(applicationCode, config.getLogCallAnnotationClass()));
  }

  @Override
  protected void processImpl(ClassNode clsNode, Model model) {
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    for (MethodNode method : clsNode.methods) {
      AnnotationNode foundAnnotation =
          // NULLSAFE_FIXME[Not Vetted Third-Party]
          findAndRemoveAnnotation(method.visibleAnnotations, method.invisibleAnnotations);
      if (foundAnnotation != null) {
        injectLogCall(method, getAnnotationDescription(foundAnnotation));
      }
    }
  }

  private AnnotationNode findAndRemoveAnnotation(
      List<AnnotationNode> visibleAnnotations, List<AnnotationNode> invisibleAnnotations) {
    for (List<AnnotationNode> list : Arrays.asList(visibleAnnotations, invisibleAnnotations)) {
      if (list == null) {
        continue;
      }

      Iterator<AnnotationNode> visibleIt = list.iterator();
      while (visibleIt.hasNext()) {
        AnnotationNode annotation = visibleIt.next();
        // NULLSAFE_FIXME[Not Vetted Third-Party]
        String nextDesc = annotation.desc;

        if (nextDesc.equals(
            AsmNameUtils.classJavaNameToDescriptor(logCallAnnotationClass.getName()))) {
          visibleIt.remove();
          return annotation;
        }
      }
    }

    // NULLSAFE_FIXME[Return Not Nullable]
    return null;
  }

  private static void validateHandler(Class<?> handlerClass)
      throws InvalidAnnotationProcessorConfigurationException {
    ReflectUtils.checkMethodIsPublicStatic(
        handlerClass, LOG_CALL_METHOD_NAME, String.class, String.class);
  }

  private static boolean checkIfMethodExists(String method, Class cls) {
    try {
      @SuppressWarnings("unchecked")
      Method m = cls.getDeclaredMethod(method);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  private void injectLogCall(MethodNode method, String description) {
    MethodInsnNode logCallMethod =
        new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            loggerIName,
            LOG_CALL_METHOD_NAME,
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
            false);
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    method.instructions.insert(logCallMethod);
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    method.instructions.insertBefore(logCallMethod, new LdcInsnNode(method.name));
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    method.instructions.insertBefore(logCallMethod, new LdcInsnNode(description));
    // Method hash used to differentiate between different instances.
    // Hex value better for external readability than default signed integer
    String methodHash = Integer.toString(method.hashCode(), 16);
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    method.instructions.insertBefore(logCallMethod, new LdcInsnNode(methodHash));
  }

  private static String getAnnotationDescription(AnnotationNode annotation) {
    if (annotation.values == null) {
      // NULLSAFE_FIXME[Return Not Nullable]
      return null;
    }

    if (annotation.values.size() % 2 != 0) {
      throw new IllegalStateException("AnnotationNode::values should be of even size");
    }

    for (int i = 0; i < annotation.values.size(); i += 2) {
      String key = (String) annotation.values.get(i);
      Object val = annotation.values.get(i + 1);
      if (key.equalsIgnoreCase(METHOD_DESCRIPTION_PARAM)) {
        return (String) val;
      }
    }

    // NULLSAFE_FIXME[Return Not Nullable]
    return null;
  }
}
