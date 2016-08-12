package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.DefaultInputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.DefaultResultToAnnotatedTableAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.DefaultAnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

public class InterpreterExecutionBatch {

  /**
   * Expects sti.properties file path as the first and test file directory path as the second
   * command line argument
   * 
   * @param args command line arguments
   * 
   * @author Josef Janoušek
   * @author Jan Váňa
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    final String propertyFilePath = args[0];
    final String testFileDirectoryPath = args[1];

    final SemanticTableInterpreterFactory factory = new TableMinerPlusFactory(propertyFilePath);
    final Map<String, SemanticTableInterpreter> semanticTableInterpreters = factory.getInterpreters();
    Preconditions.checkNotNull(semanticTableInterpreters);

    factory.setColumnIgnoresForInterpreter(ImmutableSet.of());

    final List<File> all = Arrays.asList(new File(testFileDirectoryPath).listFiles());
    final File inputFile = all.get(0);

    // Code for extraction from CSV
    try (final FileInputStream inputFileStream = new FileInputStream(inputFile)) {
      final Input input = new DefaultCsvInputParser(new ListsBackedInputBuilder())
          .parse(inputFileStream, inputFile.getName(), new CsvConfiguration());
      final Table table = new DefaultInputToTableAdapter().toTable(input);

      try {
        Map<KnowledgeBase, TAnnotation> results = new HashMap<>();
        for(Map.Entry<String, SemanticTableInterpreter> interpreterEntry : semanticTableInterpreters.entrySet()) {
          TAnnotation annotationResult = interpreterEntry.getValue().start(table, true);

          System.out.println("Result - OK:");
          System.out.println(annotationResult.toString());
          results.put(new KnowledgeBase(interpreterEntry.getKey()), annotationResult);
        }

        DefaultAnnotationToResultAdapter adapter = new DefaultAnnotationToResultAdapter();
        Result odalicResult =
            adapter.toResult(results);

        System.out.println(odalicResult.toString());
        
        DefaultResultToAnnotatedTableAdapter adapter2 = new DefaultResultToAnnotatedTableAdapter();
        AnnotatedTable annotatedTable =
            adapter2.toAnnotatedTable(odalicResult, input, new Configuration(new cz.cuni.mff.xrg.odalic.files.File(inputFile.getName(), "x", inputFile.toURI().toURL()), new KnowledgeBase("DBpedia")));
        
        System.out.println(annotatedTable.toString());
      } catch (STIException e) {
        System.out.println("Result - Error:");
        e.printStackTrace();
      }
      System.out.println("End of result.");
    }
  }

}
