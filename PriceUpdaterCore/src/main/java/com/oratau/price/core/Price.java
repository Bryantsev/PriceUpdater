package com.oratau.price.core;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * User: Tau
 * Date: 05.06.14
 */
public class Price
{
  protected ArrayList<PriceRow> priceRows = null;
  protected Workbook wbPrice = null;
  protected String priceFileName = null;

  public Price(String priceFileName) throws IOException
  {
    this.priceFileName = priceFileName;
    this.wbPrice = loadPriceDataFromFile(priceFileName);
  }

  public ArrayList<PriceRow> getPriceRows()
  {
    return priceRows;
  }

  public Workbook loadPriceDataFromFile(String priceFileName) throws IOException
  {
    String fileExt = FilenameUtils.getExtension(priceFileName);

    if ("xlsx".equals(fileExt))
      wbPrice = new XSSFWorkbook(new FileInputStream(priceFileName));
    else if ("xls".equals(fileExt))
      wbPrice = new HSSFWorkbook(new FileInputStream(priceFileName));
    else {
      System.out.println("Файлы с расширением ".concat(fileExt).concat(" (").concat(priceFileName).concat(") не поддерживаются!"));
      System.exit(1);
    }

    System.out.println("Файл: ".concat(priceFileName).concat(" загружен."));

    return wbPrice;
  }

  @Override
  public String toString()
  {
    String result = null;

    if (priceRows != null) {

      StringBuilder sb = new StringBuilder("Data from ArrayList:");

      for (Iterator<PriceRow> it = priceRows.iterator(); it.hasNext(); ) {
        PriceRow priceRow = it.next();

        if (priceRow.complexField)
          sb.append(
              "Complex Field from row "
                  .concat(Integer.toString(priceRow.rowNum).concat(" num = "))
                  .concat(Integer.toString(priceRow.numInComplexField))
                  .concat(":\r\n")
          );
        else
          sb.append("Simple row ".concat(Integer.toString(priceRow.rowNum).concat(":\r\n")));

        sb.append((priceRow.complexField ? "  |- " : "").concat("artikul: ".concat(priceRow.artikul)).concat("\r\n"));
        sb.append(
            (priceRow.complexField ? "  |- " : "")
                .concat("price: ")
                .concat(Double.toString(priceRow.price))
                .concat("\r\n")
        );
        sb.append(
            (priceRow.complexField ? "  |- " : "")
                .concat("amount: ")
                .concat(Double.toString(priceRow.amount))
                .concat("\r\n")
        );
      }

      result = sb.toString();
    }

    if (result == null)
      result = "No data";

    System.out.println(result);

    return result;
  }
}
