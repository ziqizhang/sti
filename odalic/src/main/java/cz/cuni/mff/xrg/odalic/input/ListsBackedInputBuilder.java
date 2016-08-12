package cz.cuni.mff.xrg.odalic.input;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Incrementally, row by row, helps to produce the complete {@link ListsBackedInput}.
 * 
 * @author Jan Váňa
 * @author Václav Brodec
 *
 */
public final class ListsBackedInputBuilder implements InputBuilder {
  
  private String fileIdentifier;
  private List<String> headers = new ArrayList<>();
  private List<List<String>> rows = new ArrayList<>();
  
  public ListsBackedInputBuilder() { }
  
  public ListsBackedInputBuilder(Input initialInput) {
    setFileIdentifier(initialInput.identifier());
    headers.addAll(initialInput.headers());
    for (List<String> rowList : initialInput.rows()) {
      List<String> newList = new ArrayList<>();
      newList.addAll(rowList);
      rows.add(newList);
    }
  }
  
  void setFileIdentifier(String fileIdentifier) {
    Preconditions.checkNotNull(fileIdentifier);
    
    this.fileIdentifier = fileIdentifier;
  }
  
  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#insertCell(java.lang.String, int, int)
   */
  @Override
  public void insertCell(String value, int rowIndex, int columnIndex) {
    while (rows.size() <= rowIndex) {
      rows.add(new ArrayList<>());
    }

    insertToList(rows.get(rowIndex), value, columnIndex);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#insertHeader(java.lang.String, int)
   */
  @Override
  public void insertHeader(String value, int position) {
    insertToList(headers, value, position);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#insertToList(java.util.List, java.lang.String, int)
   */
  @Override
  public void insertToList(List<String> list, String value, int position){
    while (list.size() <= position) {
      list.add(null);
    }

    list.set(position, value);
  }
  
  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#build()
   */
  @Override
  public Input build() {
    return new ListsBackedInput(this.fileIdentifier, this.headers, this.rows);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#clear()
   */
  @Override
  public void clear() {
    this.fileIdentifier = null;
    headers.clear();
    rows.clear();
  }
}
