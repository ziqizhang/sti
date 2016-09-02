package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * The default {@link ResultToAnnotatedTableAdapter} implementation.
 * 
 * @author Josef Janoušek
 *
 */
public class DefaultResultToAnnotatedTableAdapter implements ResultToAnnotatedTableAdapter {

  /**
   * The default toAnnotatedTable implementation.
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.annotatedtable.ResultToAnnotatedTableAdapter#toAnnotatedTable(cz.cuni.mff.xrg.odalic.results.Result, cz.cuni.mff.xrg.odalic.input.Input, cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration)
   */
  @Override
  public AnnotatedTable toAnnotatedTable(Result result, Input input, Configuration configuration) {
    
    TableColumnBuilder builder = new TableColumnBuilder();
    List<TableColumn> columns = new ArrayList<TableColumn>();
    List<String> headers = input.headers();
    
    for (int i = 0; i < input.columnsCount(); i++) {
      boolean addPrimary = false;
      boolean addAlternatives = false;
      
      for (int j = 0; j < input.rowsCount(); j++) {
        for (Entry<KnowledgeBase, Set<EntityCandidate>> entry : result.getCellAnnotations()[j][i].getChosen().entrySet()) {
          if (entry.getValue() != null && !entry.getValue().isEmpty()) {
            if (entry.getKey().getName().equals(configuration.getPrimaryBase().getName())) {
              addPrimary = true;
            } else {
              addAlternatives = true;
            }
          }
        }
        
        if (addPrimary && addAlternatives) {
          break;
        }
      }
      
      if (addPrimary || addAlternatives) {
        columns.add(createOriginalDisambiguatedColumn(builder, headers.get(i)));
        
        columns.add(createDisambiguationColumn(builder, headers.get(i)));
      } else {
        columns.add(createOriginalNonDisambiguatedColumn(builder, headers.get(i)));
      }
      
      if (addAlternatives) {
        columns.add(createAlternativeDisambiguationColumn(builder, headers.get(i)));
      }
      
      for (Set<EntityCandidate> set : result.getHeaderAnnotations().get(i).getChosen().values()) {
        if (set != null && !set.isEmpty()) {
          for (EntityCandidate chosen : set) {
            columns.add(createClassificationColumn(builder, headers.get(i), chosen.getEntity().getResource()));
          }
        }
      }
    }
    
    for (Entry<ColumnRelationPosition, ColumnRelationAnnotation> entry : result.getColumnRelationAnnotations().entrySet()) {
      Set<EntityCandidate> chosenRelations = entry.getValue().getChosen().get(configuration.getPrimaryBase());
      
      if (chosenRelations != null) {
        for (EntityCandidate chosen : chosenRelations) {
          columns.add(createRelationColumn(builder, chosen.getEntity().getResource(),
              headers.get(entry.getKey().getFirstIndex()), headers.get(entry.getKey().getSecondIndex())));
        }
      }
    }
    
    return new AnnotatedTable(input.identifier(), new TableSchema(columns));
  }
  
  private static final String DCTERMS_TITLE = "dcterms:title";
  private static final String RDF_TYPE = "rdf:type";
  private static final String OWL_SAMEAS = "owl:sameAs";
  private static final String SEPARATOR = " ";
  private static final String STRING = "string";
  private static final String ANY_URI = "anyURI";
  
  private TableColumn createOriginalDisambiguatedColumn(TableColumnBuilder builder, String columnName) {
    builder.clear();
    builder.setName(columnName);
    builder.setTitles(Arrays.asList(columnName));
    builder.setDataType(STRING);
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setPropertyUrl(DCTERMS_TITLE);
    return builder.build();
  }
  
  private TableColumn createOriginalNonDisambiguatedColumn(TableColumnBuilder builder, String columnName) {
    builder.clear();
    builder.setName(columnName);
    builder.setTitles(Arrays.asList(columnName));
    builder.setDataType(STRING);
    return builder.build();
  }
  
  private TableColumn createClassificationColumn(TableColumnBuilder builder, String columnName, String resource) {
    builder.clear();
    builder.setName(typeFormat(columnName));
    builder.setVirtual(true);
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setPropertyUrl(RDF_TYPE);
    builder.setValueUrl(resource);
    return builder.build();
  }
  
  private TableColumn createDisambiguationColumn(TableColumnBuilder builder, String columnName) {
    builder.clear();
    builder.setName(urlFormat(columnName));
    builder.setDataType(ANY_URI);
    builder.setSuppressOutput(true);
    builder.setValueUrl(bracketFormat(urlFormat(columnName)));
    return builder.build();
  }
  
  private TableColumn createAlternativeDisambiguationColumn(TableColumnBuilder builder, String columnName) {
    builder.clear();
    builder.setName(alternativeUrlsFormat(columnName));
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setSeparator(SEPARATOR);
    builder.setPropertyUrl(OWL_SAMEAS);
    builder.setValueUrl(bracketFormat(alternativeUrlsFormat(columnName)));
    return builder.build();
  }
  
  private TableColumn createRelationColumn(TableColumnBuilder builder, String predicateName, String subjectName, String objectName) {
    builder.clear();
    builder.setName(predicateName);
    builder.setVirtual(true);
    builder.setAboutUrl(bracketFormat(urlFormat(subjectName)));
    builder.setPropertyUrl(predicateName);
    builder.setValueUrl(bracketFormat(urlFormat(objectName)));
    return builder.build();
  }
  
  private String urlFormat(String text) {
    return String.format("%s_url", text);
  }
  
  private String alternativeUrlsFormat(String text) {
    return String.format("%s_alternative_urls", text);
  }
  
  private String typeFormat(String text) {
    return String.format("%s_type", text);
  }
  
  private String bracketFormat(String text) {
    return String.format("{%s}", text);
  }
}
