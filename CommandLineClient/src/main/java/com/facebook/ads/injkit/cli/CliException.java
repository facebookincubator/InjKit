// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.cli;

import java.util.Locale;

public class CliException extends Exception {
    public CliException(String fmt, Object... values) {
        super(String.format(Locale.US, fmt, values));
    }
}
