// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for the FIDO-iot smoketest.
 */
public class TestUtil {

  /**
   * Send a command or series of commands to the command line.
   *
   * @param command the command(s) to be sent
   * @return the process executing the command(s)
   */
  public static Process executeCommand(String[] command) {
    TestLogger.info("Command: " + Arrays.toString(command));
    Process process = null;
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);
    try {
      process = pb.start();
    } catch (Exception e) {
      TestLogger.error("Exception thrown:  \nStack Trace:\n");
      e.printStackTrace();
      TestLogger.error("\nEnd Stack Trace\n\n");
    }
    return process;
  }

  /**
   * Determine if a file contains a specific string.
   *
   * @param file         string containing the name of the file to be searched
   * @param stringToFind the string to find
   * @param findFirst    boolean flag to find first and quit; else finds all occurrences of string
   * @return true if file contained at least one instance of string; else false
   * @throws IOException          Logs an error
   * @throws InterruptedException Logs an error
   */
  public static boolean fileContainsString(String file, String stringToFind, boolean findFirst)
      throws IOException, InterruptedException {
    boolean stringFound = false;
    Pattern pattern = Pattern.compile(stringToFind);
    Matcher matcher = pattern.matcher("");
    BufferedReader br = null;
    String line;
    try {
      br = new BufferedReader(new FileReader(file));
    } catch (IOException e) {
      TestLogger.error("Cannot read '" + file + "': " + e.getMessage());
    }
    try {
      while ((line = br.readLine()) != null) {
        matcher.reset(line);
        if (matcher.find()) {
          stringFound = true;
          TestLogger.info("String found in file:  " + file + " in line containing: " + line);
          if (findFirst) {
            break;
          }
        }
      }
    } catch (IOException e) {
      TestLogger.error("Unable to find string '" + stringToFind + "'\n" + e.getStackTrace());
    }
    br.close();

    return stringFound;
  }
}
