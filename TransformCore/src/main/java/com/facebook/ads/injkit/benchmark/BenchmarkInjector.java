// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.benchmark;

import com.facebook.ads.injkit.AnnotationProcessingException;
import com.facebook.ads.injkit.AsmMethodUtils;
import com.facebook.ads.injkit.AsmNameUtils;
import com.facebook.ads.injkit.BaseInjector;
import com.facebook.ads.injkit.Injector;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.NopInjector;
import com.facebook.ads.injkit.ReflectUtils;
import com.facebook.ads.injkit.model.Model;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

class BenchmarkInjector extends BaseInjector {
    private static final String METHOD_RENAME_PREFIX = "$_facebook_benchmark_";
    private static final String WARN_AT_MILLIS_PROPERTY = "warnAtMillis";
    private static final String FAIL_AT_MILLIS_PROPERTY = "failAtMillis";
    private static final String EXECUTED_SUCCESS_METHOD_NAME = "executed";
    private static final String EXECUTED_WARNING_METHOD_NAME = "executedWithWarning";
    private static final String EXECUTED_FAILED_METHOD_NAME = "failed";
    private static final String EXECUTED_THROWN_METHOD_NAME = "thrown";

    private final boolean enabled;
    private final String annotationClassDesc;
    private BenchmarkMetrics defaultMetrics;
    private String benchmarkReceiverClassIName;

    private BenchmarkInjector(
            boolean enabled,
            Predicate<String> isPackageIgnored,
            String annotationClass,
            String benchmarkReceiverClassIName) {
        super(isPackageIgnored);

        this.enabled = enabled;
        this.annotationClassDesc = AsmNameUtils.classJavaNameToDescriptor(annotationClass);
        this.benchmarkReceiverClassIName = benchmarkReceiverClassIName;
    }

    static Injector make(
            ClassLoader applicationClasses,
            BenchmarkConfiguration config)
            throws InvalidAnnotationProcessorConfigurationException {
        if (!config.isBenchmarkEnabled() && config.getBenchmarkAnnotationClass() == null) {
            return new NopInjector();
        }

        if (config.getBenchmarkReceiverClass() != null) {
            Class<?> receiverClass =
                    ReflectUtils.checkClassExistsAndIsPublic(
                            applicationClasses,
                            config.getBenchmarkReceiverClass());

            ReflectUtils.checkMethodIsPublicStatic(
                    receiverClass,
                    EXECUTED_SUCCESS_METHOD_NAME,
                    String.class,
                    String.class,
                    String.class,
                    long.class);

            ReflectUtils.checkMethodIsPublicStatic(
                    receiverClass,
                    EXECUTED_WARNING_METHOD_NAME,
                    String.class,
                    String.class,
                    String.class,
                    long.class,
                    long.class);

            ReflectUtils.checkMethodIsPublicStatic(
                    receiverClass,
                    EXECUTED_FAILED_METHOD_NAME,
                    String.class,
                    String.class,
                    String.class,
                    long.class,
                    long.class);

            ReflectUtils.checkMethodIsPublicStatic(
                    receiverClass,
                    EXECUTED_THROWN_METHOD_NAME,
                    String.class,
                    String.class,
                    String.class,
                    Throwable.class,
                    long.class);
        }

        return new BenchmarkInjector(
                config.isBenchmarkEnabled(),
                pkg -> false,
                config.getBenchmarkAnnotationClass(),
                config.getBenchmarkReceiverClass() == null
                        ? null
                        : AsmNameUtils.classJavaNameToInternalName(
                                config.getBenchmarkReceiverClass()));
    }

    private static Integer expectIntAnnotationDefaultProperty(String desc, String prop, Model model)
            throws AnnotationProcessingException {
        if (!model.knowsAnnotation(desc)) {
            throw new AnnotationProcessingException(
                    String.format(Locale.US, "Annotation not known: %s", desc));
        }

        Map<String, Type> props = model.annotationProperties(desc);
        if (!props.containsKey(prop)) {
            throw new AnnotationProcessingException(
                    String.format(
                            Locale.US,
                            "Annotation %s does not contain property %s",
                            desc,
                            prop));
        }

        if (!props.get(prop).equals(Type.INT_TYPE)) {
            throw new AnnotationProcessingException(
                    String.format(
                            Locale.US,
                            "Annotation %s property %s has type %s instead of int",
                            desc,
                            prop,
                            props.get(prop).getInternalName()));
        }

        return (Integer) model.annotationDefaultValue(desc, prop);
    }

    private static BenchmarkMetrics getDefaultMetrics(
            String benchmarkAnnotationClassDesc,
            Model model)
            throws AnnotationProcessingException {
        Integer warnAtMillisProperty =
                expectIntAnnotationDefaultProperty(
                        benchmarkAnnotationClassDesc,
                        WARN_AT_MILLIS_PROPERTY,
                        model);
        if (warnAtMillisProperty == null) {
            warnAtMillisProperty = -1;
        }

        Integer failAtMillisProperty =
                expectIntAnnotationDefaultProperty(
                        benchmarkAnnotationClassDesc,
                        FAIL_AT_MILLIS_PROPERTY,
                        model);
        if (failAtMillisProperty == null) {
            failAtMillisProperty = -1;
        }

        return new BenchmarkMetrics(warnAtMillisProperty, failAtMillisProperty);
    }

    @Override
    protected void processImpl(ClassNode clsNode, Model model)
            throws AnnotationProcessingException {
        List<MethodNode> newMethods = new ArrayList<>();

        if (clsNode.methods == null) {
            return;
        }

        // This will be used when computing the methodBenchmarkMetrics so make sure it is set
        // before.
        if (defaultMetrics == null) {
            defaultMetrics = getDefaultMetrics(annotationClassDesc, model);
        }

        for (MethodNode methodNode : clsNode.methods) {
            removeBenchmarkMetrics(methodNode);

            // We don't forward abstract methods.
            if (methodNode.instructions == null || methodNode.instructions.size() == 0) {
                continue;
            }

            if (enabled) {
                BenchmarkMetrics metrics = methodBenchmarkMetrics(clsNode, methodNode, model);
                if (metrics != null) {
                    String moveMethodName = methodNode.name;
                    methodNode.name = METHOD_RENAME_PREFIX + methodNode.name;

                    // Make sure constructors' forward methods have valid names.
                    methodNode.name = methodNode.name.replace('<', '$');
                    methodNode.name = methodNode.name.replace('>', '$');
                    MethodNode newMethod =
                            createForwardMethod(moveMethodName, clsNode, methodNode, metrics);
                    newMethods.add(newMethod);

                    if (AsmNameUtils.INIT.equals(moveMethodName)) {
                        InsnList newInstructions =
                                extractSuperCall(methodNode.instructions, clsNode.superName);
                        if (newInstructions == null) {
                            throw new AnnotationProcessingException(
                                    String.format(
                                            Locale.US,
                                            "Don't know what to do to transform method %s%s of "
                                                    + "class %s for benchmarking",
                                            moveMethodName,
                                            methodNode.desc,
                                            clsNode.name));
                        }

                        newMethod.instructions.insert(newInstructions);
                    }
                }
            }
        }

        clsNode.methods.addAll(newMethods);
    }

    private int getAnnotationValue(
            ClassNode classNode,
            MethodNode methodNode,
            Model model,
            String property)
            throws AnnotationProcessingException {
        Map<String, Object> values =
                model.methodClosureWithAnnotationFilterAndValue(
                        classNode.name,
                        methodNode.name,
                        methodNode.desc,
                        methodNode.access,
                        annotationClassDesc,
                        property);

        if (values.isEmpty()) {
            return defaultMetrics.get(property);
        }

        if (values.size() == 1) {
            return (Integer) values.values().iterator().next();
        }

        // Try self class first.
        if (values.containsKey(classNode.name)) {
            return (Integer) values.get(classNode.name);
        }

        // Multiple classes. See if all values are equal.
        Object prev = null;
        for (Object v : values.values()) {
            if (prev == null) {
                prev = v;
            } else {
                if (!prev.equals(v)) {
                    throw new AnnotationProcessingException(
                            String.format(
                                    Locale.US,
                                    "Class %s, method %s%s (access %d) has multiple different " +
                                            "inherited values for property %s of annotation %s",
                                    classNode.name,
                                    methodNode.name,
                                    methodNode.desc,
                                    methodNode.access,
                                    property,
                                    annotationClassDesc));
                }
            }
        }

        return (Integer) prev;
    }

    private BenchmarkMetrics methodBenchmarkMetrics(
            ClassNode classNode,
            MethodNode methodNode,
            Model model)
            throws AnnotationProcessingException {
        if (model
                .methodClosureWithAnnotationFilter(
                        classNode.name,
                        methodNode.name,
                        methodNode.desc,
                        methodNode.access,
                        annotationClassDesc)
                .isEmpty()) {
            return null;
        }


        int warnValue = getAnnotationValue(classNode, methodNode, model, WARN_AT_MILLIS_PROPERTY);
        int failValue = getAnnotationValue(classNode, methodNode, model, FAIL_AT_MILLIS_PROPERTY);

        if (warnValue == -1 && failValue == -1) {
            return null;
        } else {
            return new BenchmarkMetrics(warnValue, failValue);
        }
    }

    private MethodNode createForwardMethod(
            String name,
            ClassNode owner,
            MethodNode destination,
            BenchmarkMetrics metrics) {
        MethodNode newMethod =
                new MethodNode(
                        destination.access,
                        name,
                        destination.desc,
                        destination.signature,
                        toArray(destination.exceptions));

        InsnList instructions = new InsnList();

        // Get current nano time and save because we may have an exception.
        int tempVar =
                Type.getArgumentTypes(newMethod.desc).length +
                        ((destination.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0);

        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "java/lang/System",
                        "nanoTime",
                        "()J",
                        false));
        instructions.add(new VarInsnNode(Opcodes.LSTORE, tempVar));

        // Mark method call start.
        LabelNode methodStartLabel = new LabelNode(new Label());
        instructions.add(methodStartLabel);

        // Call the forwarded method.
        Type[] arguments = Type.getArgumentTypes(destination.desc);
        int varPos = 0;
        if ((destination.access & Opcodes.ACC_STATIC) == 0) {
            varPos++;
            // Load "this".
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }

        for (Type argument : arguments) {
            instructions.add(new VarInsnNode(argument.getOpcode(Opcodes.ILOAD), varPos));
            varPos += argument.getSize();
        }

        if ((destination.access & Opcodes.ACC_STATIC) != 0) {
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            owner.name,
                            destination.name,
                            destination.desc,
                            false));
        } else {
            instructions.add(
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                            owner.name,
                            destination.name,
                            destination.desc,
                            false));
        }

        LabelNode methodCallEndLabel = new LabelNode(new Label());
        instructions.add(methodCallEndLabel);

        // At this point stack contains the return value. Add the parameters to notify. These will
        // be the same for all method callbacks.
        instructions.add(new LdcInsnNode(owner.name.replace('/', '.')));
        instructions.add(new LdcInsnNode(name));
        instructions.add(new LdcInsnNode(newMethod.desc));

        // Compute time difference in millis.
        instructions.add(new VarInsnNode(Opcodes.LLOAD, tempVar));
        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "java/lang/System",
                        "nanoTime",
                        "()J",
                        false));
        instructions.add(new InsnNode(Opcodes.LSUB));
        instructions.add(new InsnNode(Opcodes.LNEG));

        // Handle failure.
        LabelNode failureHandler = new LabelNode();
        long failNanos = ((long) metrics.failAtMillis) * 1_000_000L;
        instructions.add(new InsnNode(Opcodes.DUP2));
        instructions.add(new LdcInsnNode(failNanos));
        instructions.add(new InsnNode(Opcodes.LCMP));
        instructions.add(new JumpInsnNode(Opcodes.IFGE, failureHandler));

        // Handle warning.
        LabelNode warningHandler = new LabelNode();
        long warnNanos = ((long) metrics.warnAtMillis) * 1_000_000L;
        instructions.add(new InsnNode(Opcodes.DUP2));
        instructions.add(new LdcInsnNode(warnNanos));
        instructions.add(new InsnNode(Opcodes.LCMP));
        instructions.add(new JumpInsnNode(Opcodes.IFGE, warningHandler));

        // Otherwise compute text.
        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        benchmarkReceiverClassIName,
                        EXECUTED_SUCCESS_METHOD_NAME,
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V",
                        false));

        // Return the return value, if any.
        LabelNode endNode = new LabelNode();
        instructions.add(endNode);
        instructions.add(
                new InsnNode(Type.getReturnType(destination.desc).getOpcode(Opcodes.IRETURN)));

        // Failure handler.
        instructions.add(failureHandler);
        instructions.add(new LdcInsnNode(failNanos));
        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        benchmarkReceiverClassIName,
                        EXECUTED_FAILED_METHOD_NAME,
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJ)V",
                        false));
        instructions.add(new JumpInsnNode(Opcodes.GOTO, endNode));

        // Warning handler.
        instructions.add(warningHandler);
        instructions.add(new LdcInsnNode(warnNanos));
        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        benchmarkReceiverClassIName,
                        EXECUTED_WARNING_METHOD_NAME,
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJ)V",
                        false));
        instructions.add(new JumpInsnNode(Opcodes.GOTO, endNode));

        // Add catch handler.
        LabelNode catchLabel = new LabelNode(new Label());
        instructions.add(catchLabel);

        // Stack contains [exception]. Add parameters before exception, but keep a copy of the
        // exception on the bottom of the stack for later rethrowing.
        instructions.add(new InsnNode(Opcodes.DUP));
        instructions.add(new LdcInsnNode(owner.name.replace('/', '.')));
        instructions.add(new InsnNode(Opcodes.SWAP));
        instructions.add(new LdcInsnNode(name));
        instructions.add(new InsnNode(Opcodes.SWAP));
        instructions.add(new LdcInsnNode(newMethod.desc));
        instructions.add(new InsnNode(Opcodes.SWAP));

        // Compute time difference in millis.
        instructions.add(new VarInsnNode(Opcodes.LLOAD, tempVar));
        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "java/lang/System",
                        "nanoTime",
                        "()J",
                        false));
        instructions.add(new InsnNode(Opcodes.LSUB));
        instructions.add(new InsnNode(Opcodes.LNEG));

        // Call the method to report the failure.
        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        benchmarkReceiverClassIName,
                        EXECUTED_THROWN_METHOD_NAME,
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"
                                + "Ljava/lang/Throwable;J)V",
                        false));

        // Rethrow the exception.
        instructions.add(new InsnNode(Opcodes.ATHROW));

        // Mark the end of the catch block.
        LabelNode endCatchLabel = new LabelNode(new Label());
        instructions.add(endCatchLabel);

        if (newMethod.tryCatchBlocks == null) {
            newMethod.tryCatchBlocks = new ArrayList<>();
        }

        newMethod.tryCatchBlocks.add(
                new TryCatchBlockNode(
                        methodStartLabel,
                        methodCallEndLabel,
                        catchLabel,
                        "java/lang/Throwable"));

        newMethod.instructions = instructions;

        return newMethod;
    }

    private static String[] toArray(List<String> nullableList) {
        if (nullableList == null) {
            return new String[0];
        }

        return nullableList.toArray(new String[0]);
    }

    private void removeBenchmarkMetrics(MethodNode methodNode) {
        if (methodNode.visibleAnnotations != null) {
            for (int i = 0; i < methodNode.visibleAnnotations.size(); i++) {
                if (annotationClassDesc.equals(methodNode.visibleAnnotations.get(i).desc)) {
                    methodNode.visibleAnnotations.remove(i);
                    i--;
                }
            }
        }

        if (methodNode.invisibleAnnotations != null) {
            for (int i = 0; i < methodNode.invisibleAnnotations.size(); i++) {
                if (annotationClassDesc.equals(methodNode.invisibleAnnotations.get(i).desc)) {
                    methodNode.invisibleAnnotations.remove(i);
                    i--;
                }
            }
        }
    }

    private static InsnList extractSuperCall(InsnList instructions, String superIName) {
        AbstractInsnNode loadNode = null;
        AbstractInsnNode superNode = null;

        // Super call goes all the way from the first ALOAD0 to the actual INVOKESPECIAL.

        for (int i = 0; i < instructions.size(); i++) {
            AbstractInsnNode n = instructions.get(i);
            if (n.getOpcode() == Opcodes.ALOAD && loadNode == null && ((VarInsnNode) n).var == 0) {
                loadNode = n;
                continue;
            }

            if (loadNode != null
                    && n.getOpcode() == Opcodes.INVOKESPECIAL
                    && AsmMethodUtils.isConstructorName(((MethodInsnNode) n).name)
                    && ((MethodInsnNode) n).owner.equals(superIName)) {
                if (superNode == null) {
                    superNode = n;
                } else {
                    // Ooops, more than one invokespecial of the superclass found.
                    return null;
                }
            }
        }

        if (superNode == null) {
            // Ooops, super constructor not found.
            return null;
        }

        int loadIdx = instructions.indexOf(loadNode);
        int superIdx = instructions.indexOf(superNode);

        InsnList newInstructions = new InsnList();
        for (int i = loadIdx; i <= superIdx; i++) {
            AbstractInsnNode n = instructions.get(loadIdx);
            instructions.remove(n);
            newInstructions.add(n);
        }

        return newInstructions;
    }

    private static class BenchmarkMetrics {
        final int warnAtMillis;
        final int failAtMillis;

        BenchmarkMetrics(int warnAtMillis, int failAtMillis) {
            this.warnAtMillis = warnAtMillis;
            this.failAtMillis = failAtMillis;
        }

        int get(String property) {
            if (property.equals(WARN_AT_MILLIS_PROPERTY)) {
                return warnAtMillis;
            } else if (property.equals(FAIL_AT_MILLIS_PROPERTY)) {
                return failAtMillis;
            } else {
                throw new AssertionError();
            }
        }
   }
}
