// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.threadcheck;

public class ThreadCheckConfiguration {
    private final boolean enabled;
    private final String uiThreadAnnotationClass;
    private final String workerThreadAnnotationClass;
    private final String anyThreadAnnotationClass;
    private final String violationHandlerClass;

    ThreadCheckConfiguration(
            boolean enabled,
            String uiThreadAnnotationClass,
            String workerThreadAnnotationClass,
            String anyThreadAnnotationClass,
            String violationHandlerClass) {
        this.enabled = enabled;
        this.uiThreadAnnotationClass = uiThreadAnnotationClass;
        this.workerThreadAnnotationClass = workerThreadAnnotationClass;
        this.anyThreadAnnotationClass = anyThreadAnnotationClass;
        this.violationHandlerClass = violationHandlerClass;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUiThreadAnnotationClass() {
        return uiThreadAnnotationClass;
    }

    public String getWorkerThreadAnnotationClass() {
        return workerThreadAnnotationClass;
    }

    public String getAnyThreadAnnotationClass() {
        return anyThreadAnnotationClass;
    }

    public String getViolationHandlerClass() {
        return violationHandlerClass;
    }
}
