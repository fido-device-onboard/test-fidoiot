// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
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
 * Define default test case and associated methods.
 */
public abstract class TestCase {

  protected static final String resultFile = "result.txt";
  //Iot Platform SDK Docker commands
  protected static final String runDockerService = "docker-compose up";
  protected static final String downDockerService = "docker-compose down";
  // Spring arguments
  protected static final String D_JAVA_LIBRARY_PATH = "-Djava.library.path=";
  // Environment constants
  protected static final String osName = System.getProperty("os.name");
  protected static final String osVersion = System.getProperty("os.version");
  // Ordinals for test arguments - used for clean-up.
  private static final int ENABLED = 1;
  // Common folders and files
  private static final String LOGBACK_SPRING_XML = "logback-spring.xml";
  private static final String TEST_FILES = "testFiles";
  private static final String KEY_FILES = "/" + TEST_FILES + "/keys";
  private static final String SI_FILES = "/" + TEST_FILES + "/serviceInfoData";
  private static final String USER_DIR = "user.dir";
  // In the build environment, parentDir should be the same as TEST_DIR but it is needed for the dev
  // environment where the executables are in a separate directory pointed to by TEST_DIR.
  private static final String parentDir = Paths.get(System.getProperty(USER_DIR)).getParent()
      .toString();
  protected static final String keyDir = parentDir + KEY_FILES;
  protected static final String serviceInfoDir = parentDir + SI_FILES;
  protected String logDir = null; // Directory for application logs.
  protected String testDir = null; // Directory holding executables being tested.
  protected String ownerProxyDir = null;  //Directory from which owner will load ownership proxy.
  protected String deviceOutputDir = null; // Directory where device will save ownership proxy
  protected String deviceOutputDir2 = null; // Directory where device will save ownership proxy
  protected String mfgOutputDir = null; // Directory where manufacturer will save ownership proxy
  protected String ownerOutputDir = null; // Directory where owner will save ownership proxy

  // A very short wait to complete TO0 after it is scheduled
  protected Duration shortTimeout = Duration.of(5, ChronoUnit.SECONDS);

  // How long to wait for the device to finish the protocol
  protected Duration longTimeout = Duration.of(70, ChronoUnit.SECONDS);

  // How long to wait for Docker services to shut down
  protected Duration dockerDownTimeout = Duration.of(20, ChronoUnit.SECONDS);

  // How long to wait for FIDO Docker services to start up
  protected Duration fdoDockerUpTimeout = Duration.of(480, ChronoUnit.SECONDS);

  // How long to wait for PRI-FIDO T00
  protected Duration fdoToWait = Duration.of(45, ChronoUnit.SECONDS);

  /**
   * Get the IP address of the server.
   *
   * @return String containing IP address
   */
  protected static String getServerIpAddress() throws UnknownHostException, SocketException {
    try (final DatagramSocket socket = new DatagramSocket()) {
      socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
      return socket.getLocalAddress().getHostAddress();
    }
  }

  /**
   * Write to log so call flow can be readily determined.
   */
  @AfterClass(alwaysRun = true)
  public void afterClass() {
    TestLogger.info("After Class");
  }

  @BeforeTest(alwaysRun = true)
  public void beforeTest() {
    TestLogger.info("Before Test");
  }

  @AfterTest(alwaysRun = true)
  public void afterTest() {
    TestLogger.info("After Test");
  }

  @BeforeSuite(alwaysRun = true)
  public void beforeSuite() {
    TestLogger.info("Before Suite");
  }

  @AfterSuite(alwaysRun = true)
  public void afterSuite() {
    TestLogger.info("After Suite");
  }

  /**
   * Set up logging for the test.
   */
  @BeforeClass(alwaysRun = true)
  public void beforeClass() {
    TestLogger.info("Before Class");
    testDir = System.getenv("TEST_DIR");
    if (testDir != null) {
      testDir = testDir.replaceAll("\\\\", "/");
      TestLogger.info("Execution directory: " + testDir);
      logDir = testDir + "/logs";
      File directory = new File(logDir);
      if (!directory.exists()) {
        directory.mkdir();
        TestLogger.info("Created directory: " + logDir);
      }

      ownerProxyDir = testDir + "/tmp/ownerProxyDir";
      File ownerProxyDirectory = new File(ownerProxyDir);
      if (!ownerProxyDirectory.exists()) {
        ownerProxyDirectory.mkdirs();
        TestLogger.info("Created directory: " + ownerProxyDir);
      }

      deviceOutputDir = testDir + "/tmp/deviceOutputDir";
      File deviceOutputDirectory = new File(deviceOutputDir);
      if (!deviceOutputDirectory.exists()) {
        deviceOutputDirectory.mkdirs();
        TestLogger.info("Created directory: " + deviceOutputDir);
      }

      deviceOutputDir2 = testDir + "/tmp/deviceOutputDir2";
      File deviceOutputDirectory2 = new File(deviceOutputDir2);
      if (!deviceOutputDirectory2.exists()) {
        deviceOutputDirectory2.mkdirs();
        TestLogger.info("Created directory: " + deviceOutputDir2);
      }

      mfgOutputDir = testDir + "/tmp/mfgOutputDir";
      File mfgOutputDirectory = new File(mfgOutputDir);
      if (!mfgOutputDirectory.exists()) {
        mfgOutputDirectory.mkdirs();
        TestLogger.info("Created directory: " + mfgOutputDir);
      }

      ownerOutputDir = testDir + "/tmp/ownerOutputDir";
      File ownerOutputDirectory = new File(ownerOutputDir);
      if (!ownerOutputDirectory.exists()) {
        ownerOutputDirectory.mkdirs();
        TestLogger.info("Created directory: " + ownerOutputDir);
      }

      TestLogger.info("Application log directory:  " + logDir);
      TestLogger.info("proxyDir:  " + ownerProxyDir);
      TestLogger.info("deviceOutputDir:  " + deviceOutputDir);
      TestLogger.info("deviceOutputDir2:  " + deviceOutputDir2);
      TestLogger.info("mfgOutputDir:  " + mfgOutputDir);
      TestLogger.info("ownerOutputDir:  " + ownerOutputDir);
    } else {
      TestLogger.error("Environment variable TEST_DIR was not set properly.  "
          + "See 'Set up your SDET Environment' on the SDO wiki for more information.");
    }
  }

  /**
   * Log the start of a Test method.
   *
   * @param m      - the Method descriptor, from Reflection
   * @param params - the parameters
   */
  @BeforeMethod(alwaysRun = true)
  public void beforeMethod(Method m, Object[] params) {
    TestLogger.info("=================================================================");
    TestLogger.info("STARTING: " + methodMessage(m, params));
  }

  /**
   * Clean up data left by the test methods. Used for all device types.
   */
  @AfterMethod(groups = {"fdo_pri_smoketest", "fdo_clientsdk_smoketest"})
  public void afterDeviceCleanup(Object[] parameters) throws IOException, InterruptedException {
    TestLogger.info("=====> AfterMethod: afterDevice removing .oc & .op files.");
    if (parameters.length > 0) {

      String enabled = (String) parameters[ENABLED];

      if (enabled.equals("true")) {
        Path testPath = Paths.get(testDir);
        TestLogger.info("Removing device test artifacts.");
        TestLogger.info("Operating System: " + osName);
        TestLogger.info("enabled: " + enabled);

        String[] winString = new String[]{"cmd",
            "/c",
            "ECHO ON && ECHO. && ECHO ===== Before ===== "
                + " && ECHO deviceOutputDir && DIR tmp\\deviceOutputDir"
                + " && ECHO deviceOutputDir2 && DIR tmp\\deviceOutputDir2"
                + " && ECHO mfgOutputDir && DIR tmp\\mfgOutputDir"
                + " && ECHO ownerOutputDir && DIR tmp\\ownerOutputDir"
                + " && ECHO ownerProxyDir && DIR tmp\\ownerProxyDir"
                + " && (IF EXIST tmp\\deviceOutputDir\\*.oc (ECHO === OWNERSHIP CREDENTIAL ==="
                + " && TYPE tmp\\deviceOutputDir\\*.oc"
                + " && DEL /F tmp\\deviceOutputDir\\*.oc))"
                + " && (IF EXIST tmp\\deviceOutputDir2\\*.oc (ECHO === OWNERSHIP CREDENTIAL2 ==="
                + " && TYPE tmp\\deviceOutputDir2\\*.oc"
                + " && DEL /F tmp\\deviceOutputDir2\\*.oc))"
                + " && (IF EXIST tmp\\mfgOutputDir\\*.op (ECHO === OWNERSHIP PROXY ==="
                + " && TYPE tmp\\mfgOutputDir\\*.op"
                + " && DEL /F tmp\\mfgOutputDir\\*.op))"
                + " && (IF EXIST tmp\\ownerProxyDir\\*.op (ECHO === OWNERSHIP PROXY EXTENDED ==="
                + " && TYPE tmp\\ownerProxyDir\\*.op"
                + " && DEL /F tmp\\ownerProxyDir\\*.op))"
                + " && ECHO. && ECHO ===== After ===== && DIR && ECHO."
                + " && ECHO deviceOutputDir && DIR tmp\\deviceOutputDir"
                + " && ECHO deviceOutputDir2 && DIR tmp\\deviceOutputDir2"
                + " && ECHO mfgOutputDir && DIR tmp\\mfgOutputDir"
                + " && ECHO ownerOutputDir && DIR tmp\\ownerOutputDir"
                + " && ECHO ownerProxyDir && DIR tmp\\ownerProxyDir"};
        String[] linuxString = new String[]{"/bin/bash",
            "-c",
            "printf \"\n== deviceOutputDir ==\n\" && ls -la " + deviceOutputDir
                + " 2>/dev/null || true"
                + " && printf \"\n== deviceOutputDir2 ==\n\" && ls -la "
                + deviceOutputDir2 + " 2>/dev/null || true"
                + " && printf \"\n== mfgOutputDir ==\n\" && ls -la " + mfgOutputDir
                + " 2>/dev/null || true"
                + " && printf \"\n== ownerOutputDir ==\n\" && ls -la " + ownerOutputDir
                + " 2>/dev/null || true"
                + " && printf \"\n== ownerProxyDir ==\n\" && ls -la " + ownerProxyDir
                + " 2>/dev/null || true"
                + " && if (test -a " + deviceOutputDir + "/*.oc);"
                + "   then printf \"\n== OWNERSHIP CREDENTIAL ==\n\""
                + "   && cat " + deviceOutputDir + "/*.oc && rm -f " + deviceOutputDir + "/*.oc; fi"
                + " && if (test -a " + deviceOutputDir2 + "/*.oc);"
                + "   then printf \"\n\n== OWNERSHIP CREDENTIAL2 ==\n\""
                + "   && cat " + deviceOutputDir2 + "/*.oc && rm -f " + deviceOutputDir2
                + "/*.oc; fi"
                + " && if (test -a " + mfgOutputDir
                + "/*.op); then printf \"\n\n== OWNERSHIP PROXY ==\n\""
                + "   && cat " + mfgOutputDir + "/*.op && rm -f " + mfgOutputDir + "/*.op; fi"
                + " && if (test -a " + ownerProxyDir + "/*.op);"
                + "   then printf \"\n\n== OWNERSHIP PROXY EXTENDED ==\n\""
                + "   && cat " + ownerProxyDir + "/*.op && rm -f " + ownerProxyDir + "/*.op; fi"
                + " && rm -f " + deviceOutputDir + "/* && printf \"\n\n== deviceOutputDir ==\n\""
                + " && ls -la " + deviceOutputDir + " 2>/dev/null || true"
                + " && rm -f " + deviceOutputDir2 + "/* && printf \"\n== deviceOutputDir2 ==\n\""
                + " && ls -la " + deviceOutputDir2 + " 2>/dev/null || true"
                + " && rm -f " + mfgOutputDir + "/* && printf \"\n== manufacturerOutputDir ==\n\""
                + " && ls -la " + mfgOutputDir + " 2>/dev/null || true"
                + " && rm -f " + ownerOutputDir + "/* && printf \"\n== ownerOutputDir ==\n\""
                + " && ls -la " + ownerOutputDir + " 2>/dev/null || true"
                + " && rm -f " + ownerProxyDir + "/* && printf \"\n== ownerProxyDir ==\n\""
                + " && ls -la " + ownerProxyDir + " 2>/dev/null || true"};

        String[] cleanUpCmd = (osName.contains("Windows")) ? winString : linuxString;

        TestProcess cleanUp = new TestProcess(testPath, cleanUpCmd);
        int cleanUpResult = -1;
        try (TestProcess.Handle hCleanUp = cleanUp.start()) {
          if (hCleanUp.waitFor(longTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
            cleanUpResult = hCleanUp.exitValue();
          }
        }

        // Confirm clean-up successfully completed.
        Assert.assertEquals(
            cleanUpResult,
            0,
            "ERROR: afterDeviceCleanUp did not exit properly. Exit value: " + cleanUpResult);
        TestLogger.info("afterDeviceCleanUp completed successfully.  Exit value: " + cleanUpResult);
      }
    }
  }

  /**
   * Log the completion of a Test method. This is somewhat redundant with the PASSED/FAILED/SKIPPED
   * message from TestNG, but this one appears in-line in the body of the log file rather than at
   * the end.
   *
   * @param m      : the Method descriptor, from Reflection
   * @param params : the parameters
   * @param res    : the result descriptor
   */
  @AfterMethod(alwaysRun = true)
  public void logTestResults(Method m, Object[] params, ITestResult res) {
    TestLogger.info("AfterMethod: logTestResults.");
    String s;
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
    TestLogger.info("=================================================================");
  }

  /**
   * Return a String containing the name and parameters of a Test method in the same format as the
   * TestNG PASSED and FAILED messages. As with those messages, the output is intended for human
   * consumption and may not be syntactically correct Java -- e.g. embedded " characters in String
   * values are not escaped.
   *
   * @param m - the Method descriptor, from Reflection= - the parameters
   */
  private String methodMessage(Method m, Object[] params) {
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

}
