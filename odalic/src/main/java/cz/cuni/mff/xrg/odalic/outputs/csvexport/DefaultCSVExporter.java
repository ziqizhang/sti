package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * Default implementation of the {@link CSVExporter}.
 * 
 * @author Josef Janoušek
 */
public class DefaultCSVExporter implements CSVExporter {

  /**
   * The default export implementation.
   * @throws IOException 
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.csvexport.CSVExporter#export(cz.cuni.mff.xrg.odalic.input.Input, cz.cuni.mff.xrg.odalic.input.CsvConfiguration)
   */
  @Override
  public String export(Input content, CsvConfiguration configuration) throws IOException {
    
    CSVFormat format = configuration.toApacheConfiguration();
    StringWriter stringWriter = new StringWriter();
    CSVPrinter csvPrinter = new CSVPrinter(stringWriter, format.withRecordSeparator(System.lineSeparator()));
    
    csvPrinter.printRecord(content.headers());
    csvPrinter.printRecords(content.rows());
    
    csvPrinter.flush();
    csvPrinter.close();
    
    return stringWriter.toString().trim();
  }

}
