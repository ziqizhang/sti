package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.util.List;
import java.util.Set;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * The default {@link ResultToCSVExportAdapter} implementation.
 * 
 * @author Josef Janoušek
 *
 */
public class DefaultResultToCSVExportAdapter implements ResultToCSVExportAdapter {

  /**
   * The default toCSVExport implementation.
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.csvexport.ResultToCSVExportAdapter#toCSVExport(cz.cuni.mff.xrg.odalic.results.Result, cz.cuni.mff.xrg.odalic.input.Input, cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration)
   */
  @Override
  public Input toCSVExport(Result result, Input input, Configuration configuration) {
    
    ListsBackedInputBuilder builder = new ListsBackedInputBuilder(input);
    
    List<String> headers = input.headers();
    int newPosition = input.columnsCount();
    int i = 0;
    for (HeaderAnnotation headerAnnotation : result.getHeaderAnnotations()) {
      Set<EntityCandidate> chosenCandidates = headerAnnotation.getChosen().get(configuration.getPrimaryBase());
      boolean chosenIsEmpty = chosenCandidates.isEmpty();
      
      if (!chosenIsEmpty) {
        builder.insertHeader(urlFormat(headers.get(i)), newPosition);
        
        for (int j = 0; j < input.rowsCount(); j++) {
          chosenCandidates = result.getCellAnnotations()[j][i].getChosen().get(configuration.getPrimaryBase());
          
          for (EntityCandidate chosen : chosenCandidates) {
            builder.insertCell(chosen.getEntity().getResource(), j, newPosition);
          }
        }
        
        newPosition++;
      }
      
      i++;
    }
    
    return builder.build();
  }
  
  private String urlFormat(String text) {
    return String.format("%s_url", text);
  }
}
