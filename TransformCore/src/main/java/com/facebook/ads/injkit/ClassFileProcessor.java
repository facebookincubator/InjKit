// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit;

import com.facebook.ads.injkit.model.Model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ClassFileProcessor {
    void process(InputStream input, OutputStream output)
            throws IOException, AnnotationProcessingException;
    void updateModel(InputStream input, Model model)
            throws IOException, AnnotationProcessingException;
}
