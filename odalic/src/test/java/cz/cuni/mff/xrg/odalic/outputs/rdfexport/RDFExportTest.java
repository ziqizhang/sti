package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import com.google.gson.Gson;
import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;

/**
 * JUnit test for RDF export
 * 
 * @author Josef Janoušek
 * @author Tomáš Knap
 *
 */
public class RDFExportTest {

  private static final Logger log = LoggerFactory.getLogger(RDFExportTest.class);
  
  static File inputJsonFile;
  static File inputFile;

  @BeforeClass
  public static void beforeClass() throws URISyntaxException, IOException {

    inputJsonFile = new File(RDFExportTest.class.getClassLoader().getResource("book.json").toURI());
    inputFile = new File(RDFExportTest.class.getClassLoader().getResource("book.csv").toURI());
  }

  @Test
  public void TestConversionToTurtle() {

    // Convert JSON to Java Object AnnotatedTable
    AnnotatedTable annotatedTable;
    try (Reader reader = new FileReader(inputJsonFile)) {
      annotatedTable = new Gson().fromJson(reader, AnnotatedTable.class);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    // Code for extraction from CSV
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

    //TODO write some assert which e.g. checks that the resulting turtle file contains some triples
    //TODO use logger, do not log to console


  }
  
  public static void testExportToRDFFile(AnnotatedTable annotatedTable, Input extendedInput, String filePath) {
    // Conversion from annotated table to RDF model
    Model rdfModel = new DefaultAnnotatedTableToRDFExportAdapter().toRDFExport(annotatedTable, extendedInput);

    // Export RDF Model to RDF String (in turtle format)
    String rdf = new DefaultRDFExporter().export(rdfModel, RDFFormat.TURTLE);
    log.info("Resulting RDF is: " + rdf);
    
    // Write RDF String to file
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(rdf);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
