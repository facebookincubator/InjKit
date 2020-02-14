// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class KeepsTrackOfAnnotationDefaultValuesTest {
    private final Model model = ModelFactory.defaultFactory().make();

    @Before
    public void before() {
        model.update(
                AsmCreationUtils.addMethods(
                        AsmCreationUtils.makeAnnotationClass("my/Ann"),
                        AsmCreationUtils.makeMethod(
                                "aString",
                                "()Ljava/lang/String;",
                                Opcodes.ACC_PUBLIC,
                                Collections.emptyList(),
                                Collections.emptyList(),
                                "bar"),
                        AsmCreationUtils.makeMethod(
                                "aInt",
                                "()I",
                                Opcodes.ACC_PUBLIC,
                                Collections.emptyList(),
                                Collections.emptyList(),
                                42)));
        model.update(AsmCreationUtils.makeClass("my/Cls", "java/lang/Object"));
    }

    @Test
    public void knowsPresentedAnnotation() {
        assertThat(model.knowsAnnotation("Lmy/Ann;")).isTrue();
    }

    @Test
    public void doesNotKnowNonPresentedAnnotation() {
        assertThat(model.knowsAnnotation("Lmy/AnnX;")).isFalse();
    }

    @Test
    public void obtainsStringValueOfPropertyOfPresentedAnnotationThatHasValue() {
        assertThat(model.annotationDefaultValue("Lmy/Ann;", "aString")).isEqualTo("bar");
    }

    @Test
    public void obtainsIntValueOfPropertyOfPresentedAnnotationThatHasValue() {
        assertThat(model.annotationDefaultValue("Lmy/Ann;", "aInt")).isEqualTo(42);
    }

    @Test
    public void canTryToGetNonExistentPropertyValueOfAnnotationThatIsKnown() {
        assertThat(model.annotationDefaultValue("Lmy/Ann;", "aaa")).isNull();
    }

    @Test
    public void cannotTryToGetPropertyValueOfNotKnownAnnotation() {
        try {
            model.annotationPropertyOfClass("my/Cls", "Lmy/AnnX;", "aString");
            fail();
        } catch (IllegalStateException e) {
            // Expected.
        }
    }

    @Test
    public void cannotTryToGetPropertyValueOfClassThatIsNotAnnotation() {
        try {
            model.annotationDefaultValue("Lmy/Cls;", "aString");
            fail();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
    }

    @Test
    public void obtainsAnnotationPropertyTypes() {
        assertThat(model.annotationProperties("Lmy/Ann;")).containsExactly(
                "aString", Type.getType("Ljava/lang/String;"),
                "aInt", Type.INT_TYPE);
    }

    @Test
    public void cannotObtainAnnotationPropertyTypesIfAnnotationNotKnown() {
        try {
            model.annotationProperties("Lmy/AnnX;");
            fail();
        } catch (IllegalStateException e) {
            // Expected.
        }
    }

    @Test
    public void cannotObtainAnnotationPropertyTypesIfClassIsNotAnnotation() {
        try {
            model.annotationProperties("Lmy/cls;");
            fail();
        } catch (IllegalStateException e) {
            // Expected.
        }
    }
}
