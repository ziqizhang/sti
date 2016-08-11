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
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
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
    
    List<TableColumn> columns = new ArrayList<TableColumn>();
   
    List<String> headers = input.headers();
    int i = 0;
    for (HeaderAnnotation headerAnnotation : result.getHeaderAnnotations()) {
      Set<EntityCandidate> chosenCandidates = headerAnnotation.getChosen().get(configuration.getPrimaryBase());
      boolean chosenIsEmpty = chosenCandidates.isEmpty();
      
      columns.add(createOriginalColumn(headers.get(i), chosenIsEmpty));
      
      if (!chosenIsEmpty) {
        columns.add(createDisambiguationColumn(headers.get(i)));
        
        for (EntityCandidate chosen : chosenCandidates) {
          columns.add(createClassificationColumn(headers.get(i), chosen.getEntity().getResource()));
        }
      }
      
      i++;
    }
    
    for (Entry<ColumnRelationPosition, ColumnRelationAnnotation> entry : result.getColumnRelationAnnotations().entrySet()) {
      for (EntityCandidate chosen : entry.getValue().getChosen().get(configuration.getPrimaryBase())) {
        columns.add(createRelationColumn(chosen.getEntity().getResource(),
            headers.get(entry.getKey().getFirstIndex()), headers.get(entry.getKey().getSecondIndex())));
      }
    }
    
    return new AnnotatedTable(input.identifier(), new TableSchema(columns));
  }
  
  private TableColumn createOriginalColumn(String columnName, boolean chosenIsEmpty) {
    return new TableColumn(columnName, Arrays.asList(columnName),
        "", chosenIsEmpty ? "" : "anyURI",
        false, false,
        chosenIsEmpty ? "" : bracketFormat(urlFormat(columnName)), "", chosenIsEmpty ? "" : "dcterms:title", "");
  }
  
  private TableColumn createClassificationColumn(String columnName, String resource) {
    return new TableColumn(typeFormat(columnName), Arrays.asList(""),
        "", "",
        true, false,
        bracketFormat(urlFormat(columnName)), "", "rdf:type", resource);
  }
  
  private TableColumn createDisambiguationColumn(String columnName) {
    return new TableColumn(urlFormat(columnName), Arrays.asList(""),
                           "", "",
                           false, true,
                           "", "", "", bracketFormat(urlFormat(columnName)));
  }
  
  private TableColumn createRelationColumn(String predicateName, String subjectName, String objectName) {
    return new TableColumn(predicateName, Arrays.asList(""),
        "", "",
        true, false,
        bracketFormat(urlFormat(subjectName)), "", predicateName, bracketFormat(urlFormat(objectName)));
  }
  
  private String urlFormat(String text) {
    return String.format("%s_url", text);
  }
  
  private String typeFormat(String text) {
    return String.format("%s_type", text);
  }
  
  private String bracketFormat(String text) {
    return String.format("{%s}", text);
  }
}
