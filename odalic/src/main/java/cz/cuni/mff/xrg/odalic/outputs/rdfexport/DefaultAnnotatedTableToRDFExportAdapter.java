package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableColumn;

/**
 * The default {@link AnnotatedTableToRDFExportAdapter} implementation.
 * 
 * @author Josef Janoušek
 *
 */
public class DefaultAnnotatedTableToRDFExportAdapter implements AnnotatedTableToRDFExportAdapter {

  /**
   * The default toRDFExport implementation.
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.rdfexport.AnnotatedTableToRDFExportAdapter#toRDFExport(cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable, cz.cuni.mff.xrg.odalic.input.Input)
   */
  @Override
  public Model toRDFExport(AnnotatedTable annotatedTable, Input extendedInput) {
    
    ValueFactory factory = SimpleValueFactory.getInstance();
    
    // create a new Model to put statements in
    Model model = new LinkedHashModel();
    
    HashMap<String, Integer> columnsNamesPositionsInput = new HashMap<>();
    int i = 0;
    for (String headerName : extendedInput.headers()) {
      columnsNamesPositionsInput.put(headerName, i);
      i++;
    }
    
    HashMap<String, TableColumn> namesColumnsAnnotatedTable = new HashMap<>();
    for (TableColumn column : annotatedTable.getTableSchema().getColumns()) {
      namesColumnsAnnotatedTable.put(column.getName(), column);
    }
    
    // fetch the relations in appropriate order
    HashMap<String, HashMap<String, HashMap<String, String>>> relations = new HashMap<>();
    for (TableColumn column : annotatedTable.getTableSchema().getColumns()) {
      if ((column.getSuppressOutput() == null || !column.getSuppressOutput()) && 
          StringUtils.isNoneBlank(column.getAboutUrl(), column.getPropertyUrl())) {
        relations.putIfAbsent(column.getAboutUrl(), new HashMap<>());
        relations.get(column.getAboutUrl()).putIfAbsent(column.getPropertyUrl(), new HashMap<>());
        if (column.getValueUrl() == null) {
          relations.get(column.getAboutUrl()).get(column.getPropertyUrl()).put(String.format("{%s}", column.getName()), 
              column.getDataType());
        }
        else if (isColumnLink(column.getValueUrl())) {
          relations.get(column.getAboutUrl()).get(column.getPropertyUrl()).put(column.getValueUrl(), 
              namesColumnsAnnotatedTable.get(column.getValueUrl().substring(1, column.getValueUrl().length()-1)).getDataType());
        }
        else {
          relations.get(column.getAboutUrl()).get(column.getPropertyUrl()).put(column.getValueUrl(), null);
        }
      }
    }
    
    // process the rows
    IRI subject, predicate;
    Value object;
    for (List<String> row : extendedInput.rows()) {
      for (Entry<String, HashMap<String, HashMap<String, String>>> subjectEntry : relations.entrySet()) {
        for (Entry<String, HashMap<String, String>> predicateEntry : subjectEntry.getValue().entrySet()) {
          for (Entry<String, String> objectEntry : predicateEntry.getValue().entrySet()) {
            
            // create the subject
            if (isColumnLink(subjectEntry.getKey())) {
              subject = factory.createIRI(row.get(
                  columnsNamesPositionsInput.get(subjectEntry.getKey().substring(1, subjectEntry.getKey().length()-1))));
            }
            else {
              subject = factory.createIRI(subjectEntry.getKey());
            }
            
            // create the predicate
            switch (predicateEntry.getKey()) {
              case DCTERMS_TITLE:
                predicate = DCTERMS.TITLE;
                break;
              case RDF_TYPE:
                predicate = RDF.TYPE;
                break;
              default:
                predicate = factory.createIRI(predicateEntry.getKey());
                break;
            }
            
            // create the object
            if (isColumnLink(objectEntry.getKey())) {
              int objectPosition = columnsNamesPositionsInput.get(objectEntry.getKey().substring(1, objectEntry.getKey().length()-1));
              if (ANY_URI.equals(objectEntry.getValue())) {
                object = factory.createIRI(row.get(objectPosition));
              }
              else {
                object = factory.createLiteral(row.get(objectPosition));
              }
            }
            else {
              object = factory.createIRI(objectEntry.getKey());
            }
            
            // add the RDF statement by providing subject, predicate and object
            model.add(subject, predicate, object);
          }
        }
      }
    }
    
    return model;
  }
  
  private static final String DCTERMS_TITLE = "dcterms:title";
  private static final String RDF_TYPE = "rdf:type";
  private static final String ANY_URI = "anyURI";
  
  private boolean isColumnLink(String value) {
    return value.startsWith("{") && value.endsWith("}");
  }
}
