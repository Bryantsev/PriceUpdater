package com.oratau.price.core;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Tau
 * Date: 13.06.14
 */
public class SupplierPrice extends Price
{
  protected HashMap<String, PriceRow> priceMapByArtikul = null;
  protected String supplierName = null;

  private static final Pattern patternFirstNumber = Pattern.compile("\\D*(\\d+\\.?\\d*)\\D*");


  public String getSupplierName()
  {
    return supplierName;
  }

  public void setSupplierName(String supplierName)
  {
    this.supplierName = supplierName;
  }

  public SupplierPrice(String priceFileName, String supplierName) throws IOException
  {
    super(priceFileName);
    setSupplierName(supplierName);
  }

  public HashMap<String, PriceRow> getPriceMapByArtikul()
  {
    return priceMapByArtikul;
  }

  public ArrayList<PriceRow> getPriceRows()
  {
    return priceRows;
  }

  public HashMap<String, PriceRow> parsePriceToMap(int startRow, int artikulColumn, int amountColumn, int priceColumn) throws IOException
  {
    String artikul;
    Double price;
    Double amount;

    Sheet sheet = null;
    if (wbPrice.getNumberOfSheets() >= 0)
      sheet = wbPrice.getSheetAt(0);

    if (sheet != null) {
      startRow--;
      artikulColumn--;
      amountColumn--;
      priceColumn--;

      int rows = sheet.getPhysicalNumberOfRows();
      //System.out.println("Sheet " + k + " \"" + wbPrice.getSheetName(k) + "\" has " + rows + " row(s).");

      priceMapByArtikul = new HashMap<>(rows - startRow + 1);

      for (int r = startRow; r < rows; r++) {
        Row row = sheet.getRow(r);
        if (row == null) continue;

        amount = 0.0;
        price = 0.0;

        artikul = Commons.getCellValueAsString(row.getCell(artikulColumn, Row.RETURN_BLANK_AS_NULL));
        if (artikul == null) continue; // if artikul is not found then go to the next row
        artikul = artikul.trim();

        Cell cell = row.getCell(amountColumn, Row.RETURN_BLANK_AS_NULL);// amount
        Object obj = Commons.getCellValue(cell);
        if (obj != null) {
          if (obj instanceof Double) amount = (Double) obj;
          else if (obj instanceof String) {
            Matcher m_amount = patternFirstNumber.matcher((String) obj);
            if (m_amount.matches() && m_amount.group(1) != null) amount = new Double(m_amount.group(1));
          }
        }

        cell = row.getCell(priceColumn, Row.RETURN_BLANK_AS_NULL);// price
        obj = Commons.getCellValue(cell);
        if (obj != null) {
          if (obj instanceof Double) price = (Double) obj;
          else if (obj instanceof String) {
            Matcher m_price = patternFirstNumber.matcher((String) obj);
            if (m_price.matches() && m_price.group(1) != null)
              price = new Double(m_price.group(1));
          }
        }

        priceMapByArtikul.put(artikul, new PriceRow(r, artikul, "", "", price, amount, false, 0));
      }
    }

    return priceMapByArtikul;
  }

}
