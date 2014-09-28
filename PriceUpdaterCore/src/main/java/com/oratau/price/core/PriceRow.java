package com.oratau.price.core;

/**
 * User: Tau
 * Date: 04.06.14
 */
public class PriceRow
{
  public int rowNum;
  public String artikul;
  public String name;
  public String producer;
  public double price;
  //    public double priceSale;
  public double amount;
  public boolean complexField;
  public int numInComplexField;

  private boolean positionIsFounded;

  public boolean getPositionIsFounded()
  {
    return positionIsFounded;
  }

  public void setPositionIsFounded(boolean positionIsFounded)
  {
    this.positionIsFounded = positionIsFounded;
  }

  public PriceRow(int rowNum, String artikul, String name, String producer, double price, /*double priceSale,*/ double amount, boolean complexField, int numInComplexField)
  {
    this.rowNum = rowNum;
    this.artikul = artikul;
    this.name = name;
    this.producer = producer;
    this.price = price;
//        this.priceSale = priceSale;
    this.amount = amount;
    this.complexField = complexField;
    this.numInComplexField = numInComplexField;

    this.positionIsFounded = false;
  }
}
