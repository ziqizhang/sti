package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

  private static final String DCTERMS_TITLE = "dcterms:title";
  private static final String RDF_TYPE = "rdf:type";
  private static final String ANY_URI = "anyURI";
  
  private ValueFactory factory = SimpleValueFactory.getInstance();
  
  /**
   * The default toRDFExport implementation.
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.rdfexport.AnnotatedTableToRDFExportAdapter#toRDFExport(cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable, cz.cuni.mff.xrg.odalic.input.Input)
   */
  @Override
  public Model toRDFExport(AnnotatedTable annotatedTable, Input extendedInput) {
    
    // map for accessing column positions by column names in input
    HashMap<String, Integer> inputColumnNamesPositions = new HashMap<>();
    int i = 0;
    for (String headerName : extendedInput.headers()) {
      inputColumnNamesPositions.put(headerName, i);
      i++;
    }
    
    // map for accessing columns by names in annotated table
    HashMap<String, TableColumn> annotatedTableNamesColumns = new HashMap<>();
    for (TableColumn column : annotatedTable.getTableSchema().getColumns()) {
      annotatedTableNamesColumns.put(column.getName(), column);
    }
    
    // fetch the relations from annotated table
    ArrayList<TriplePattern> relations = new ArrayList<>();
    for (TableColumn column : annotatedTable.getTableSchema().getColumns()) {
      if ((column.getSuppressOutput() == null || !column.getSuppressOutput()) && 
          StringUtils.isNoneBlank(column.getAboutUrl(), column.getPropertyUrl())) {
        String valueUrl = column.getValueUrl();
        if (valueUrl == null) {
          valueUrl = String.format("{%s}", column.getName());
        }
        relations.add(new TriplePattern(column.getAboutUrl(), createPredicateIRI(column.getPropertyUrl()), valueUrl));
      }
    }
    
    // create a new Model to put statements in
    Model model = new LinkedHashModel();
    
    // process the rows from extended input
    IRI subject;
    Value object;
    for (List<String> row : extendedInput.rows()) {
      for (TriplePattern relation : relations) {
        
        // create the subject
        if (isColumnLink(relation.getSubjectPattern())) {
          subject = factory.createIRI(row.get(
              inputColumnNamesPositions.get(getNameFromColumnLink(relation.getSubjectPattern()))));
        }
        else {
          subject = factory.createIRI(relation.getSubjectPattern());
        }
        
        // create the object
        if (isColumnLink(relation.getObjectPattern())) {
          String columnName = getNameFromColumnLink(relation.getObjectPattern());
          int objectPosition = inputColumnNamesPositions.get(columnName);
          if (ANY_URI.equals(annotatedTableNamesColumns.get(columnName).getDataType())) {
            object = factory.createIRI(row.get(objectPosition));
          }
          else {
            object = factory.createLiteral(row.get(objectPosition));
          }
        }
        else {
          object = factory.createIRI(relation.getObjectPattern());
        }
        
        // add the RDF statement by providing subject, predicate and object
        model.add(subject, relation.getPredicate(), object);
      }
    }
    
    return model;
  }
  
  /**
   * creates IRI from given predicate constant (dcterms:title or rdf:type) or URL String
   * 
   * @param predicateString
   * @return
   */
  private IRI createPredicateIRI(String predicateString) {
    switch (predicateString) {
      case DCTERMS_TITLE:
        return DCTERMS.TITLE;
      case RDF_TYPE:
        return RDF.TYPE;
      default:
        return factory.createIRI(predicateString);
    }
  }
  
  /**
   * returns true when the value starts with "{" and ends with "}"
   * 
   * @param value
   * @return
   */
  private boolean isColumnLink(String value) {
    return value.startsWith("{") && value.endsWith("}");
  }
  
  /**
   * Takes the column link value from Annotated table (e.g. "{Book_url}")
   * and returns only linked column name without brackets (e.g. "Book_url").
   * 
   * @param columnLink column link with brackets
   * @return column name without brackets
   */
  private String getNameFromColumnLink(String columnLink) {
    return columnLink.substring(1, columnLink.length()-1);
  }
}
