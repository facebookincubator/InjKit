// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.gradle;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

class LocalClassLoader extends URLClassLoader {
  private final ClassLoader delegate;

  // loadClass(String, boolean) is protected in ClassLoader: make it available for us to call.
  private final LoadClassCall delegateLoadClassCall;

  LocalClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, findTopmostClassLoader());

    AtomicReference<LoadClassCall> loadClassCallHolder = new AtomicReference<>();
    new URLClassLoader(new URL[0], parent) {
      {
        loadClassCallHolder.set(this::loadClass);
      }
    };

    delegateLoadClassCall = loadClassCallHolder.get();
    delegate = parent;
  }

  private static ClassLoader findTopmostClassLoader() {
    ClassLoader rootClassLoader = ClassLoader.getSystemClassLoader();
    while (rootClassLoader.getParent() != null) {
      rootClassLoader = rootClassLoader.getParent();
    }

    return rootClassLoader;
  }

  @Override
  protected Class<?> loadClass(String name, boolean initialize) throws ClassNotFoundException {
    try {
      return super.loadClass(name, initialize);
    } catch (ClassNotFoundException e) {
      return delegateLoadClassCall.loadClass(name, initialize);
    }
  }

  @Override
  public URL getResource(String s) {
    URL found = super.getResource(s);
    if (found == null) {
      found = delegate.getResource(s);
    }

    return found;
  }

  interface LoadClassCall {
    Class<?> loadClass(String name, boolean initialize) throws ClassNotFoundException;
  }
}
