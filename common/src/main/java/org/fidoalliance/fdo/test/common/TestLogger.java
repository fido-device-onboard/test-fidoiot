// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This module serves to isolate log4j from the rest of the test framework so another logging
 * library could be incorporated should the need arise.
 *
 * @author Karen Herrold
 */
public class TestLogger {

  public static Logger logger = LogManager.getLogger();

  public static void debug(String message) {
    logger.debug(message);
  }

  public static void error(String message) {
    logger.error(message);
  }

  public static void fatal(String message) {
    logger.fatal(message);
  }

  public static void info(String message) {
    logger.info(message);
  }

  public static void trace(String message) {
    logger.trace(message);
  }

  public static void warn(String message) {
    logger.warn(message);
  }
}
