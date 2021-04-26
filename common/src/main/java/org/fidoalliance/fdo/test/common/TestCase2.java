// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test.common;

import java.lang.reflect.Method;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

/**
 * Base class and methods for helpers and tt.
 */
public abstract class TestCase2 extends TestCase {

  protected String runDir = null;
  protected String testDir = null; // Directory holding executables being tested.

  /**
   * Return a String containing the name and parameters of an &#x40;Test method, in the same format
   * as the TestNG PASSED and FAILED messages. As with those messages, the output is intended for
   * human consumption and may not be syntactically correct Java -- e.g. embedded " characters in
   * String values are not escaped.
   *
   * @param m      - the Method descriptor, from Reflection
   * @param params - the parameters
   * @return methodname
   */
  public String methodMessage(Method m, Object[] params) {
    String s = m.getName() + "(";
    for (int i = 0; i < params.length; ++i) {
      if (params[i] instanceof String) {
        s = s + "\"" + params[i] + "\"";
      } else {
        s = s + params[i];
      }
      if (i < params.length - 1) {
        s = s + ", ";
      }
    }
    return (s + ")");
  }

  /**
   * Log the start of an &#x40;Test method.
   *
   * @param m      - the Method descriptor, from Reflection
   * @param params - the parameters
   */
  @BeforeMethod(alwaysRun = true)
  public void beforeMethod(Method m, Object[] params) {
    TestLogger.info("STARTING: " + methodMessage(m, params));
  }

  /**
   * Log the completion of an &#x40;Test method. This is somewhat redundant with the
   * PASSED/FAILED/SKIPPED message from TestNG, but this one appears inline in the body of the
   * logfile rather than at the end.
   *
   * @param m      - the Method descriptor, from Reflection
   * @param params - the parameters
   * @param res    - the result descriptor
   */
  @AfterMethod(alwaysRun = true)
  public void afterMethod(Method m, Object[] params, ITestResult res) {
    String s = null;
    if (res == null) {
      s = "SKIPPED: ";
    } else {
      switch (res.getStatus()) {
        case ITestResult.FAILURE:
          s = "FAILED: ";
          break;
        case ITestResult.SKIP:
          s = "SKIPPED: ";
          break;
        case ITestResult.SUCCESS:
          s = "PASSED: ";
          break;
        default:
          s = "unexpected status " + res.getStatus() + ": ";
          break;
      }
    }
    TestLogger.info(s + methodMessage(m, params));
  }

  /**
   * Setup test-suite related variables.
   */
  @BeforeClass(alwaysRun = true)
  public void beforeClass() {
    TestLogger.info("Before Class");

    testDir = System.getenv("TEST_DIR");
    if (testDir != null) {
      testDir = testDir.replaceAll("\\\\", "/");
      runDir = testDir;
      TestLogger.info("Execution directory: " + runDir);
    } else {
      TestLogger.error("Environment variable TEST_DIR was "
          + "not set properly. See 'Set up your SDET Environment' on the Marshal Point"
          + " wiki for more information.");
    }
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    TestLogger.info("After Class");
  }

  /**
   * Executes before every test.
   */
  @BeforeTest(alwaysRun = true)
  public void beforeTest() {
    TestLogger.info("================================="
        + "=========================");
    TestLogger.info("Before Test");
  }

  @AfterTest(alwaysRun = true)
  public void afterTest() {
    TestLogger.info("After Test");
  }

  /**
   * Define execution and logging directories based on OS.
   */
  @BeforeSuite(alwaysRun = true)
  public void beforeSuite() {
    TestLogger.info("Before Suite");
  }

  @AfterSuite(alwaysRun = true)
  public void afterSuite() {
    TestLogger.info("After Suite");
  }

}
