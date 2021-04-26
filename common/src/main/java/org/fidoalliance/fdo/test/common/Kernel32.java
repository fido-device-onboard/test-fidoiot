// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.test.common;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Provide win32 kernel services.
 */
interface Kernel32 extends Library {

  //  Kernel32 INSTANCE = Native.loadLibrary("kernel32", Kernel32.class);
  Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
  // How long to wait for servers to shut down
  Duration shortTimeout = Duration.of(5, ChronoUnit.SECONDS);

  /**
   * Windows processes don't kill their children when they die. When destroying a windows
   * process,children must be killed first.
   *
   * @param process Component process
   * @throws IllegalArgumentException if an Illegal Argument exception occurs
   * @throws IllegalAccessException   if an Illegal Access exception occurs
   * @throws NoSuchFieldException     if no such field exception occurs
   * @throws SecurityException        if an security exception occurs
   * @throws IOException              if an I/O exception occurs
   * @throws InterruptedException     when another thread interrupts (not caught)
   */

  static void destroyForcibly(Process process)
      throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
      SecurityException, IOException, InterruptedException {
    if (Platform.isWindows()) {
      Field f = process.getClass().getDeclaredField("handle");
      f.setAccessible(true);
      int pid = Kernel32.INSTANCE.getProcessId((Long) f.get(process));
      Process killer = Runtime.getRuntime().exec("TASKKILL /F /T /PID " + pid);
      while (killer.isAlive()) {
        killer.waitFor(shortTimeout.toMillis(), TimeUnit.MILLISECONDS); // Fancy timeout
      }
    } else {
      throw new UnsupportedOperationException();
    }
  }

  int getProcessId(Long process);
}
