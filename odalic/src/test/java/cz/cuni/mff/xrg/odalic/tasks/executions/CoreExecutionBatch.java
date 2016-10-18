package cz.cuni.mff.xrg.odalic.tasks.executions;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.xrg.odalic.feedbacks.DefaultFeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.DefaultInputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.DefaultAnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

public class CoreExecutionBatch {

  private static final Logger log = LoggerFactory.getLogger(CoreExecutionBatch.class);
  
  private static File inputFile;
  private static Input input;
  private static Configuration config;

  /**
   * Expects sti.properties file path as the first and test input CSV file path as the second
   * command line argument
   * 
   * @param args command line arguments
   * 
   * @author Josef Janoušek
   * @author Jan Váňa
   */
  public static void main(String[] args) {

    final String propertyFilePath = args[0];
    final String testInputFilePath = args[1];

    // Core execution
    testCoreExecution(propertyFilePath, testInputFilePath);
  }

  public static Result testCoreExecution(String propertyFilePath, String testInputFilePath) {

    inputFile = new File(testInputFilePath);

    // TableMinerPlus initialization
    final SemanticTableInterpreterFactory factory = new TableMinerPlusFactory(propertyFilePath);
    final Map<String, SemanticTableInterpreter> semanticTableInterpreters = factory.getInterpreters();
    Preconditions.checkNotNull(semanticTableInterpreters);

    // Code for extraction from CSV
    try (final FileInputStream inputFileStream = new FileInputStream(inputFile)) {
      input = new DefaultCsvInputParser(new ListsBackedInputBuilder())
              .parse(inputFileStream, inputFile.getName(), new CsvConfiguration());
      log.info("Input CSV file loaded.");
    } catch (IOException e) {
      log.error("Error - loading input CSV file:");
      e.printStackTrace();
      return null;
    }

    // Feedback settings
    Feedback feedback = new Feedback();

    // Configuration settings
    try {
      config = new Configuration(new cz.cuni.mff.xrg.odalic.files.File(inputFile.getName(), "x",
          inputFile.toURI().toURL(), true), new KnowledgeBase("DBpedia"), feedback);
    } catch (MalformedURLException e) {
      log.error("Error - Configuration settings:");
      e.printStackTrace();
      return null;
    }

    // input Table creation
    final Table table = new DefaultInputToTableAdapter().toTable(input);

    // TableMinerPlus algorithm run
    Map<KnowledgeBase, TAnnotation> results = new HashMap<>();
    try {
      for (Map.Entry<String, SemanticTableInterpreter> interpreterEntry : semanticTableInterpreters.entrySet()) {
        Constraints constraints = new DefaultFeedbackToConstraintsAdapter()
            .toConstraints(config.getFeedback(), new KnowledgeBase(interpreterEntry.getKey()));

        TAnnotation annotationResult = interpreterEntry.getValue().start(table, constraints);

        results.put(new KnowledgeBase(interpreterEntry.getKey()), annotationResult);
      }
    } catch (STIException e) {
      log.error("Error - running TableMinerPlus algorithm:");
      e.printStackTrace();
      return null;
    }

    // Odalic Result creation
    Result odalicResult = new DefaultAnnotationToResultAdapter().toResult(results);
    log.info("Odalic Result is: " + odalicResult);

    return odalicResult;
  }

  public static File getInputFile() {
    return inputFile;
  }

  public static Input getInput() {
    return input;
  }

  public static Configuration getConfiguration() {
    return config;
  }
}
