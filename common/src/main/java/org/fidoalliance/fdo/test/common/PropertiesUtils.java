// Copyright 2021 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

  /**
   * Returns the CSV data in String[][] format.
   */
  public static String getProperty(String filePath, String key) throws IOException {
    InputStream input = new FileInputStream(filePath);
    Properties prop = new Properties();
    prop.load(input);
    String value = prop.getProperty(key);
    if (value.isBlank()) {
      throw new IOException("Property " + key + "not found.");
    }
    return prop.getProperty(key);
  }
}
