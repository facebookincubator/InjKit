// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.crashshield;

import com.facebook.ads.injkit.AnnotationProcessingException;
import com.facebook.ads.injkit.AsmMethodUtils;
import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.BaseInjector;
import com.facebook.ads.injkit.Injector;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.NopInjector;
import com.facebook.ads.injkit.ReflectUtils;
import com.facebook.ads.injkit.model.Model;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class CrashShieldInjector extends BaseInjector {
    private static final String HANDLE_THROWABLE_METHOD_NAME = "handleThrowable";
    private static final String METHOD_FINISHED_METHOD_NAME = "methodFinished";
    private static final String SAFE_PREFIX = "safe_";
    private static final Map<String, String> ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP;
    private static final List<String> IGNORED_PACKAGES;

    private final boolean shouldProcessConstructors;
    private final boolean shouldProcessViews;

    private final String exceptionHandlerIName;
    private final Class<? extends Annotation> enableAnnotationClass;
    private final Class<? extends Annotation> disableAnnotationClass;

    static {
        ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP = new HashMap<>();
        ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
                .put("android/view/View", "com/facebook/ads/internal/shield/SafeView");
        ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
                .put("android/view/ViewGroup", "com/facebook/ads/internal/shield/SafeViewGroup");
        ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
                .put("android/widget/RelativeLayout",
                        "com/facebook/ads/internal/shield/SafeRelativeLayout");
        ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
            .put("android/widget/FrameLayout",
                "com/facebook/ads/internal/shield/SafeFrameLayout");
        ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
            .put("android/widget/LinearLayout",
                "com/facebook/ads/internal/shield/SafeLinearLayout");

        IGNORED_PACKAGES = new ArrayList<>();
        IGNORED_PACKAGES.add("com.facebook.ads.internal.util.reporting");
    }

    private CrashShieldInjector(
            String exceptionHandlerIName,
            Class<? extends Annotation> enableAnnotationClass,
            Class<? extends Annotation> disableAnnotationClass,
            boolean shouldProcessConstructors,
            boolean shouldProcessViews) {
        super(IGNORED_PACKAGES::contains);
        this.exceptionHandlerIName = exceptionHandlerIName;
        this.enableAnnotationClass = enableAnnotationClass;
        this.disableAnnotationClass = disableAnnotationClass;
        this.shouldProcessConstructors = shouldProcessConstructors;
        this.shouldProcessViews = shouldProcessViews;
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

            Retention retention = clsAnnotation.getAnnotation(Retention.class);
            if (retention == null || retention.value() == RetentionPolicy.SOURCE) {
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
                    String.format(Locale.US, "Failed to load class '%s'", name),
                    e);
        }
    }

    static Injector make(
            URLClassLoader applicationCode,
            CrashShieldConfiguration config)
            throws InvalidAnnotationProcessorConfigurationException {
        if (!config.isEnabled()) {
            return new NopInjector();
        }

        Class<?> handler =
                ReflectUtils.checkClassExistsAndIsPublic(
                        applicationCode,
                        config.getExceptionHandlerClass());
        validateHandler(handler);

        return new CrashShieldInjector(
                Type.getInternalName(handler),
                loadAnnotation(applicationCode, config.getEnableAnnotationClass()),
                loadAnnotation(
                        applicationCode,
                        config.getDisableAnnotationClass()),
                config.isShouldProcessConstructors(),
                config.isShouldProcessViews());
    }

    @Override
    protected void processImpl(ClassNode clsNode, Model model) {
        if ((clsNode.access & Opcodes.ACC_INTERFACE) != 0) {
            return;
        }

        if (ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP.containsValue(clsNode.name)) {
            return;
        }

        FindAndRemoveAnnotationResult defaultOp =
                findAndRemoveAnnotation(
                        clsNode.visibleAnnotations,
                        clsNode.invisibleAnnotations);

        for (MethodNode method : clsNode.methods) {
            if (AsmMethodUtils.isStaticInitializer(method)) {
                continue;
            }
            if (AsmMethodUtils.isConstructor(method)) {
                if (!maybeRenameParentConstructorCall(shouldProcessViews, method, clsNode)
                        && shouldProcessConstructors) {
                    injectHandleThrowable(clsNode, method);
                }
                continue;
            }

            if (shouldProcessViews) {
                if (CrashShieldViewClassFilter.isViewClassChild(clsNode, model)) {
                    renameMethodsAndSuperCalls(method, clsNode);
                    if (CrashShieldViewClassFilter.
                            isViewMethodToRename(method.name, method.desc)) {
                        renameMethod(method);
                    }
                    continue;
                }
            }

            if (CrashShieldViewClassFilter.isAutoProcessedMethod(method, clsNode, model)) {
                injectHandleThrowable(clsNode, method);
                continue;
            }

            FindAndRemoveAnnotationResult methodOp =
                    findAndRemoveAnnotation(method.visibleAnnotations, method.invisibleAnnotations)
                            .or(defaultOp);

            if (methodOp == FindAndRemoveAnnotationResult.FOUND_AUTO_HANDLE) {
                injectHandleThrowable(clsNode, method);
            }
        }

        if (shouldProcessViews) {
            renameSuperClassIfDirectChild(clsNode);
        }
    }

    private static void renameSuperClassIfDirectChild(ClassNode clsNode) {
        String safeSuperClassName
                = ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP.get(clsNode.superName);
        if (safeSuperClassName != null) {
            clsNode.superName = safeSuperClassName;
        }
    }

    private void renameMethodsAndSuperCalls(MethodNode method, ClassNode clsNode) {
        for (Iterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
                AbstractInsnNode insnNode = it.next();
                if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL
                        && CrashShieldViewClassFilter.
                    isViewMethodToRename(
                                ((MethodInsnNode) insnNode).name,
                                ((MethodInsnNode) insnNode).desc)) {
                    renameInst((MethodInsnNode) insnNode);
                    if (clsNode.superName != null) {
                        String safeSuperClassName
                                = ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
                                        .get(clsNode.superName);
                        if (safeSuperClassName != null) {
                            ((MethodInsnNode) insnNode).owner = safeSuperClassName;
                        }
                    }
                }
        }
    }

    private void renameInst(MethodInsnNode istr) {
        istr.name = SAFE_PREFIX + istr.name;
    }

    private void renameMethod(MethodNode method) {
        method.name = SAFE_PREFIX + method.name;
    }

    private static boolean maybeRenameParentConstructorCall(boolean shouldProcessViews,
                                                            MethodNode method,
                                                            ClassNode clsNode) {
        if (!shouldProcessViews) {
            return false;
        }

        for (Iterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
            AbstractInsnNode insnNode = it.next();
            if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL
                    && AsmMethodUtils.isConstructorName(((MethodInsnNode) insnNode).name)
                    // Only do if this is super constructor call: we could
                    // be calling a constructor of another object
                    // for example through a new instruction.
                    && ((MethodInsnNode) insnNode)
                            .owner
                                .equals(clsNode.superName)) {
                                        String safeSuperClassName
                                                = ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
                                                .get(clsNode.superName);
                                        if (safeSuperClassName != null) {
                                            ((MethodInsnNode) insnNode).owner = safeSuperClassName;
                                            return true;
                                        }
                }
        }

        return false;
    }

    private FindAndRemoveAnnotationResult findAndRemoveAnnotation(
            List<AnnotationNode> visibleAnnotations,
            List<AnnotationNode> invisibleAnnotations) {
        FindAndRemoveAnnotationResult result = FindAndRemoveAnnotationResult.NOT_FOUND;

        for (List<AnnotationNode> list : Arrays.asList(visibleAnnotations, invisibleAnnotations)) {
            if (list == null) {
                continue;
            }

            Iterator<AnnotationNode> visibleIt = list.iterator();
            while (visibleIt.hasNext()) {
                String nextDesc = visibleIt.next().desc;

                if (nextDesc.equals(
                        AsmNameUtils.classJavaNameToDescriptor(
                                enableAnnotationClass.getName()))) {
                    result = result.or(FindAndRemoveAnnotationResult.FOUND_AUTO_HANDLE);
                    visibleIt.remove();
                } else if (nextDesc.equals(
                        AsmNameUtils.classJavaNameToDescriptor(
                                disableAnnotationClass.getName()))) {
                    result = result.or(FindAndRemoveAnnotationResult.FOUND_NO_AUTO_HANDLE);
                    visibleIt.remove();
                }
            }
        }

        return result;
    }

    private static void validateHandler(Class<?> handlerClass)
            throws InvalidAnnotationProcessorConfigurationException {
        ReflectUtils.checkMethodIsPublicStatic(
                handlerClass, HANDLE_THROWABLE_METHOD_NAME, Throwable.class, Object.class);
        ReflectUtils.checkMethodIsPublicStatic(
                handlerClass, METHOD_FINISHED_METHOD_NAME, Object.class);
    }

    private void injectHandleThrowable(ClassNode cls, MethodNode method) {

        LabelNode tryNode = new LabelNode();
        LabelNode catchNode = new LabelNode();

        AbstractInsnNode firstCoveredInstruction;

        try {
            firstCoveredInstruction = findFirstCoveredInstruction(cls, method);
        } catch (AnnotationProcessingException t) {
            return;
        }

        if (firstCoveredInstruction != null) {

            method.instructions.insertBefore(firstCoveredInstruction, tryNode);

            if (!AsmMethodUtils.isConstructor(method)) {
                insertIfCrashingCondition(cls, method);
            }

            method.instructions.add(catchNode);

            if ((method.access & Opcodes.ACC_STATIC) != 0) {
                method.instructions.add(new LdcInsnNode(Type.getObjectType(cls.name)));
            } else {
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            }

            method.instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            exceptionHandlerIName,
                            "handleThrowable",
                            "(Ljava/lang/Throwable;Ljava/lang/Object;)V",
                            false));
            method.instructions.add(makeReturnCodeFor(method));

            if (method.tryCatchBlocks == null) {
                method.tryCatchBlocks = new ArrayList<>();
            }

            method.tryCatchBlocks.add(
                    new TryCatchBlockNode(tryNode, catchNode, catchNode, "java/lang/Throwable"));
        }
    }

    private void insertIfCrashingCondition(ClassNode cls, MethodNode method) {
        InsnList insnList = new InsnList();

        if ((method.access & Opcodes.ACC_STATIC) != 0) {
            insnList.add(new LdcInsnNode(Type.getObjectType(cls.name)));
        } else {
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }

        insnList.add(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        exceptionHandlerIName,
                        "isObjectCrashing",
                        "(Ljava/lang/Object;)Z",
                        false
                ));

        LabelNode skipReturn = new LabelNode();
        insnList.add(new JumpInsnNode(Opcodes.IFEQ, skipReturn));
        insnList.add(makeReturnCodeFor(method));
        insnList.add(skipReturn);
        method.instructions.insert(insnList);
    }

    private InsnList makeReturnCodeFor(MethodNode method) {
        InsnList returnCode = new InsnList();

        switch (Type.getReturnType(method.desc).getSort()) {
            case Type.VOID:
                returnCode.add(new InsnNode(Opcodes.RETURN));
                break;
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.INT:
            case Type.SHORT:
                returnCode.add(new InsnNode(Opcodes.ICONST_0));
                returnCode.add(new InsnNode(Opcodes.IRETURN));
                break;
            case Type.DOUBLE:
                returnCode.add(new InsnNode(Opcodes.DCONST_0));
                returnCode.add(new InsnNode(Opcodes.DRETURN));
                break;
            case Type.FLOAT:
                returnCode.add(new InsnNode(Opcodes.FCONST_0));
                returnCode.add(new InsnNode(Opcodes.FRETURN));
                break;
            case Type.LONG:
                returnCode.add(new InsnNode(Opcodes.LCONST_0));
                returnCode.add(new InsnNode(Opcodes.LRETURN));
                break;
            default:
                returnCode.add(new InsnNode(Opcodes.ACONST_NULL));
                returnCode.add(new InsnNode(Opcodes.ARETURN));
                break;
        }

        return returnCode;
    }

    private AbstractInsnNode findFirstCoveredInstruction(ClassNode clsNode, MethodNode method)
            throws AnnotationProcessingException {
        if (!AsmMethodUtils.isConstructor(method)) {
            return method.instructions.getFirst();
        }

        AbstractInsnNode superOrThisInit = null;
        for (Iterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext(); ) {
            AbstractInsnNode current = it.next();
            if (current.getOpcode() == Opcodes.INVOKESPECIAL) {
                if (((MethodInsnNode) current).owner.equals(clsNode.superName)
                        || ((MethodInsnNode) current).owner.equals(clsNode.name)) {
                    if (superOrThisInit != null) {
                        // This may actually happen in legal code, but it is really difficult to
                        // support (I think). And so far doesn't occur in our codebase.
                        throw new AnnotationProcessingException(
                                String.format(
                                        Locale.US,
                                        "Class '%s', constructor '%s' has more than one "
                                                + "INVOKESPECIAL for this or superclass",
                                        clsNode.name,
                                        method.desc));
                    }

                    superOrThisInit = current;
                }
            }
        }

        if (superOrThisInit == null) {
            // Super class must always be constructed.
            throw new AssertionError();
        }

        return superOrThisInit.getNext();
    }

    private enum FindAndRemoveAnnotationResult {
        NOT_FOUND,
        FOUND_AUTO_HANDLE,
        FOUND_NO_AUTO_HANDLE;

        FindAndRemoveAnnotationResult or(FindAndRemoveAnnotationResult result) {
            if (this == NOT_FOUND) {
                return result;
            }

            return this;
        }
    }

    public static void setSafeNames(String safeViewIName, String safeViewGroupIname) {
        ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
                .put("android/view/View", safeViewIName);
        ANDROID_CLASS_INAME_AND_SAFE_CLASS_INAME_MAP
                .put("android/view/ViewGroup", safeViewGroupIname);
    }
}
