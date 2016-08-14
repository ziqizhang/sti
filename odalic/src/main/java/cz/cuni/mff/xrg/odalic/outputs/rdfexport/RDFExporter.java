package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 * RDF exporter.
 * 
 * @author Josef Janoušek
 *
 */
public interface RDFExporter {
  /**
   * Exports RDF Model content to RDF String.
   * 
   * @param rdfModel RDF Model content to export
   * @param rdfFormat format for RDF serialization
   * @return RDF String
   */
  String export(Model rdfModel, RDFFormat rdfFormat);
}
