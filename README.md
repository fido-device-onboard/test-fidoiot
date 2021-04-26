# About
The fdo-test contains automated smoke tests for FDO components. The smoke tests are written
on top of TESTNG framework.

# Getting Started with the fdo-test

The following are the system constraints for the fdo-test.
- Operating System: Ubuntu* 20.04
- Java* Development Kit 11
- Apache Maven* 3.5.4
- Java IDE (Optional) for convenience in modifying the source code
- Docker 18.09
- Docker compose 1.21.2

# Configuring JAVA Execution Environment

Appropriate proxy configuration should be updated in **`_JAVA_OPTIONS`** environment variable. (Mandatory, if you are working behind a proxy.)

Update the proxy information in _JAVA_OPTIONS as ```_JAVA_OPTIONS=-Dhttp.proxyHost=http_proxy_host -Dhttp.proxyPort=http_proxy_port -Dhttps.proxyHost=https_proxy_host -Dhttps.proxyPort=https_proxy_port```.

# Directory Structure

The fdo-test repository is structured into 4 folders.

* `clientSdkTests` : Includes the sanity end-to-end test for ClientSdk-fidoiot component.

* `priTests` : Includes the sanity end-to-end test for Pri-fidoiot component.

* `common`: Includes the common test-framework shared across the smoke tests.  It also contains common utility and automation classes.

# Configuring Smoke Test execution environment

Make sure to export the following environment variables, before starting the smoke test.

* TEST_DIR: the directory containing the executables under test.  Test logs and temporary files are also stored under this folder.

    `export TEST_DIR=<directory-of-fdo-test-repo>`

and copy the component binary to the respective folder in binaries directory.


# Running the smoke test

Smoke tests are executed as a task in the maven test stage.

The generic command to invoke smoke test :  `mvn clean test -Dgroups=<test-group-name>`

Individual test folder contains the instructions to run the specific smoke test.
