/**
 * 
 */
package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTableService;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.CsvExportService;

/**
 * A {@link RdfExportService} implementation based on {@link Rio}.
 * 
 * @author Václav Brodec
 *
 */
public class RioBackedRdfExportService implements RdfExportService {

  private final AnnotatedTableService annotatedTableService;
  
  private final CsvExportService csvExportService;
  
  private final AnnotatedTableToRDFExportAdapter annotatedTableToRdfExportAdapter;
  
  private final RDFExporter rdfExporter;

  @Autowired
  public RioBackedRdfExportService(AnnotatedTableService annotatedTableService, CsvExportService csvExportService, AnnotatedTableToRDFExportAdapter annotatedTableToRdfExportAdapter, RDFExporter rdfExporter) {
    Preconditions.checkNotNull(annotatedTableService);
    Preconditions.checkNotNull(csvExportService);
    Preconditions.checkNotNull(annotatedTableToRdfExportAdapter);
    Preconditions.checkNotNull(rdfExporter);
    
    this.annotatedTableService = annotatedTableService;
    this.csvExportService = csvExportService;
    this.annotatedTableToRdfExportAdapter = annotatedTableToRdfExportAdapter;
    this.rdfExporter = rdfExporter;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.outputs.rdfexport.RdfExportService#exportToTurtle(java.lang.String)
   */
  @Override
  public String exportToTurtle(String id)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    return export(id, RDFFormat.TURTLE);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.outputs.rdfexport.RdfExportService#exportToJsonLd(java.lang.String)
   */
  @Override
  public String exportToJsonLd(String id)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    return export(id, RDFFormat.JSONLD);
  }

  private String export(String id, final RDFFormat rdfFormat)
      throws InterruptedException, ExecutionException, IOException {
    final AnnotatedTable annotatedTable = annotatedTableService.getAnnotatedTableForTaskId(id);
    final Input extendedInput = csvExportService.getExtendedInputForTaskId(id);
    
    final Model rdfModel = annotatedTableToRdfExportAdapter.toRDFExport(annotatedTable, extendedInput);
    
    return rdfExporter.export(rdfModel, rdfFormat);
  }
}
