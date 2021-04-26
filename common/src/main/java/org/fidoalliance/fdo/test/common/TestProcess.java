// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test.common;

import com.sun.jna.Platform;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Extended functionalities for TestCase Class.
 */
public class TestProcess extends TestCase {

  private static final String OS = System.getProperty("os.name").toLowerCase();

  private static final String JAVA_OPTS = "JAVA_OPTS";
  private static final String JAVACMD = "JAVACMD";
  private static final String D_LOGBACK_CONFIG = "-Dlogback.configurationFile=";

  // How long to wait for servers to shut down
  private Duration shortTimeout = Duration.of(5, ChronoUnit.SECONDS);

  private ProcessBuilder builder;

  /**
   * Parameterized constructor for TestProcess.
   *
   * @param workPath : path for test Directory
   * @param cmd      : command to execute
   */
  public TestProcess(Path workPath, String[] cmd) {

    builder = new ProcessBuilder()
        .directory(workPath.toFile())
        .command(cmd)
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT);

    TestLogger.info("directory: " + workPath.toFile());
    TestLogger.info("command: " + Arrays.toString(cmd));
  }

  /**
   * Parameterized constructor for TestProcess.
   *
   * @param workPath : path for test Directory
   * @param cmd      : command to execute
   */
  public TestProcess(Path workPath, List<String> cmd) {

    builder = new ProcessBuilder()
        .directory(workPath.toFile())
        .command(cmd)
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT);

    TestLogger.info("directory: " + workPath.toFile());
    TestLogger.info("command: ");

    for (String s : cmd) {
      TestLogger.info(s);
    }
  }

  /**
   * Parameterized constructor for TestProcess.
   *
   * @param workPath : path for test Directory
   * @param cmd      : command to execute
   */
  public TestProcess(Path workPath, ArrayList<String> cmd) {

    builder = new ProcessBuilder()
        .directory(workPath.toFile())
        .command(cmd)
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT);

    TestLogger.info("directory: " + workPath.toFile());
    TestLogger.info("command: ");

    for (String s : cmd) {
      TestLogger.info(s);
    }
  }

  /**
   * This method used for Iot Platform SDK Docker services make UP/DOWN.
   */
  public static void execute_dockerCmd(String directory, String command) {
    List<String> commands = new ArrayList<>();
    commands.add("bash");
    commands.add("-c");
    commands.add(command);

    ProcessBuilder builder = new ProcessBuilder().inheritIO();
    builder.command(commands);
    File path1 = new File(directory);
    TestLogger.info("=====> File(directory): " + path1.toString());
    builder.directory(new File(directory));
    String path = System.getenv("PATH");
    builder.environment().put("PATH", "/usr/bin:" + path);
    builder.redirectErrorStream(true);
    builder.redirectError(Redirect.INHERIT);

    try {
      builder.start();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Starts a new process using the attributes of the process builder.
   *
   * @return a new Process object for managing the subprocess
   * @throws IOException if an I/O exception occurs
   */
  public Handle start() throws IOException {
    return new Handle(builder.start());
  }

  /**
   * Kill the process forcefully.
   *
   * @param process : Component process.
   */
  private void kill(Process process) {
    if (Platform.isWindows()) {
      try {
        Kernel32.destroyForcibly(process);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      process.destroyForcibly();
    }
  }

  /**
   * An object that may hold resources until it is closed.
   */
  public class Handle implements AutoCloseable {

    Process process;

    /**
     * Constructor for Handle.
     *
     * @param p argument of type Process
     */
    private Handle(Process p) {
      process = p;
    }

    /**
     * Causes the current process to wait.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return true if the subprocess has exited and false if the waiting time elapsed before exit
     * @throws InterruptedException when another thread interrupts (not caught)
     */
    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
      return process.waitFor(timeout, unit);
    }

    /**
     * Exit value for the subprocess.
     *
     * @return the exit value of the subprocess represented by this Process object
     */
    public int exitValue() {
      return process.exitValue();
    }

    /**
     * Override the close method of an AutoCloseable object. The close() method of an AutoCloseable
     * object is called automatically when exiting a try-with-resources block
     */
    @Override
    public void close() {
      try {
        kill(process);
        while (!process.waitFor(shortTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
          kill(process);
        }
      } catch (InterruptedException e) {
        kill(process);
      }
    }
  }
}
