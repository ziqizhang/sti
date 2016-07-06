package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
// import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.results.DefaultAnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
// import uk.ac.shef.dcs.sti.parser.table.TableParserLimayeDataset;
import uk.ac.shef.dcs.sti.xtractor.csv.TableXtractorCSV;

public class InterpreterExecutionBatch {

  /**
   * Expects sti.properties file path as the first and test file directory path as the second
   * command line argument
   * 
   * @param args command line arguments
   * 
   * @author Josef Janoušek
   * @author Jan Váňa
   */
  public static void main(String[] args) {
    final String propertyFilePath = args[0];
    final String testFileDirectoryPath = args[1];

    final SemanticTableInterpreterFactory factory = new TableMinerPlusFactory(propertyFilePath);
    final SemanticTableInterpreter semanticTableInterpreter = factory.getInterpreter();
    Preconditions.checkNotNull(semanticTableInterpreter);
    
    factory.setColumnIgnoresForInterpreter(ImmutableSet.of());

    final List<File> all = Arrays.asList(new File(testFileDirectoryPath).listFiles());
    final File inputFile = all.get(0);

    // Code for extraction from CSV
    TableXtractorCSV tableExtractor = new TableXtractorCSV();
    List<Table> tables = tableExtractor.extract(inputFile, inputFile.getName());
    if (tables.isEmpty()) {
      throw new IllegalArgumentException();
    }

    final TAnnotation annotationResult;
    try {
      annotationResult = semanticTableInterpreter.start(tables.get(0), true);

      System.out.println("Result - OK:");
      System.out.println(annotationResult.toString());
      
      DefaultAnnotationToResultAdapter adapter = new DefaultAnnotationToResultAdapter();
      Result odalicResult = adapter.toResult(ImmutableMap.of(new KnowledgeBase("DBpedia"), annotationResult));
      
      System.out.println(odalicResult.toString());
    } catch (STIException e) {
      System.out.println("Result - Error:");
      e.printStackTrace();
    }
    System.out.println("End of result.");
  }

}
