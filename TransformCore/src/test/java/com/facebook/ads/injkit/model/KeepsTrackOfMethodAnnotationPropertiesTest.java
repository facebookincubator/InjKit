// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.


package com.facebook.ads.injkit.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.Opcodes;

import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class KeepsTrackOfMethodAnnotationPropertiesTest {
    private final Model model = ModelFactory.defaultFactory().make();

    @Before
    public void before() {
        model.update(
                AsmCreationUtils.addMethods(
                        AsmCreationUtils.makeClass("cls/with/Methods", "java/lang/Object"),
                        AsmCreationUtils.makeMethodWithAnnotations(
                                "withStringAnn",
                                "()V",
                                Opcodes.ACC_PUBLIC,
                                Collections.singletonList(
                                        AsmCreationUtils.makeAnnotation("LSA;", "val", "boom")),
                                Collections.emptyList(),
                                null),
                        AsmCreationUtils.makeMethodWithAnnotations(
                                "withIntAnn",
                                "()V",
                                Opcodes.ACC_PUBLIC,
                                Collections.singletonList(
                                        AsmCreationUtils.makeAnnotation("LIA;", "val", 5)),
                                Collections.emptyList(),
                                null)));
        model.update(
                AsmCreationUtils.addMethods(
                        AsmCreationUtils.makeClass("subcls/with/Methods", "cls/with/Methods"),
                        AsmCreationUtils.makeMethodWithAnnotations(
                                "withStringAnn",
                                "()V",
                                Opcodes.ACC_PUBLIC,
                                Collections.singletonList(
                                        AsmCreationUtils.makeAnnotation("LSA;", "val", "booom")),
                                Collections.emptyList(),
                                null)));
        model.update(
                AsmCreationUtils.addMethods(
                        AsmCreationUtils.makeClass("subcls/with/MissingProps", "cls/with/Methods"),
                        AsmCreationUtils.makeMethodWithAnnotations(
                                "withStringAnn",
                                "()V",
                                Opcodes.ACC_PUBLIC,
                                Collections.singletonList(AsmCreationUtils.makeAnnotation("LSA;")),
                                Collections.emptyList(),
                                null)));
    }

    @Test
    public void findStringProperty() {
        assertThat(
                model.annotationPropertyOfMethod(
                        "cls/with/Methods",
                        "withStringAnn",
                        "()V",
                        Opcodes.ACC_PUBLIC,
                        "LSA;",
                        "val"))
                .isEqualTo("boom");
    }

    @Test
    public void findIntegerProperty() {
        assertThat(
                model.annotationPropertyOfMethod(
                        "cls/with/Methods",
                        "withIntAnn",
                        "()V",
                        Opcodes.ACC_PUBLIC,
                        "LIA;",
                        "val"))
                .isEqualTo(5);
    }

    @Test
    public void obtainingNonExistentProperty() {
        assertThat(
                model.annotationPropertyOfMethod(
                        "cls/with/Methods",
                        "withStringAnn",
                        "()V",
                        Opcodes.ACC_PUBLIC,
                        "LSA;",
                        "vall"))
                .isNull();
    }

    @Test
    public void obtainingPropertyOfNonExistingAnnotation() {
        try {
            model.annotationPropertyOfMethod(
                    "cls/with/Methods",
                    "withStringAnn",
                    "()V",
                    Opcodes.ACC_PUBLIC,
                    "LSB;",
                    "val");
            fail();
        } catch (IllegalStateException e) {
            // Expected.
        }
    }

    @Test
    public void closureWithAnnotationProperties() {
        assertThat(
                model.methodClosureWithAnnotationFilterAndValue(
                        "subcls/with/Methods",
                        "withStringAnn",
                        "()V",
                        Opcodes.ACC_PUBLIC,
                        "LSA;",
                        "val"))
                .containsExactly("subcls/with/Methods", "booom", "cls/with/Methods", "boom");
    }

    @Test
    public void closureWithAnnotationPropertiesWithMissingValues() {
        assertThat(
                model.methodClosureWithAnnotationFilterAndValue(
                        "subcls/with/MissingProps",
                        "withStringAnn",
                        "()V",
                        Opcodes.ACC_PUBLIC,
                        "LSA;",
                        "val"))
                .containsExactly("cls/with/Methods", "boom");
    }
}
