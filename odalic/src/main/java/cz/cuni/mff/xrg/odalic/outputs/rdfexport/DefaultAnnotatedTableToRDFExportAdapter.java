package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableColumn;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp.DataPropertyTriplePattern;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp.ObjectPropertyTriplePattern;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp.TriplePattern;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default {@link AnnotatedTableToRDFExportAdapter} implementation.
 * 
 * @author Josef Janoušek
 * @author Tomáš Knap
 *
 */
public class DefaultAnnotatedTableToRDFExportAdapter implements AnnotatedTableToRDFExportAdapter {

  private static final Logger log = LoggerFactory.getLogger(DefaultAnnotatedTableToRDFExportAdapter.class);

  private static final String DCTERMS_TITLE = "dcterms:title";
  private static final String RDF_TYPE = "rdf:type";
  
  private ValueFactory factory = SimpleValueFactory.getInstance();
  
  /**
   * The default toRDFExport implementation.
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.rdfexport.AnnotatedTableToRDFExportAdapter#toRDFExport(cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable, cz.cuni.mff.xrg.odalic.input.Input)
   */
  @Override
  public Model toRDFExport(AnnotatedTable annotatedTable, Input extendedInput) {

    // map for accessing column positions by column names in input
    Map<String, Integer> positionsForColumnNames = new HashMap<>();
    int i = 0;
    for (String headerName : extendedInput.headers()) {
      positionsForColumnNames.put(headerName, i);
      i++;
    }

    // fetch the triplePatterns from annotated table
    log.debug("Preparing set of triple patterns to be applied to all rows");
    List<TriplePattern> triplePatterns = new ArrayList<>();
    for (TableColumn column : annotatedTable.getTableSchema().getColumns()) {
      if (column.getSuppressOutput() != null && column.getSuppressOutput()) {
        // we do not create any triple for the suppressed column
        log.debug("Column has suppressed output, we do not create any triple for {}", column.getName());
        continue;
      }
      if (StringUtils.isEmpty(column.getPropertyUrl())) {
        log.warn("PropertyUrl is not defined for the column {}, no triple is created for that column", column.getName());
        continue;
      }
      if (StringUtils.isEmpty(column.getAboutUrl())) {
        log.warn("AboutUrl is not defined for the column {}, no triple is created for that column", column.getName());
        // Currently we require aboutUrl to be defined for all columns. Nevertheless, based on the spec, this is not required and
        //  if aboutUrl is not defined on the column, it may be e.g. defined at the level of whole tableScheme.
        // Also aboutUrl may contain more complex patterns, e.g.: "aboutUrl": "http://example.org/tree/{on_street}/{GID}", but so far we expect that
        //  it contains only "{columnName}"
        continue;
      }

      TriplePattern tp;
      if (column.getValueUrl() == null) {
        // if valueUrl is null, than we now that we should generate triple with data property (object is literal)
        tp = new DataPropertyTriplePattern(column.getAboutUrl(), createPredicateIRI(column.getPropertyUrl()), column.getName());

      }
      else {
        // it is object property
        // so far we suppose that valueUrl contains either the URL itself or pattern in the form {columnName}, meaning that URL is taken from that column.
        tp = new ObjectPropertyTriplePattern(column.getAboutUrl(), createPredicateIRI(column.getPropertyUrl()), column.getValueUrl());
      }
      triplePatterns.add(tp);

    }
    
    // create a new Model to put statements in
    Model model = new LinkedHashModel();
    
    // process the rows from extended input
    log.debug("Iterating over set of row and creating triples for each row");
    IRI subject;
    Value object = null;
    for (List<String> row : extendedInput.rows()) {
      for (TriplePattern tp : triplePatterns) {
        
        // create the subject
        // currently we expect only subject patterns of the form {columnName}
        String columnSubjectName = getColumnName(tp.getSubjectPattern());
        if (positionsForColumnNames.get(columnSubjectName) == null) {
          // column with that name does not exist, so we can not produce the triple
          log.warn("Column named '{}' does not exist, no triple is produced", columnSubjectName);
          continue;
        }
        int subjectPosition = positionsForColumnNames.get(columnSubjectName);
        subject = factory.createIRI(row.get(subjectPosition));
        
        // create the object
        if (tp instanceof DataPropertyTriplePattern) {
          // it is data property
          String columnName = ((DataPropertyTriplePattern)tp).getObjectColumnName();
          if (positionsForColumnNames.get(columnName) == null) {
            // column with that name does not exist, so we can not produce the triple
            log.warn("Column named '{}' does not exist, no triple is produced", columnName);
            continue;
          }
          int objectPosition = positionsForColumnNames.get(columnName);
          object = factory.createLiteral(row.get(objectPosition));
        }
        else if (tp instanceof ObjectPropertyTriplePattern){
          // it is object property
          ObjectPropertyTriplePattern otp = (ObjectPropertyTriplePattern) tp;
          if (isColumnLink(otp.getObjectPattern())) {
            String columnName = getColumnName(otp.getObjectPattern());
            if (positionsForColumnNames.get(columnName) == null) {
              // column with that name does not exist, so we can not produce the triple
              log.warn("Column named '{}' does not exist, no triple is produced", columnName);
              continue;
            }
            int objectPosition = positionsForColumnNames.get(columnName);
            object = factory.createIRI(row.get(objectPosition));
          }
          else {
            // object pattern contains URI:
            object = factory.createIRI(otp.getObjectPattern());
          }
        }
        else {
          log.error("Unsupported Triple Pattern");
        }
        
        // add the RDF statement by providing subject, predicate and object
        model.add(subject, tp.getPredicate(), object);
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

    //TODO (in the future) Parsing of the predicates must be done differently - it should construct the predicate URL from the JSON, not from some predefined list.
    //  It is not a good practise to change the code here when new predicate is added
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
  private String getColumnName(String columnLink) {
    return columnLink.substring(1, columnLink.length()-1);
  }
}
