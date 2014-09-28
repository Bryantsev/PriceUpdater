package com.oratau.price.core;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Tau
 * Date: 05.06.14
 */
public class SourcePrice extends Price
{
  public int startRow;
  public int priceColumn;
  public int amountColumn;
  public int artikulColumn;
  public int nameColumn;
  public int producerColumn;
  public int complexFieldColumn;

  Pattern p_artikul = Pattern.compile("\\[([^:]+):.*$");
  Pattern p_price = Pattern.compile(".*:.*:(\\d+\\.?\\d*):\\d+\\.?\\d*\\]$");
  Pattern p_amount = Pattern.compile(".*:(\\d+\\.?\\d*)\\]$");
  Pattern patternForReplaceComplexField = Pattern.compile("(.*:)(\\d+\\.?\\d*)(:)(\\d+\\.?\\d*)(\\])$");


  public SourcePrice(String priceFileName, int startRow, int artikulColumn, int amountColumn, int priceColumn, int nameColumn, int producerColumn, int complexFieldColumn) throws IOException
  {
    super(priceFileName);

    this.startRow = --startRow;

    this.priceColumn = --priceColumn;
    this.amountColumn = --amountColumn;
    this.artikulColumn = --artikulColumn;
    this.nameColumn = --nameColumn;
    this.producerColumn = --producerColumn;
    this.complexFieldColumn = --complexFieldColumn;

    this.priceRows = parsePriceToArray();
  }

  public ArrayList<PriceRow> parsePriceToArray() throws IOException
  {
    ArrayList<PriceRow> priceRowArrayList = null;
    String artikul;
    String name;
    String producer;
    Double price;
    Double amount;

    for (int k = 0; k < wbPrice.getNumberOfSheets() && k < 1; k++) {
      Sheet sheet = wbPrice.getSheetAt(k);
      int rows = sheet.getPhysicalNumberOfRows();
      //System.out.println("Sheet " + k + " \"" + wbPrice.getSheetName(k) + "\" has " + rows + " row(s).");

      priceRowArrayList = new ArrayList<>(rows - 1);

      int logical_row = 0;
      for (int r = startRow - 1; r < rows; r++) {
        Row row;
        int count = 0;
        do {
          logical_row++;
          count++;
          row = sheet.getRow(logical_row);
        } while (row == null && count < 100);

        if (row == null) continue;

        Cell cell = row.getCell(artikulColumn, Row.RETURN_BLANK_AS_NULL); // artikul
        if (cell == null) continue; // if artikul is not found then next row is got
        artikul = Commons.getCellValueAsString(cell);
        if (artikul == null) continue;
        else artikul = artikul.trim();

        cell = row.getCell(nameColumn, Row.RETURN_BLANK_AS_NULL); // name
        name = (cell != null ? Commons.getCellValueAsString(cell) : "Name unknown");

        cell = row.getCell(producerColumn, Row.RETURN_BLANK_AS_NULL); // producer
        producer = (cell != null ? Commons.getCellValueAsString(cell) : "Producer unknown");

        // if price is not set then prices and amounts in column complexFieldColumn, else they are in different columns
        if (row.getCell(priceColumn, Row.RETURN_BLANK_AS_NULL) == null
            && row.getCell(complexFieldColumn, Row.RETURN_BLANK_AS_NULL) != null) {
          String[] complexFieldParts = getComplexFieldStrings(row);

          for (int i = 0; i < complexFieldParts.length; i++) {
            Matcher m_artikul = p_artikul.matcher(complexFieldParts[i]);
            if (m_artikul.matches() && m_artikul.group(1) != null) artikul = m_artikul.group(1).trim();
            else artikul = null;

            Matcher m_price = p_price.matcher(complexFieldParts[i]);
            if (m_price.matches() && m_price.group(1) != null) price = new Double(m_price.group(1));
            else price = 0.0;

            Matcher m_amount = p_amount.matcher(complexFieldParts[i]);
            if (m_amount.matches() && m_amount.group(1) != null) amount = new Double(m_amount.group(1));
            else amount = 0.0;

            if (artikul != null)
              priceRowArrayList.add(new PriceRow(logical_row, artikul, name, producer, price, amount, true, i));
          }

        } else {
          amount = Commons.getCellValueAsDouble(row.getCell(amountColumn, Row.RETURN_BLANK_AS_NULL), 0.0);
          price = Commons.getCellValueAsDouble(row.getCell(priceColumn, Row.RETURN_BLANK_AS_NULL), 0.0);

          priceRowArrayList.add(new PriceRow(logical_row, artikul, name, producer, price, amount, false, 0));
        }

      }
    }

    return priceRowArrayList;
  }

  private String[] getComplexFieldStrings(Row row)
  {
    String complexField = null;

    if (row != null)
      complexField = Commons.getCellValueAsString(row.getCell(6, Row.RETURN_BLANK_AS_NULL));

    return StringUtils.split(complexField, ";");
  }

  public void refreshPrice(HashMap<String, PriceRow> newPrice, String supplierName) throws IOException
  {
    if (newPrice != null) {
      ArrayList<PriceRow> updatedPriceRows = getUpdatedPriceRows(newPrice, supplierName);

      Sheet sheet = wbPrice.getSheetAt(0);
      Row row;
      Cell cell;

      for (PriceRow priceRow : updatedPriceRows) {
        row = sheet.getRow(priceRow.rowNum);
        if (row != null) {
          if (priceRow.complexField) {
            cell = row.getCell(complexFieldColumn, Row.RETURN_BLANK_AS_NULL);
            if (cell != null) {
              String[] complexFieldParts = getComplexFieldStrings(row);

              Matcher m_price = patternForReplaceComplexField.matcher(complexFieldParts[priceRow.numInComplexField]);
              if (m_price.matches())
                complexFieldParts[priceRow.numInComplexField] = m_price.group(1)
                    .concat(StringUtils.removeEnd(StringUtils.removeEnd(Double.toString(priceRow.price), "0"), "."))
                    .concat(m_price.group(3))
                    .concat(StringUtils.removeEnd(StringUtils.removeEnd(Double.toString(priceRow.amount), "0"), "."))
                    .concat(m_price.group(5));

              cell.setCellValue(StringUtils.join(complexFieldParts, ";"));
            }

          } else {
            cell = row.getCell(priceColumn); // price
            if (cell != null)
              cell.setCellValue(priceRow.price);

            cell = row.getCell(amountColumn); // amount
            if (cell != null)
              cell.setCellValue(priceRow.amount);
          }
        }
      }

    }
  }

  private ArrayList<PriceRow> getUpdatedPriceRows(HashMap<String, PriceRow> newPrice, String supplierName) throws IOException
  {
    StringBuilder logPriceChanges = new StringBuilder(
        "Формат: [<артикул>] <Наименование>: [<старая цена> + <изменение> = <новая цена>.] [Кол-во: <было> => <стало>.]"
    );
    final String templateCommon = "\r\n[<artikul>] <name>:";
    final String templatePrice = " <price_old> + <price_change> = <price_new>.";
    final String templateAmount = " Кол-во: <amount_old> => <amount_new>.";

    ArrayList<PriceRow> updatedPriceRows = new ArrayList<>();

    for (PriceRow priceRow : priceRows) {
      PriceRow newPriceRow = newPrice.get(priceRow.artikul);
      if (newPriceRow != null) {


        //priceRows[priceRow.]
        priceRow.setPositionIsFounded(true);


        if ((priceRow.amount == 0.0 && newPriceRow.amount > 0.0)
            || (priceRow.amount > 0.0 && newPriceRow.amount == 0.0)
            || priceRow.price != newPriceRow.price) {
          logPriceChanges.append(templateCommon.replace("<artikul>", priceRow.artikul).replace("<name>", priceRow.name));

          if (priceRow.price != newPriceRow.price) {
            logPriceChanges.append(
                templatePrice
                    .replace("<price_old>", Double.toString(priceRow.price))
                    .replace("<price_change>", Double.toString(newPriceRow.price - priceRow.price))
                    .replace("<price_new>", Double.toString(newPriceRow.price))
            );

            priceRow.price = newPriceRow.price;
          }

          if ((priceRow.amount == 0.0 && newPriceRow.amount > 0.0)
              || (priceRow.amount > 0.0 && newPriceRow.amount == 0.0)) {
            logPriceChanges.append(
                templateAmount
                    .replace("<amount_old>", Double.toString(priceRow.amount))
                    .replace("<amount_new>", Double.toString(newPriceRow.amount))
            );

            priceRow.amount = newPriceRow.amount;
          }

          updatedPriceRows.add(priceRow);
        }
      }
    }

    logPriceChanges.append("\r\n".concat(String.valueOf(updatedPriceRows.size())).concat(" of price rows were updated."));
    FileUtils.writeStringToFile(
        new File(
            "results\\"
                .concat(new SimpleDateFormat(Commons.DATE_FORMAT).format(new Date()))
                .concat("\\source_changes_")
                .concat((supplierName == null ? "unknown_supplier" : supplierName))
                .concat("_")
                .concat(new SimpleDateFormat(Commons.DATETIME_FORMAT).format(new Date()))
                .concat(".txt")
        ),
        logPriceChanges.toString()
    );

    return updatedPriceRows;
  }

  public void logNotFoundedPosition() throws IOException
  {
    StringBuilder logPriceChanges = new StringBuilder("В прайсах поставщиков не найдены следующие товары:\r\n");

    //Formatter

    for (PriceRow priceRow : priceRows) {
      if (!priceRow.getPositionIsFounded()) {
        logPriceChanges.append(priceRow.artikul).append(" (").append(priceRow.name).append("; ").append(priceRow.producer).append(").\r\n");
        //logPriceChanges.append(String.format("%1$ (%2$; %3$).\r\n", priceRow.artikul, priceRow.name, priceRow.producer));
      }
    }

    FileUtils.writeStringToFile(
        new File(
            "results\\"
                .concat(new SimpleDateFormat(Commons.DATE_FORMAT).format(new Date()))
                .concat("\\not_founded_positions")
                .concat("_")
                .concat(new SimpleDateFormat(Commons.DATETIME_FORMAT).format(new Date()))
                .concat(".txt")
        ),
        logPriceChanges.toString()
    );

  }


  public void save() throws IOException
  {
    FileOutputStream fileOut = new FileOutputStream(priceFileName);
    try {
      wbPrice.write(fileOut);
    } finally {
      fileOut.close();
    }
  }

}