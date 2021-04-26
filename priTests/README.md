# About
This folder contains the automated smoke tests for PRI-fidoiot component.

# Getting Started with the PriTests

The following are the system constraints for the PriTests.
- Operating System: Ubuntu* 20.04
- Java* Development Kit 11
- Apache Maven* 3.5.4
- Java IDE (Optional) for convenience in modifying the source code
- Docker 18.09
- Docker compose 1.21.2

# Configuring JAVA Execution Environment

Appropriate proxy configuration should be updated in **`_JAVA_OPTIONS`** environment variable. (Mandatory, if you are working behind a proxy.)

Update the proxy information in _JAVA_OPTIONS as ```_JAVA_OPTIONS=-Dhttp.proxyHost=http_proxy_host -Dhttp.proxyPort=http_proxy_port -Dhttps.proxyHost=https_proxy_host -Dhttps.proxyPort=https_proxy_port```.

# Configuring Smoke Test execution environment

Make sure to export the following environment variables, before starting the smoke test.

* TEST_DIR: the directory containing the executables under test.  Test logs and temporary files are also stored under this folder.

    `export TEST_DIR=<directory-of-fdo-test-repo>`

# Adding binaries for the smoke test

- Create the following folders in the ./binaries directory.

  ```
  mkdir -p binaries/pri-fidoiot
  ```

- Copy the build binaries from pri-fidoiot/demo folder to binaries/pri-fidoiot folder.

  ```
  cp -r pri-fidoiot/component-samples/demo/*  binaries/pri-fidoiot/
  ```

# Running the smoke test

Smoke tests are executed as a task in the maven test stage.

The generic command to invoke smoke test :  `mvn clean test -Dgroups=fdo_pri_smoketest`

Individual test folder contains the instructions to run the specific smoke test.


# Disabling a specific test

You can disable a specific test by switching the enabled field from `true` to `false`
in `resources/FdoPriTestData.csv` file.
