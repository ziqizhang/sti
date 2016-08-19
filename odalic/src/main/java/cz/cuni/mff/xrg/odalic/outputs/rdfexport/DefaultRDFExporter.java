package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import java.io.FileWriter;
import java.io.StringWriter;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

/**
 * Default implementation of the {@link RDFExporter}.
 * 
 * @author Josef Janoušek
 *
 */
public class DefaultRDFExporter implements RDFExporter {

  /**
   * The default export implementation. 
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.rdfexport.RDFExporter#export(org.eclipse.rdf4j.model.Model, org.eclipse.rdf4j.rio.RDFFormat)
   */
  @Override
  public String export(Model rdfModel, RDFFormat rdfFormat) {
    
    StringWriter stringWriter = new StringWriter();
    
    Rio.write(rdfModel, stringWriter, rdfFormat);
    
    return stringWriter.toString();
  }
  
  public void exportToFile(Model rdfModel, RDFFormat rdfFormat, FileWriter fileWriter) {
    
    Rio.write(rdfModel, fileWriter, rdfFormat);
  }

}
