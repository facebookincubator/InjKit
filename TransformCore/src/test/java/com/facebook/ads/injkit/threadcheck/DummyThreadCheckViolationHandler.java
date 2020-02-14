// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.threadcheck;

import java.util.ArrayList;
import java.util.List;

public class DummyThreadCheckViolationHandler {
    private static final List<Class<?>> wtViolationClasses = new ArrayList<>();
    private static final List<String> wtViolationMethodNames = new ArrayList<>();
    private static final List<String> wtViolationMethodDescs = new ArrayList<>();
    private static final List<Class<?>> uitViolationClasses = new ArrayList<>();
    private static final List<String> uitViolationMethodNames = new ArrayList<>();
    private static final List<String> uitViolationMethodDescs = new ArrayList<>();

    public static synchronized void workerThreadViolationDetected(
            Class<?> cls,
            String methodName,
            String methodDesc) {
        wtViolationClasses.add(cls);
        wtViolationMethodNames.add(methodName);
        wtViolationMethodDescs.add(methodDesc);
    }

    public static void uiThreadViolationDetected(
            Class<?> cls,
            String methodName,
            String methodDesc) {
        uitViolationClasses.add(cls);
        uitViolationMethodNames.add(methodName);
        uitViolationMethodDescs.add(methodDesc);
    }

    public static void reset() {
        wtViolationClasses.clear();
        wtViolationMethodNames.clear();
        wtViolationMethodDescs.clear();
        uitViolationClasses.clear();
        uitViolationMethodNames.clear();
        uitViolationMethodDescs.clear();
    }

    static int violationsDetected() {
        return wtViolationClasses.size() + uitViolationClasses.size();
    }

    static int uiThreadViolationsDetected() {
        return uitViolationClasses.size();
    }

    static boolean uiViolationsAre(Class<?> cls, String method, String desc) {
        return uitViolationClasses.size() == 1
                && uitViolationClasses.get(0).getName().equals(cls.getName())
                && uitViolationMethodNames.get(0).equals(method)
                && uitViolationMethodDescs.get(0).equals(desc);
    }

    static int workerThreadViolationsDetected() {
        return wtViolationClasses.size();
    }

    static boolean workerViolationsAre(Class<?> cls, String method, String desc) {
        return wtViolationClasses.size() == 1
                && wtViolationClasses.get(0).getName().equals(cls.getName())
                && wtViolationMethodNames.get(0).equals(method)
                && wtViolationMethodDescs.get(0).equals(desc);
    }
}
