// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.ads.injkit.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandDescription {
  private static final Pattern COMMAND_PATTERN = Pattern.compile("--([^=]+)(?:=(.*))");

  private static final String INPUT_COMMAND = "input";
  private static final String OUTPUT_COMMAND = "output";
  private static final String CONFIG_COMMAND = "config";
  private static final String CLASSPATH_COMMAND = "classpath";

  private final File inputFile;
  private final File outputFile;
  private final File configFile;
  private final List<File> classpath;

  private CommandDescription(
      File inputFile, File outputFile, File configFile, List<File> classpath) {
    this.inputFile = inputFile;
    this.outputFile = outputFile;
    this.configFile = configFile;
    this.classpath = new ArrayList<>(classpath);
  }

  public static CommandDescription parse(String[] args) throws CliException {
    File inputFile = null;
    File outputFile = null;
    File configFile = null;
    List<File> classpath = new ArrayList<>();

    for (int i = 0; i < args.length; i++) {
      ArgumentNameAndValue argumentNameAndValue = parseArgument(args[i]);
      switch (argumentNameAndValue.getName()) {
        case INPUT_COMMAND:
          if (inputFile != null) {
            throw new CliException("'%s' argument specified more than once", INPUT_COMMAND);
          }

          inputFile = new File(argumentNameAndValue.getValue());
          break;
        case OUTPUT_COMMAND:
          if (outputFile != null) {
            throw new CliException("'%s' argument specified more than once", OUTPUT_COMMAND);
          }

          outputFile = new File(argumentNameAndValue.getValue());
          break;
        case CONFIG_COMMAND:
          if (configFile != null) {
            throw new CliException("'%s' argument specified more than once", CONFIG_COMMAND);
          }

          configFile = new File(argumentNameAndValue.getValue());
          break;
        case CLASSPATH_COMMAND:
          for (String element :
              argumentNameAndValue.getValue().split(System.getProperty("path.separator"))) {
            classpath.add(new File(element));
          }

          break;
        default:
          throw new CliException("Unknown argument '%s'", argumentNameAndValue.getName());
      }
    }

    if (inputFile == null) {
      throw new CliException("'%s' argument not specified", INPUT_COMMAND);
    }

    if (outputFile == null) {
      throw new CliException("'%s' argument not specified", OUTPUT_COMMAND);
    }

    if (configFile == null) {
      throw new CliException("'%s' argument not specified", CONFIG_COMMAND);
    }

    return new CommandDescription(inputFile, outputFile, configFile, classpath);
  }

  public File getInputFile() {
    return inputFile;
  }

  public File getOutputFile() {
    return outputFile;
  }

  public File getConfigFile() {
    return configFile;
  }

  public List<File> getClasspath() {
    ArrayList<File> classpath = new ArrayList<>();
    classpath.addAll(this.classpath);
    classpath.add(inputFile);
    return classpath;
  }

  private static ArgumentNameAndValue parseArgument(String argument) throws CliException {
    Matcher matcher = COMMAND_PATTERN.matcher(argument);
    if (!matcher.matches()) {
      throw new CliException(String.format(Locale.US, "Invalid argument format: '%s'", argument));
    }

    return new ArgumentNameAndValue(matcher.group(1), matcher.group(2));
  }

  private static class ArgumentNameAndValue {
    private final String name;
    private final String value;

    ArgumentNameAndValue(String name, String value) {
      this.name = name;
      this.value = value;
    }

    String getName() {
      return name;
    }

    String getValue() {
      return value;
    }
  }
}
