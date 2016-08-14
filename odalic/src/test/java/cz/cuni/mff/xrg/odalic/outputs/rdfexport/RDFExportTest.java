package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import com.google.gson.Gson;

import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;

public class RDFExportTest {

  /**
   * Expects annotated table JSON file path as the first and extended input CSV file path as the second
   * command line argument
   * 
   * @param args command line arguments
   * 
   * @author Josef Janoušek 
   */
  public static void main(String[] args) {
    final String jsonFilePath = args[0];
    final String csvFilePath = args[1];
    
    // Convert JSON to Java Object AnnotatedTable
    AnnotatedTable annotatedTable;
    try (Reader reader = new FileReader(jsonFilePath)) {
      annotatedTable = new Gson().fromJson(reader, AnnotatedTable.class);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    
    // Code for extraction from CSV
    File inputFile = new File(csvFilePath);
    Input extendedInput;
    try (final FileInputStream inputFileStream = new FileInputStream(inputFile)) {
      extendedInput = new DefaultCsvInputParser(new ListsBackedInputBuilder())
          .parse(inputFileStream, inputFile.getName(), new CsvConfiguration());
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    
    // Export from annotated table to RDF file
    testExportToRDFFile(annotatedTable, extendedInput, 
        inputFile.getParent() + File.separator + FilenameUtils.getBaseName(inputFile.getName()) + ".rdf");
  }
  
  public static void testExportToRDFFile(AnnotatedTable annotatedTable, Input extendedInput, String filePath) {
    // Conversion from annotated table to RDF model
    Model rdfModel = new DefaultAnnotatedTableToRDFExportAdapter().toRDFExport(annotatedTable, extendedInput);
    System.out.println(rdfModel);
    
    // Export RDF Model to RDF String (in turtle format)
    String rdf = new DefaultRDFExporter().export(rdfModel, RDFFormat.TURTLE);
    System.out.println(rdf);
    
    // Write RDF String to file
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(rdf);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
