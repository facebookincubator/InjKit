// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.crashshield.safe_components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

@SuppressLint({"EmptyCatchBlock", "CatchGeneralException"})
public class SafeView extends View {

    public boolean onMeasureCalled;
    public boolean safe_onMeasureCalled;

    public SafeView(Context context) {
        super(context);
    }

    public SafeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SafeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean performClick() {
        try {
            safe_performClick();
        } catch (Throwable t) {
        }

        return super.performClick();
    }

    // Subclasses that implement performClick() will have their method
    // change automatically to this by crashshield
    public boolean safe_performClick() {
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        onMeasureCalled = true;

        try {
            safe_onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (Throwable t) {
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void safe_onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        safe_onMeasureCalled = true;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            safe_onDraw(canvas);
        } catch (Throwable t) {
        }

        super.onDraw(canvas);
    }

    protected void safe_onDraw(Canvas canvas) {
    }
}
