package com.oratau.price.console;

import com.Ostermiller.util.CmdLn;
import com.Ostermiller.util.CmdLnOption;
import com.Ostermiller.util.CmdLnResult;
import com.oratau.price.Commons;
import com.oratau.price.SourcePrice;
import com.oratau.price.SupplierPrice;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: Tau
 * Date: 05.06.14
 */
public class Console
{

  static String sourcePriceFileName = null;
  static String[] suppliersFiles = null;
  static HierarchicalINIConfiguration conf = null;

  /**
   * Associate each option with a Java enum.
   * Good when:
   * Need for a static context.
   * Compiler can enforce handling of all command line options.
   * Properly preserves argument ordering when a later option may cancel out an earlier option.
   * Drawbacks:
   * Glue code to associate each command line option with an enum.
   * Can create a long switch statement to deal with arguments
   * Does not work when arguments not known at compile time
   */
  private enum EnumOptions
  {
    SOURCE(new CmdLnOption("source_file").setRequiredArgument().setDescription("source price-file to updating")),
    SUPPLIERS(
        new CmdLnOption("suppliers").setRequiredArgument().setDescription(
            "suppliers price-files in format <supplier config-file name>,<path to price-file>,..."
        )
    );
    private CmdLnOption option;

    private EnumOptions(CmdLnOption option)
    {
      option.setUserObject(this);
      this.option = option;
    }

    private CmdLnOption getCmdLineOption()
    {
      return option;
    }
  }

  public static void main(String[] args) throws Exception
  {
    parseArgs(args);

    if (suppliersFiles == null || suppliersFiles.length % 2 != 0) {
      System.out.println("Parameter <suppliers> is not set correct!");
      System.exit(1);
    }

    String outputSourcePriceFileName = null;
    if (sourcePriceFileName != null && new File(sourcePriceFileName).exists()) {
      outputSourcePriceFileName =
          "results\\"
              .concat((new SimpleDateFormat(Commons.DATE_FORMAT)).format(new Date()))
              .concat("\\")
              .concat(FilenameUtils.removeExtension(FilenameUtils.getName(sourcePriceFileName)))
              .concat("_")
              .concat((new SimpleDateFormat(Commons.DATETIME_FORMAT)).format(new Date()))
              .concat(".")
              .concat(FilenameUtils.getExtension(sourcePriceFileName));
      FileUtils.copyFile(new File(sourcePriceFileName), new File(outputSourcePriceFileName));
    } else {
      System.out.println("Не задан или неверно задан исходный прайс для обновления!");
      System.exit(1);
    }

    conf = new HierarchicalINIConfiguration(); // Настройки прайсов поставщиков и исходного
    String configFileName = new File(".").getAbsolutePath().concat("\\config\\source_price.ini");
    System.out.println(configFileName);
    loadConfiguration(configFileName);

    SourcePrice sourcePrice = new SourcePrice(
        outputSourcePriceFileName,
        conf.getInt("start_row"),
        conf.getInt("artikul_column"),
        conf.getInt("amount_column"),
        conf.getInt("price_column"),
        conf.getInt("name_column"),
        conf.getInt("producer_column"),
        conf.getInt("complex_field_column")
    );


    for (int i = 0; i < suppliersFiles.length / 2; i++) {
      String supplierType = suppliersFiles[i * 2];
      String supplierPriceFileName = suppliersFiles[i * 2 + 1];
      configFileName = new File(".").getAbsolutePath().concat("\\config\\suppliers\\").concat(supplierType).concat(".ini");
      System.out.println(configFileName);
      loadConfiguration(configFileName); // Load settings for each supplier

      SupplierPrice supplierPrice = new SupplierPrice(supplierPriceFileName, supplierType);
      supplierPrice.parsePriceToMap(
          conf.getInt("start_row"), conf.getInt("artikul_column"), conf.getInt("amount_column"), conf.getInt("price_column")
      );
      sourcePrice.refreshPrice(supplierPrice.getPriceMapByArtikul(), supplierPrice.getSupplierName());
    }

    sourcePrice.logNotFoundedPosition();

    sourcePrice.save();
  }

  private static void loadConfiguration(String configurationFileName)
  {
    if (configurationFileName != null && new File(configurationFileName).exists()) {
      try {
        if (conf == null)
          conf = new HierarchicalINIConfiguration();
        else
          conf.clear();

        conf.load(configurationFileName);

      } catch (ConfigurationException e) {
        System.out.println("Ошибка загрузки файла конфигурации".concat(configurationFileName).concat(": ").concat(e.getMessage()));
        System.exit(1);
      }

    } else {
      System.out.println("Неверно задан путь к файлу конфигурации или файл не существует ".concat(configurationFileName).concat("!"));
      System.exit(1);
    }
  }

  private static void parseArgs(String[] args)
  {
    CmdLn cmdLn = new CmdLn(args);
    for (EnumOptions option : EnumOptions.values()) {
      cmdLn.addOption(option.getCmdLineOption());
    }

    for (CmdLnResult result : cmdLn.getResults()) {
      switch ((EnumOptions) result.getOption().getUserObject()) {
        case SOURCE:
          sourcePriceFileName = result.getArgument();
          break;

        case SUPPLIERS:
          suppliersFiles = StringUtils.split(result.getArgument(), ",");
          break;
      }
    }
  }

}