package cz.cuni.mff.xrg.odalic.tasks.executions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

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
import java.util.HashSet;
import java.util.Map;

import cz.cuni.mff.xrg.odalic.feedbacks.Classification;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.DefaultFeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.DefaultInputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;
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
    final SemanticTableInterpreterFactory factory = new TableMinerPlusFactory(new DefaultKnowledgeBaseSearchFactory(propertyFilePath), propertyFilePath);
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
    Feedback feedback = createFeedback(true);

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

  private static Feedback createFeedback(boolean emptyFeedback) {
    Feedback feedback = new Feedback();

    if (!emptyFeedback) {
      // subject columns example
      HashMap<KnowledgeBase, ColumnPosition> subjectColumns = new HashMap<>();
      subjectColumns.put(new KnowledgeBase("DBpedia Clone"), new ColumnPosition(0));

      // classifications example
      HashSet<EntityCandidate> candidatesClassification = new HashSet<>();
      candidatesClassification.add(new EntityCandidate(new Entity("http://schema.org/Bookxyz", "Booooook"), new Score(1.0)));
      candidatesClassification.add(new EntityCandidate(new Entity("http://schema.org/Book", "Book"), new Score(1.0)));
      HashMap<KnowledgeBase, HashSet<EntityCandidate>> headerAnnotation = new HashMap<>();
      headerAnnotation.put(new KnowledgeBase("DBpedia Clone"), candidatesClassification);
      HashSet<Classification> classifications = new HashSet<>();
      classifications.add(new Classification(new ColumnPosition(0), new HeaderAnnotation(headerAnnotation, headerAnnotation)));

      // disambiguations example
      HashSet<EntityCandidate> candidatesDisambiguation = new HashSet<>();
      candidatesDisambiguation.add(new EntityCandidate(
          new Entity("http://dbpedia.org/resource/Gardens_of_the_Moonxyz", "Gars of Moooooon"), new Score(1.0)));
      candidatesDisambiguation.add(new EntityCandidate(
          new Entity("http://dbpedia.org/resource/Gardens_of_the_Moon", "Gardens of the Moon"), new Score(1.0)));
      HashMap<KnowledgeBase, HashSet<EntityCandidate>> cellAnnotation = new HashMap<>();
      cellAnnotation.put(new KnowledgeBase("DBpedia Clone"), candidatesDisambiguation);
      HashSet<Disambiguation> disambiguations = new HashSet<>();
      disambiguations.add(new Disambiguation(new CellPosition(0, 0), new CellAnnotation(cellAnnotation, cellAnnotation)));

      // relations example
      HashSet<EntityCandidate> candidatesRelation = new HashSet<>();
      candidatesRelation.add(new EntityCandidate(new Entity("http://dbpedia.org/property/authorxyz", ""), new Score(1.0)));
      candidatesRelation.add(new EntityCandidate(new Entity("http://dbpedia.org/property/author", ""), new Score(1.0)));
      HashMap<KnowledgeBase, HashSet<EntityCandidate>> columnRelationAnnotation = new HashMap<>();
      columnRelationAnnotation.put(new KnowledgeBase("DBpedia Clone"), candidatesRelation);
      HashSet<ColumnRelation> relations = new HashSet<>();
      relations.add(new ColumnRelation(new ColumnRelationPosition(0, 1),
          new ColumnRelationAnnotation(columnRelationAnnotation, columnRelationAnnotation)));

      // ignore columns example
      HashSet<ColumnIgnore> columnIgnores = new HashSet<>();
      columnIgnores.add(new ColumnIgnore(new ColumnPosition(3)));

      // construction example
      feedback = new Feedback(subjectColumns, columnIgnores, ImmutableSet.of(),
          classifications, relations, disambiguations, ImmutableSet.of());
    }

    return feedback;
  }
}
