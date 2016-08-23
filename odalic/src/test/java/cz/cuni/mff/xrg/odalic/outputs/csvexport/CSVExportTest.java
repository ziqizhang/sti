package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.*;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.DefaultResultToAnnotatedTableAdapter;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * JUnit test for CSV export
 * 
 * @author Josef Janoušek
 *
 */
public class CSVExportTest {
  
  private static final Logger log = LoggerFactory.getLogger(CSVExportTest.class);
  
  @BeforeClass
  public static void beforeClass() throws URISyntaxException, IOException {
    
  }
  
  @Test
  public void TestConversionToCSV() {
    
    // Now this test cannot be launched separately,
    // because there are problems with deserializing Result (ResultValue) from external JSON file
  }
  
  public static Input testExportToCSVFile(Result result, Input input, Configuration config, String filePath) {
    
    // Conversion from result to CSV extended input
    Input extendedInput = new DefaultResultToCSVExportAdapter().toCSVExport(result, input, config);
    
    // Export CSV extended Input to CSV String
    String csv;
    try {
      csv = new DefaultCSVExporter().export(extendedInput, new CsvConfiguration());
    } catch (IOException e) {
      log.error("Error - exporting extended Input to CSV:");
      e.printStackTrace();
      return null;
    }
    log.info("Resulting CSV is: " + csv);
    
    // Write CSV String to file
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(csv);
      log.info("CSV export saved to file " + filePath);
      return extendedInput;
    } catch (IOException e) {
      log.error("Error - saving CSV export file:");
      e.printStackTrace();
      return null;
    }
  }
  
  public static AnnotatedTable testExportToAnnotatedTable(Result result, Input input, Configuration config, String filePath) {
    
    // Conversion from result to annotated table
    AnnotatedTable annotatedTable = new DefaultResultToAnnotatedTableAdapter().toAnnotatedTable(result, input, config);
    
    // Export Annotated Table to JSON String
    String json = new GsonBuilder().setPrettyPrinting().create().toJson(annotatedTable);
    log.info("Resulting JSON is: " + json);
    
    // Write JSON String to file
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(json);
      log.info("JSON export saved to file " + filePath);
      return annotatedTable;
    } catch (IOException e) {
      log.error("Error - saving JSON export file:");
      e.printStackTrace();
      return null;
    }
  }
}
