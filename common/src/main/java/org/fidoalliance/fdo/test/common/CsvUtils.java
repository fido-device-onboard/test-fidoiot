package org.fidoalliance.fdo.test.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class CsvUtils {

  /**
   * Returns the CSV data in String[][] format.
   */
  public static String[][] getDataArray(String filePath) throws Exception {

    String line = "";
    BufferedReader br = new BufferedReader(new FileReader(filePath));

    int rows = countLines(filePath);
    int columns = br.readLine().split(",").length;

    String[][] dataArray = new String[rows - 1][columns];
    Arrays.stream(dataArray).forEach(dataRow -> Arrays.fill(dataRow, ""));

    TestLogger.info("numRows: " + rows + "; numCols: " + columns);
    int row = 0;

    while ((line = br.readLine()) != null) {

      String[] data = line.split(",");
      int col;
      for (col = 0; col < data.length; col++) {
        String element = data[col];
        dataArray[row][col] = element;
        TestLogger.info(element);
      }
      row++;
      TestLogger.info("");
    }
    return dataArray;
  }

  public static int countLines(String filepath) throws IOException {
    return (int) Files.lines(Paths.get(filepath), Charset.defaultCharset()).count();
  }

}
