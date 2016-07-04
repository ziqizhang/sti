package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
//import uk.ac.shef.dcs.sti.parser.table.TableParserLimayeDataset;
import uk.ac.shef.dcs.sti.xtractor.csv.TableXtractorCSV;

public class InterpreterExecutionBatch {

  public static void main(String[] args) {
    // for test of execution of semantic table interpreter from Odalic project
    
    final String propertyFilePath = args[0];
    final String testFileDirectoryPath = args[1];
    
    SemanticTableInterpreterFactory factory = new TableMinerPlusFactory(propertyFilePath);
    SemanticTableInterpreter semanticTableInterpreter = factory.getInterpreter();
    Preconditions.checkNotNull(semanticTableInterpreter);
    factory.setIgnoreColumnsForInterpreter(new Integer[]{});
    
    //TODO: Write your own path to the folder containing (only) the input file.
    List<File> all = Arrays.asList(new File(testFileDirectoryPath).listFiles());
    File inputFile = all.get(0);
    
    // code for extraction from CSV
    TableXtractorCSV tableExtractor = new TableXtractorCSV();
    List<Table> tables = tableExtractor.extract(inputFile, inputFile.getName());
    
    // code for extraction from LimayeDataset
    /*TableParserLimayeDataset tableExtractor = new TableParserLimayeDataset();
    List<Table> tables;
    try {
      tables = tableExtractor.extract(inputFile.toString(), inputFile.toString());
    } catch (STIException e1) {
      e1.printStackTrace();
      tables = new ArrayList<>();
    }*/
    
    if (tables.isEmpty()) {
      throw new IllegalArgumentException();
    }
    
    TAnnotation annotationResult;
    try {
      annotationResult = semanticTableInterpreter.start(tables.get(0), true);
      
      System.out.println("V�sledek - OK:");
      System.out.println(annotationResult.toString());
    } catch (STIException e) {
      System.out.println("V�sledek - Chyba:");
      e.printStackTrace();
    }
    System.out.println("Konec v�sledku.");
  }

}
