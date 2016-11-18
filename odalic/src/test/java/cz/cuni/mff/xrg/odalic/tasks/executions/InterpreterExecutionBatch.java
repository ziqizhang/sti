package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.CSVExportTest;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.RDFExportTest;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import uk.ac.shef.dcs.sti.STIException;

public class InterpreterExecutionBatch {

  private static final Logger log = LoggerFactory.getLogger(InterpreterExecutionBatch.class);

  /**
   * Expects sti.properties file path as the first and test input CSV file path as the second
   * command line argument
   * 
   * @param args command line arguments
   * 
   * @author Josef Janoušek
   * @throws IOException when the initialization process fails to load its configuration
   * @throws STIException when the interpreters fail to initialize
   * 
   */
  public static void main(String[] args) throws STIException, IOException {

    final String propertyFilePath = args[0];
    final String testInputFilePath = args[1];

    // Core execution
    Result odalicResult = CoreExecutionBatch.testCoreExecution(propertyFilePath, testInputFilePath);

    if (odalicResult == null) {
      log.warn("Result of core algorithm is null, so exports cannot be launched.");
      return;
    }

    // settings for export
    File inputFile = CoreExecutionBatch.getInputFile();
    Input input = CoreExecutionBatch.getInput();
    Configuration config = CoreExecutionBatch.getConfiguration();
    String baseExportPath = inputFile.getParent() + File.separator
        + FilenameUtils.getBaseName(inputFile.getName()) + "-export";

    // JSON export
    AnnotatedTable annotatedTable = CSVExportTest.testExportToAnnotatedTable(odalicResult, input,
        config, baseExportPath + ".json");

    // CSV export
    Input extendedInput =
        CSVExportTest.testExportToCSVFile(odalicResult, input, config, baseExportPath + ".csv");

    // RDF export
    if (annotatedTable == null || extendedInput == null) {
      log.warn("Annotated table or extended input is null, so RDF export cannot be launched.");
      return;
    }
    RDFExportTest.testExportToRDFFile(annotatedTable, extendedInput, baseExportPath + ".rdf");
  }
}
