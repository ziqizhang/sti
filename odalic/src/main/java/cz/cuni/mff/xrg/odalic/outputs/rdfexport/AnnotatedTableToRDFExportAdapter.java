package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import org.eclipse.rdf4j.model.Model;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;

/**
 * Interface for {@link AnnotatedTable} to RDF Export {instance of @link Model} conversion.
 * 
 * @author Josef Janoušek
 *
 */
public interface AnnotatedTableToRDFExportAdapter {
  
  /**
   * Converts the annotated table to the RDF export (org.eclipse.rdf4j.model.Model).
   * 
   * @param annotatedTable annotated table
   * @param extendedInput extended input
   * @return model for RDF export
   */
  Model toRDFExport(AnnotatedTable annotatedTable, Input extendedInput);
}
