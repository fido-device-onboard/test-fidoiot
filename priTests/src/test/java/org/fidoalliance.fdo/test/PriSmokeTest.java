// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.fidoalliance.fdo.test.common.CsvUtils;
import org.fidoalliance.fdo.test.common.PropertiesUtils;
import org.fidoalliance.fdo.test.common.TestCase;
import org.fidoalliance.fdo.test.common.TestLogger;
import org.fidoalliance.fdo.test.common.TestProcess;
import org.fidoalliance.fdo.test.common.TestUtil;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;
import org.testng.annotations.Test;


/**
 * CI SmokeTest for PRI-FIDOIOT
 */
public class PriSmokeTest extends TestCase {

  @DataProvider(name = "FdoTestData")
  public static Object[][] getData() throws Exception {
    TestLogger.info("=====> Reading csv data file.");
    String dataFile = "priTest.csv";
    URL resource = PriSmokeTest.class.getClassLoader().getResource(dataFile);
    Assert.assertNotNull(resource, "File " + dataFile + " does not exist;");
    String resourcePath = Paths.get(resource.toURI()).toString();
    TestLogger.info("=====> resourcePath: " + Paths.get(resource.toURI()));
    return CsvUtils.getDataArray(resourcePath);
  }

  /**
   * This method is used to start docker container for PRI-FIDO components.
   */
  @BeforeGroups("fdo_pri_smoketest")
  public void startFdoDockerService() throws IOException, InterruptedException {
    TestLogger.info("=====> Starting FDO Docker Service");

//    try {
//      mfgApiPass = PropertiesUtils.getProperty(
//          testDir + "/binaries/pri-fidoiot/manufacturer/creds.env",
//          "manufacturer_api_password");
//
//      ownerApiPass = PropertiesUtils.getProperty(
//          testDir + "/binaries/pri-fidoiot/owner/creds.env",
//          "owner_api_password");
//    } catch (IOException ex) {
//      Assert.fail("Couldn't read API Password.");
//      throw ex;
//    }

    Path mfgDockerPath = Paths.get(testDir + "/binaries/pri-fidoiot/manufacturer");
    Path ownerDockerPath = Paths.get(testDir + "/binaries/pri-fidoiot/owner");
    Path rvDockerPath = Paths.get(testDir + "/binaries/pri-fidoiot/rv");
    Path aioDockerPath = Paths.get(testDir + "/binaries/pri-fidoiot/aio");

    try {
      TestProcess.execute_dockerCmd(mfgDockerPath.toString(), runDockerService + " --build ");
      TestProcess.execute_dockerCmd(rvDockerPath.toString(), runDockerService + " --build ");
      TestProcess.execute_dockerCmd(ownerDockerPath.toString(), runDockerService + " --build ");
      TestProcess.execute_dockerCmd(aioDockerPath.toString(), runDockerService + " --build ");
      Thread.sleep(fdoDockerUpTimeout.toMillis());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is used to stop docker container for PRI-FIDO components.
   */
  @AfterGroups("fdo_pri_smoketest")
  public void stopFdoDockerService() {
    TestLogger.info("=====> Stopping FDO Docker Service");
    Path mfgDockerPath = Paths.get(testDir + "/binaries/pri-fidoiot/manufacturer");
    Path ownerDockerPath = Paths.get(testDir + "/binaries/pri-fidoiot/owner");
    Path rvDockerPath = Paths.get(testDir + "/binaries/pri-fidoiot/rv");
    Path aioDockerPath = Paths.get(testDir + "/binaries/pri-fidoiot/aio");
    try {
      TestProcess.execute_dockerCmd(mfgDockerPath.toString(), downDockerService);
      TestProcess.execute_dockerCmd(ownerDockerPath.toString(), downDockerService);
      TestProcess.execute_dockerCmd(rvDockerPath.toString(), downDockerService);
      TestProcess.execute_dockerCmd(aioDockerPath.toString(), downDockerService);
      Thread.sleep(dockerDownTimeout.toMillis());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void componentSampleTest(String sviEnabled) throws Exception {

    TestLogger.info("=====> testDir: " + testDir);
    Assert.assertNotNull(testDir,
        "The environment variable TEST_DIR must be set for tests to execute properly.");
    Path testPath = Paths.get(testDir);
    Path testDevicePath = Paths.get(testDir + "binaries/pri-fidoiot/device/");

    String[] deviceDiCmd = {"java", D_JAVA_LIBRARY_PATH + testDir,
        "-Dfidoalliance.fdo.pem.dev=binaries/pri-fidoiot/device/device.pem",
        "-jar", "binaries/pri-fidoiot/device/device.jar"};

    TestProcess deviceDi = new TestProcess(testPath, deviceDiCmd);
    int deviceResultDi = -1;
    try (TestProcess.Handle hDeviceDi = deviceDi.start()) {
      if (hDeviceDi.waitFor(longTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
        deviceResultDi = hDeviceDi.exitValue();
      }
    }

    if (deviceResultDi != 0) {
      TestLogger.error(
          "Device DI did not complete successfully.Most likely cause is a timeout.Exit value: "
              + deviceResultDi);
    } else {
      TestLogger.info("Device DI completed successfully.Exit value: " + deviceResultDi);
    }
    // Confirm device completed successfully.
    Assert.assertEquals(deviceResultDi, 0,
        "ERROR: Device DI did not exit properly. Exit value: " + deviceResultDi + "; ");

    Thread.sleep(shortTimeout.toMillis());

    String[] shellCmdVoucher = {"bash", "-cx",
        "curl -D - --digest -u apiUser:" + mfgApiPass + " "
            + "-XGET http://localhost:8039/api/v1/vouchers/0 "
            + "-o ext_voucher"};
    TestProcess shellVoucher = new TestProcess(testPath, shellCmdVoucher);
    try (TestProcess.Handle hShellCmd = shellVoucher.start()) {
      hShellCmd.waitFor(1000, TimeUnit.MILLISECONDS);
    }

    String[] shellCmdTo = {"bash", "-cx",
        "curl -D - --digest -u apiUser:" + ownerApiPass + " "
            + "--header \"Content-Type: application/cbor\" "
            + "--data-binary @ext_voucher http://localhost:8042/api/v1/owner/vouchers/ -o guid"};
    TestProcess shellTo = new TestProcess(testPath, shellCmdTo);
    try (TestProcess.Handle hShellCmdTo = shellTo.start()) {
      hShellCmdTo.waitFor(1000, TimeUnit.MILLISECONDS);
    }

    if (sviEnabled.toLowerCase().equals("true")) {
      BufferedReader br = new BufferedReader(new FileReader(Paths.get(testDir + "/guid").toFile()));
      String guid = br.readLine();

      String[] shellCmdServiceInfoActivate = {"bash", "-cx",
          "curl --location --digest -u apiUser:" + ownerApiPass + " "
          + "--request PUT 'http://localhost:8042/api/v1/device/svi?module=fdo_sys&"
          + "var=active&priority=0&bytes=F5' --header 'Content-Type: application/octet-stream'"};
      TestProcess shellServiceInfo = new TestProcess(testPath, shellCmdServiceInfoActivate);
      try (TestProcess.Handle hShellCmd = shellServiceInfo.start()) {
        hShellCmd.waitFor(2000, TimeUnit.MILLISECONDS);
      }

      String[] shellCmdServiceInfoFileTransfer = {"bash", "-cx",
          "curl --location --digest -u apiUser:" + ownerApiPass + " "
              + "--request PUT 'http://localhost:8042/api/v1/device/svi?module=fdo_sys&"
              + "var=filedesc&priority=1&filename=linux64.sh&guid=" + guid
              + "' --header 'Content-Type: application/octet-stream' --data-binary "
              + "'@common/src/main/resources/linux64.sh'"};

      TestProcess shellServiceInfoFileTransfer = new TestProcess(testPath,
          shellCmdServiceInfoFileTransfer);
      try (TestProcess.Handle hShellCmd = shellServiceInfoFileTransfer.start()) {
        hShellCmd.waitFor(2000, TimeUnit.MILLISECONDS);
      }

      String[] shellCmdServiceInfoExec = {"bash", "-cx",
          "curl --location --digest -u apiUser:" + ownerApiPass + " "
              + "--request PUT 'http://localhost:8042/api/v1/device/svi?module=fdo_sys&"
              + "var=exec&guid=" + guid + "&priority=2&"
              + "bytes=82672F62696E2F73686A6C696E757836342E7368' "
              + "--header 'Content-Type: application/octet-stream'"};
      TestProcess shellServiceInfoExec = new TestProcess(testPath, shellCmdServiceInfoExec);
      try (TestProcess.Handle hShellCmd = shellServiceInfoExec.start()) {
        hShellCmd.waitFor(2000, TimeUnit.MILLISECONDS);
      }

    }

    Thread.sleep(fdoToWait.toMillis());

    String[] deviceToCmd = {"java", D_JAVA_LIBRARY_PATH + testDir,
        "-Dfidoalliance.fdo.pem.dev=binaries/pri-fidoiot/device/device.pem",
        "-jar", "binaries/pri-fidoiot/device/device.jar"};

    TestProcess deviceTo = new TestProcess(testPath, deviceToCmd);
    int deviceResultTo = -1;
    try (TestProcess.Handle hDeviceTo = deviceTo.start()) {
      if (hDeviceTo.waitFor(longTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
        deviceResultTo = hDeviceTo.exitValue();
      }
    }

    if (deviceResultTo != 0) {
      TestLogger.error(
          "Device TO did not complete successfully.Most likely cause is a timeout.Exit value: "
              + deviceResultTo);
    } else {
      TestLogger.info("Device TO completed successfully.Exit value: " + deviceResultTo);
    }

    Assert.assertEquals(deviceResultTo, 0,
        "ERROR: Device TO did not exit properly. Exit value: " + deviceResultTo + "; ");

    if (sviEnabled.toLowerCase().equals("true")) {
      String logFileDevice = Paths.get(testDir, resultFile).toString();
      Assert.assertTrue(
          TestUtil.fileContainsString(logFileDevice, "Device onboarded successfully.", true),
          "ERROR: Device: ServiceInfo not processed successfully.");
    }
  }

  private void aioTest(String sviEnabled) throws Exception {

    TestLogger.info("=====> testDir: " + testDir);
    Assert.assertNotNull(testDir,
            "The environment variable TEST_DIR must be set for tests to execute properly.");
    Path testPath = Paths.get(testDir);
    Path testDevicePath = Paths.get(testDir + "binaries/pri-fidoiot/device/");

    String[] deviceDiCmd = {"bash", "-cx", "java  -Djava.library.path=/home/sdo/linking/test-fidoiot/ -jar device.jar"};

    TestProcess deviceDi = new TestProcess(testPath, deviceDiCmd);
    int deviceResultDi = -1;
    try (TestProcess.Handle hDeviceDi = deviceDi.start()) {
      if (hDeviceDi.waitFor(longTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
        deviceResultDi = hDeviceDi.exitValue();
      }
    }

    if (deviceResultDi != 0) {
      TestLogger.error(
              "Device DI did not complete successfully.Most likely cause is a timeout.Exit value: "
                      + deviceResultDi);
    } else {
      TestLogger.info("Device DI completed successfully.Exit value: " + deviceResultDi);
    }
    // Confirm device completed successfully.
    Assert.assertEquals(deviceResultDi, 0,
            "ERROR: Device DI did not exit properly. Exit value: " + deviceResultDi + "; ");

    Thread.sleep(shortTimeout.toMillis());


    if (sviEnabled.toLowerCase().equals("true")) {
      BufferedReader br = new BufferedReader(new FileReader(Paths.get(testDir + "/guid").toFile()));
      String guid = br.readLine();



      String[] shellCmdServiceInfoFileTransfer1 = {"bash", "-cx", "curl -D - --digest -u apiUser: " +
              "--location --request POST 'http://localhost:8080/api/v1/owner/resource?filename=payload.bin' " +
              "--header 'Content-Type: text/plain' --data-binary '@common/src/main/resources/payload.bin'"};

      TestProcess shellServiceInfoFileTransfer1 = new TestProcess(testPath,
              shellCmdServiceInfoFileTransfer1);
      try (TestProcess.Handle hShellCmd = shellServiceInfoFileTransfer1.start()) {
        hShellCmd.waitFor(2000, TimeUnit.MILLISECONDS);
      }

      String[] shellCmdServiceInfoFileTransfer2 = {"bash", "-cx", "curl -D - --digest -u apiUser: " +
              "--location --request POST 'http://localhost:8080/api/v1/owner/resource?filename=linux64.sh' " +
              "--header 'Content-Type: text/plain' --data-binary '@common/src/main/resources/linux64.sh'"};

      TestProcess shellServiceInfoFileTransfer2 = new TestProcess(testPath,
              shellCmdServiceInfoFileTransfer2);
      try (TestProcess.Handle hShellCmd = shellServiceInfoFileTransfer2.start()) {
        hShellCmd.waitFor(2000, TimeUnit.MILLISECONDS);
      }

      String[] shellCmdServiceInfoExec = {"bash", "-cx","curl -D - --digest -u apiUser: --location --request POST 'http://localhost:8080/api/v1/owner/svi' --header 'Content-Type: text/plain' --data-raw '[{\"filedesc\" : \"payload.bin\",\"resource\" : \"payload.bin\"},{\"filedesc\" : \"linux64.sh\",\"resource\" : \"linux64.sh\"},{\"exec\" : [\"/bin/bash\",\"linux64.sh\"]}]'"};
    
      TestProcess shellServiceInfoExec = new TestProcess(testPath, shellCmdServiceInfoExec);
      try (TestProcess.Handle hShellCmd = shellServiceInfoExec.start()) {
        hShellCmd.waitFor(2000, TimeUnit.MILLISECONDS);
      }

    }

    String[] deviceToCmd = {"bash", "-cx", "java  -Djava.library.path=/home/sdo/linking/test-fidoiot/ -jar device.jar"};

    TestProcess deviceTo = new TestProcess(testPath, deviceToCmd);
    int deviceResultTo = -1;
    try (TestProcess.Handle hDeviceTo = deviceTo.start()) {
      if (hDeviceTo.waitFor(longTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
        deviceResultTo = hDeviceTo.exitValue();
      }
    }

    if (deviceResultTo != 0) {
      TestLogger.error(
              "Device TO did not complete successfully.Most likely cause is a timeout.Exit value: "
                      + deviceResultTo);
    } else {
      TestLogger.info("Device TO completed successfully.Exit value: " + deviceResultTo);
    }

    Assert.assertEquals(deviceResultTo, 0,
            "ERROR: Device TO did not exit properly. Exit value: " + deviceResultTo + "; ");

    if (sviEnabled.toLowerCase().equals("true")) {
      String logFileDevice = Paths.get(testDir, resultFile).toString();
      Assert.assertTrue(
              TestUtil.fileContainsString(logFileDevice, "Device onboarded successfully.", true),
              "ERROR: Device: ServiceInfo not processed successfully.");
    }
  }

  @Test(groups = {"fdo_pri_smoketest"}, dataProvider = "FdoTestData")
  public void priSmokeTest(String testName,
      String enabled,
      String deviceType,
      String sviEnabled)
      throws Exception {

    TestLogger.info("OS: " + osName + "; OS Version: " + osVersion + "; enabled: " + enabled);

    if (enabled.toLowerCase().equals("false")) {
      throw new SkipException("Skipping disabled test.");
    }

    TestLogger.info("Test Name:" + testName);

    if (testName.equals("component-sample-test")) {
      componentSampleTest(sviEnabled);
    } else if (testName.equals("aio-test")) {
      aioTest(sviEnabled);
    } else {
      throw new SkipException("Skipping tests for unknown device type " + deviceType);
    }
  }
}
