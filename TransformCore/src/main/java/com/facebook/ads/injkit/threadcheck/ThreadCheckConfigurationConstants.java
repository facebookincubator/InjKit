// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.threadcheck;

public class ThreadCheckConfigurationConstants {
    private ThreadCheckConfigurationConstants() {}

    public static final String ENABLED = "thread-check-enabled";
    public static final String UI_THREAD_ANNOTATION_CLASS = "thread-check-ui-annotation-class";
    public static final String WORKER_THREAD_ANNOTATION_CLASS =
            "thread-check-worker-annotation-class";
    public static final String ANY_THREAD_ANNOTATION_CLASS = "thread-check-any-annotation-class";
    public static final String VIOLATION_HANDLER_CLASS = "thread-check-violation-handler-class";
}
