// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.crashshield;

import com.facebook.ads.injkit.ConfigurationParser;
import com.facebook.ads.injkit.InvalidAnnotationProcessorConfigurationException;
import com.facebook.ads.injkit.LineDirectiveSplit;
import com.facebook.ads.injkit.ParseContext;
import com.facebook.ads.injkit.UniqueSetting;

class CrashShieldConfigurationParser implements ConfigurationParser<CrashShieldConfiguration> {

    private final UniqueSetting enabledSetting = new UniqueSetting(
            CrashShieldConfigurationConstants.ENABLED);
    private final UniqueSetting disableAnnotationClassSetting =
            new UniqueSetting(CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS);
    private final UniqueSetting enableAnnotationClassSetting =
            new UniqueSetting(CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS);
    private final UniqueSetting exceptionHandlerClassSetting =
            new UniqueSetting(CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS);
    private final UniqueSetting shouldProcessConstructor =
            new UniqueSetting(CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR);
    private final UniqueSetting shouldProcessViews =
        new UniqueSetting(CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS);

    @Override
    public boolean parse(LineDirectiveSplit split, ParseContext ctx)
            throws InvalidAnnotationProcessorConfigurationException {
        switch (split.getDirective()) {
            case CrashShieldConfigurationConstants.ENABLED:
                enabledSetting.setValue(ctx, split.getContentsSingleString(ctx));
                return true;
            case CrashShieldConfigurationConstants.DISABLE_ANNOTATION_CLASS:
                disableAnnotationClassSetting.setValue(ctx, split.getContentsSingleString(ctx));
                return true;
            case CrashShieldConfigurationConstants.ENABLE_ANNOTATION_CLASS:
                enableAnnotationClassSetting.setValue(ctx, split.getContentsSingleString(ctx));
                return true;
            case CrashShieldConfigurationConstants.EXCEPTION_HANDLER_CLASS:
                exceptionHandlerClassSetting.setValue(ctx, split.getContentsSingleString(ctx));
                return true;
            case CrashShieldConfigurationConstants.SHOULD_PROCESS_CONSTRUCTOR:
                shouldProcessConstructor.setValue(ctx, split.getContentsSingleString(ctx));
                return true;
            case CrashShieldConfigurationConstants.SHOULD_PROCESS_VIEWS:
                shouldProcessViews.setValue(ctx, split.getContentsSingleString(ctx));
                return true;
            default:
                return false;
        }
    }

    @Override
    public CrashShieldConfiguration finish(ParseContext ctx)
            throws InvalidAnnotationProcessorConfigurationException {
        if (!enabledSetting.isSet() || !enabledSetting.asBoolean(ctx)) {
            return CrashShieldConfiguration.makeDisabled();
        }

        return CrashShieldConfiguration.makeEnabled(
                disableAnnotationClassSetting.getValue(ctx),
                enableAnnotationClassSetting.getValue(ctx),
                exceptionHandlerClassSetting.getValue(ctx),
                shouldProcessConstructor.asBoolean(ctx),
                shouldProcessViews.asBoolean(ctx));
    }
}
