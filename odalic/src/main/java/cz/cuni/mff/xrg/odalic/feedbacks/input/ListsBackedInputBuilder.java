package cz.cuni.mff.xrg.odalic.feedbacks.input;

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
public final class ListsBackedInputBuilder {
  
  private String fileIdentifier;
  private List<String> headers = new ArrayList<>();
  private List<List<String>> rows = new ArrayList<>();
  
  public ListsBackedInputBuilder() { }
  
  void setFileIdentifier(String fileIdentifier) {
    Preconditions.checkNotNull(fileIdentifier);
    
    this.fileIdentifier = fileIdentifier;
  }
  
  public void insertCell(String value, int rowIndex, int columnIndex) {
    while (rows.size() <= rowIndex) {
      rows.add(new ArrayList<>());
    }

    insertToList(rows.get(rowIndex), value, columnIndex);
  }

  public void insertHeader(String value, int position) {
    insertToList(headers, value, position);
  }

  public void insertToList(List<String> list, String value, int position){
    while (list.size() <= position) {
      list.add(null);
    }

    list.set(position, value);
  }
  
  ListsBackedInput build() {
    return new ListsBackedInput(this.fileIdentifier, this.headers, this.rows);
  }

  public void clear() {
    this.fileIdentifier = null;
    headers.clear();
    rows.clear();
  }
}
