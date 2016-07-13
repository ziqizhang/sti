package cz.cuni.mff.xrg.odalic.feedbacks.input;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;
import cz.cuni.mff.xrg.odalic.util.Arrays;

@XmlRootElement(name = "input")
public class SimpleInput implements Input, Serializable {

  private static final long serialVersionUID = 4101912998363935336L;
  
  @XmlElement
  private final List<String> headers;
  
  @XmlElement
  private final String[][] content;

  @SuppressWarnings("unused")
  private SimpleInput() {
    headers = ImmutableList.of();
    content = new String[0][0];
  }
  
  /**
   * @param headers
   * @param content
   */
  public SimpleInput(List<? extends String> headers, String[][] content) {
    Preconditions.checkNotNull(headers);
    Preconditions.checkNotNull(content);
    
    Preconditions.checkArgument(!Arrays.containsNull(content));
    Preconditions.checkArgument(Arrays.isMatrix(content));
    
    this.headers = ImmutableList.copyOf(headers);
    this.content = Arrays.deepCopy(String.class, content);
  }

  /**
   * @return the headers
   */
  public List<String> getHeaders() {
    return headers;
  }

  /**
   * @return the content
   */
  public String[][] getContent() {
    return Arrays.deepCopy(String.class, content);
  }

  @Override
  public String at(CellPosition position) {
    return content[position.getRowIndex()][position.getColumnIndex()];
  }

  @Override
  public String headerAt(ColumnPosition position) {
    return headers.get(position.getIndex());
  }

  @Override
  public int rowsCount() {
    return content.length;
  }

  @Override
  public int columnsCount() {
    return headers.size();
  }

  @Override
  public List<String> headers() {
    return headers;
  }

  @Override
  public List<String> rowAt(RowPosition position) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> columnAt(ColumnPosition position) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<List<String>> rows() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<List<String>> columns() {
    // TODO Auto-generated method stub
    return null;
  }

}
