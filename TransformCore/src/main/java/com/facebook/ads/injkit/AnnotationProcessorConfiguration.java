// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AnnotationProcessorConfiguration {
  private final Set<ModuleHandler<?>> handlers;

  private AnnotationProcessorConfiguration(Set<ModuleHandler<?>> handlers) {
    this.handlers = handlers;
  }

  Collection<Injector> makeInjectors(URLClassLoader applicationCode)
      throws InvalidAnnotationProcessorConfigurationException {
    Set<Injector> injectors = new HashSet<>();
    for (ModuleHandler<?> handler : handlers) {
      injectors.add(handler.makeInjector(applicationCode));
    }

    return injectors;
  }

  public static AnnotationProcessorConfiguration parse(
      File configurationFile, Iterable<Module<?>> modules)
      throws IOException, InvalidAnnotationProcessorConfigurationException {

    Set<ModuleHandler<?>> handlers = new HashSet<>();
    for (Module<?> module : modules) {
      handlers.add(new ModuleHandler<>(module));
    }

    ParseContext ctx = new ParseContext(configurationFile.getCanonicalPath());

    try (FileReader configurationFileReader = new FileReader(configurationFile);
        BufferedReader lineReader = new BufferedReader(configurationFileReader)) {
      String line;
      lineReading:
      while ((line = lineReader.readLine()) != null) {
        ctx.nextLine();

        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        LineDirectiveSplit directiveSplit = splitDirective(line);
        for (ModuleHandler<?> moduleHandler : handlers) {
          if (moduleHandler.getParser().parse(directiveSplit, ctx)) {
            continue lineReading;
          }
        }

        throw new InvalidAnnotationProcessorConfigurationException(
            String.format(
                Locale.US,
                "%s: Unknown directive: '%s'",
                ctx.lineDescription(),
                directiveSplit.getDirective()));
      }
    }

    for (ModuleHandler<?> moduleHandler : handlers) {
      moduleHandler.finish(ctx);
    }

    return new AnnotationProcessorConfiguration(handlers);
  }

  private static LineDirectiveSplit splitDirective(String line) {
    String[] lineSplit = line.split("\\s+", 2);
    if (lineSplit.length == 1) {
      return new LineDirectiveSplit(lineSplit[0], "");
    } else {
      return new LineDirectiveSplit(lineSplit[0], lineSplit[1]);
    }
  }

  private static class ModuleHandler<ConfigurationT> {
    private final Module<ConfigurationT> module;
    private final ConfigurationParser<ConfigurationT> parser;
    private ConfigurationT configuration;

    ModuleHandler(Module<ConfigurationT> module) {
      this.module = module;
      parser = module.makeConfigurationParser();
    }

    ConfigurationParser<ConfigurationT> getParser() {
      return parser;
    }

    void finish(ParseContext ctx) throws InvalidAnnotationProcessorConfigurationException {
      configuration = parser.finish(ctx);
    }

    Injector makeInjector(URLClassLoader applicationCode)
        throws InvalidAnnotationProcessorConfigurationException {
      return module.makeInjectorFactory().make(applicationCode, configuration);
    }
  }
}
