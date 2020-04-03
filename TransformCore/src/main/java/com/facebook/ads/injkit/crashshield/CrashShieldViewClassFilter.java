// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.crashshield;

import com.facebook.ads.injkit.model.Model;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CrashShieldViewClassFilter {
    // Maps method names to the descriptors
    private static final Map<String, String> FILTER_AUTO_PROCESSED_METHODS;
    private static final Map<String, Set<String>> FILTER_VIEW_METHODS;
    private static final Set<String> FILTER_AUTO_PROCESSED_INAMES;

    private static final Set<String> EXCLUDE_FROM_VIEW_FILTER_INAMES;
    private static final String VIEW_INAME = "android/view/View";
    private static final String VIEW_GROUP_INAME = "android/view/ViewGroup";
    private static final String ASYNC_TASK_INAME = "android/os/AsyncTask";
    private static final String HANDLER_INAME = "android/os/Handler";

    static {
        FILTER_AUTO_PROCESSED_INAMES = new HashSet<>();
        FILTER_AUTO_PROCESSED_INAMES.add("android/view/View$OnClickListener");
        FILTER_AUTO_PROCESSED_INAMES.add("java/lang/Runnable");
        FILTER_AUTO_PROCESSED_INAMES.add(ASYNC_TASK_INAME);
        FILTER_AUTO_PROCESSED_INAMES.add(HANDLER_INAME);
        FILTER_AUTO_PROCESSED_METHODS = new HashMap<>();
        FILTER_AUTO_PROCESSED_METHODS.put("onClick", "(Landroid/view/View;)V");
        FILTER_AUTO_PROCESSED_METHODS.put("run", "()V");
        FILTER_AUTO_PROCESSED_METHODS.put("onPreExecute", "()V");
        FILTER_AUTO_PROCESSED_METHODS.put("doInBackground",
            "([Ljava/lang/Object;)Ljava/lang/Object;");
        FILTER_AUTO_PROCESSED_METHODS.put("onPostExecute", "(Ljava/lang/Object;)V");
        FILTER_AUTO_PROCESSED_METHODS.put("handleMessage", "(Landroid/os/Message;)V");

        EXCLUDE_FROM_VIEW_FILTER_INAMES = new HashSet<>();
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/AbsoluteLayout");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/AdapterView");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/app/FragmentBreadCrumbs");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/GridLayout");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/SlidingDrawer");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/Toolbar");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/media/tv/TvView");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/TextView");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/ImageView");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/AnalogClock");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/inputmethodservice/KeyboardView");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/app/MediaRouteButton");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/ProgressBar");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/view/SurfaceView");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/view/TextureView");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/view/ViewStub");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/DialerFilter");
        EXCLUDE_FROM_VIEW_FILTER_INAMES.add("android/widget/TwoLineListItem");

        FILTER_VIEW_METHODS = new HashMap<>();
        addMethodToFilter("performClick", "()Z");
        addMethodToFilter("onMeasure", "(II)V");
        addMethodToFilter("onDraw", "(Landroid/graphics/Canvas;)V");
        addMethodToFilter("onFinishInflate", "()V");
        addMethodToFilter("onLayout", "(ZIIII)V");
        addMethodToFilter("onSizeChanged", "(IIII)V");
        addMethodToFilter("onKeyDown", "(ILandroid/view/KeyEvent)Z");
        addMethodToFilter("onKeyUp", "(ILandroid/view/KeyEvent)Z");
        addMethodToFilter("onTrackballEvent", "(Landroid/view/MotionEvent)Z");
        addMethodToFilter("onTouchEvent", "(Landroid/view/MotionEvent)Z");
        addMethodToFilter("onFocusChanged", "(ZILandroid/graphics/Rect)V");
        addMethodToFilter("onWindowFocusChanged", "(Z)V");
        addMethodToFilter("onAttachedToWindow", "()V");
        addMethodToFilter("onDetachedFromWindow", "()V");
        addMethodToFilter("onWindowVisibilityChanged", "(I)V");
    }

    private static void addMethodToFilter(String name, String desc) {
        Set<String> methodDescs = FILTER_VIEW_METHODS.getOrDefault(name, new HashSet<>());
        methodDescs.add(desc);
        FILTER_VIEW_METHODS.put(name, methodDescs);
    }

    public static boolean isAutoProcessedMethod(MethodNode method,
                                                ClassNode classNode,
                                                Model model) {
        String methodDesc = FILTER_AUTO_PROCESSED_METHODS.get(method.name);
        return methodDesc != null
            && (methodDesc.equals(method.desc) ||
            model.hierarchicalClosure(classNode.name).contains(ASYNC_TASK_INAME))
            && FILTER_AUTO_PROCESSED_INAMES
            .stream()
            .map(x ->
                model.hierarchicalClosure(classNode.name).contains(x))
            .reduce(false, (x, y) -> x || y);

    }

    private static boolean hierarchyContainsViewGroupAndroidChild(Set<String> hierarchicalClosure) {
        return EXCLUDE_FROM_VIEW_FILTER_INAMES
                .stream()
                .map(hierarchicalClosure::contains)
                .reduce(false, (x, y) -> x || y);
    }

    public static boolean isViewClassChild(ClassNode classNode, Model model) {
        Set<String> hierarchicalClosure = model.hierarchicalClosure(classNode.name);
        return (hierarchicalClosure.contains(VIEW_INAME)
            || hierarchicalClosure.contains(VIEW_GROUP_INAME))
            && !hierarchyContainsViewGroupAndroidChild(hierarchicalClosure);

    }

    public static boolean isViewMethodToRename(String name, String desc) {
        for (Map.Entry<String, Set<String>> e: FILTER_VIEW_METHODS.entrySet()) {
            if (e.getKey().equals(name) && e.getValue().contains(desc)) {
                return true;
            }
        }
        return false;
    }
}
