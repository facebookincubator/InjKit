/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.ads.injkit.crashshield.safe_components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;

@SuppressLint({"EmptyCatchBlock", "CatchGeneralException"})
public class SafeViewGroup extends ViewGroup {

  public SafeViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public SafeViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public SafeViewGroup(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SafeViewGroup(Context context) {
    super(context);
  }

  public boolean performClick() {
    try {
      safe_performClick();
      return super.performClick();
    } catch (Throwable t) {
      return super.performClick();
    }
  }

  // Subclasses that implement performClick() will have their method
  // change automatically to this by crashshield
  public boolean safe_performClick() {
    return super.performClick();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    try {
      safe_onMeasure(widthMeasureSpec, heightMeasureSpec);
    } catch (Throwable t) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  protected void safe_onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    try {
      safe_onDraw(canvas);
    } catch (Throwable t) {
      super.onDraw(canvas);
    }
  }

  protected void safe_onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }

  @Override
  protected void onFinishInflate() {
    try {
      safe_onFinishInflate();
    } catch (Throwable t) {
      super.onFinishInflate();
    }
  }

  protected void safe_onFinishInflate() {
    super.onFinishInflate();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    try {
      safe_onLayout(changed, l, t, r, b);
    } catch (Throwable throwable) {
    }
  }

  protected void safe_onLayout(boolean changed, int l, int t, int r, int b) {}

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    try {
      safe_onSizeChanged(w, h, oldw, oldh);
    } catch (Throwable t) {
      super.onSizeChanged(w, h, oldw, oldh);
    }
  }

  protected void safe_onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    try {
      return safe_onKeyDown(keyCode, event);
    } catch (Throwable t) {
      return super.onKeyDown(keyCode, event);
    }
  }

  public boolean safe_onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    try {
      return safe_onKeyUp(keyCode, event);
    } catch (Throwable t) {
      return super.onKeyUp(keyCode, event);
    }
  }

  public boolean safe_onKeyUp(int keyCode, KeyEvent event) {
    return super.onKeyUp(keyCode, event);
  }

  @Override
  public boolean onTrackballEvent(MotionEvent event) {
    try {
      return safe_onTrackballEvent(event);
    } catch (Throwable t) {
      return super.onTrackballEvent(event);
    }
  }

  public boolean safe_onTrackballEvent(MotionEvent event) {
    return super.onTrackballEvent(event);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    try {
      return safe_onTouchEvent(event);
    } catch (Throwable t) {
      return super.onTouchEvent(event);
    }
  }

  public boolean safe_onTouchEvent(MotionEvent event) {
    return super.onTouchEvent(event);
  }

  @Override
  protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
    try {
      safe_onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    } catch (Throwable t) {
      super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }
  }

  protected void safe_onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
    super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
  }

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    try {
      safe_onWindowFocusChanged(hasWindowFocus);
    } catch (Throwable t) {
      super.onWindowFocusChanged(hasWindowFocus);
    }
  }

  public void safe_onWindowFocusChanged(boolean hasWindowFocus) {
    super.onWindowFocusChanged(hasWindowFocus);
  }

  @Override
  protected void onAttachedToWindow() {
    try {
      safe_onAttachedToWindow();
    } catch (Throwable t) {
      super.onAttachedToWindow();
    }
  }

  protected void safe_onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override
  protected void onDetachedFromWindow() {
    try {
      safe_onDetachedFromWindow();
    } catch (Throwable t) {
      super.onDetachedFromWindow();
    }
  }

  protected void safe_onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  @Override
  protected void onWindowVisibilityChanged(int visibility) {
    try {
      safe_onWindowVisibilityChanged(visibility);
    } catch (Throwable t) {
      super.onWindowVisibilityChanged(visibility);
    }
  }

  protected void safe_onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
  }
}
