package com.oratau.price.core;

import org.apache.poi.ss.usermodel.Cell;

/**
 * User: Tau
 * Date: 05.06.14
 */
public class Commons
{

  public static final String DATE_FORMAT = "yyyyMMdd";
  public static final String DATETIME_FORMAT = "yyyyMMdd_HHmmss";

  public static Object getCellValue(Cell cell)
  {
    Object value = null;
    if (cell != null) {
      switch (cell.getCellType()) {

        case Cell.CELL_TYPE_FORMULA:
          value = cell.getCellFormula();
          break;

        case Cell.CELL_TYPE_NUMERIC:
          value = cell.getNumericCellValue();
          break;

        case Cell.CELL_TYPE_STRING:
          value = cell.getStringCellValue();
          break;

        default:
      }
    }

    return value;
  }

  public static String getCellValueAsString(Cell cell)
  {
    String str = null;

    if (cell != null) {
      Object obj = getCellValue(cell);
      if (obj != null) {
        if (obj instanceof Double) str = String.valueOf(((Double) obj).longValue());
        else if (obj instanceof String) str = (String) obj;
        else str = "unknown type of data";
      }
    }

    return str;
  }

  public static Double getCellValueAsDouble(Cell cell, Double default_value)
  {
    Double double_value = default_value;
    if (cell != null) {
      Object obj = getCellValue(cell);
      if (obj != null)
        if (obj instanceof Double) double_value = (Double) obj;
    }

    return double_value;
  }
}
