package cz.cuni.mff.xrg.odalic.feedbacks.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

@XmlRootElement(name = "input")
public class SimpleInput implements Input, Serializable {

  private static final long serialVersionUID = 4101912998363935336L;

  @XmlElement
  private final List<List<String>> rows = new ArrayList<>();
  @XmlElement
  private final List<String> headers = new ArrayList<>();
  @XmlElement
  private final String fileIdentifier;

  public SimpleInput(String fileIdentifier) {
    this.fileIdentifier = fileIdentifier;
  }

  @Override
  public String at(CellPosition position) {
    return rows.get(position.getRowIndex()).get(position.getColumnIndex());
  }

  @Override
  public String headerAt(ColumnPosition position) {
    return headers.get(position.getIndex());
  }

  @Override
  public List<String> rowAt(RowPosition position) {
    return rows.get(position.getIndex());
  }

  @Override
  public int rowsCount() {
    return rows.size();
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
  public List<List<String>> rows() {
    return rows;
  }

  @Override
  public String fileIdentifier() {
    return fileIdentifier;
  }

  void insertCell(String value, int rowIndex, int columnIndex) {
    while (rows.size() <= rowIndex) {
      rows.add(new ArrayList<>());
    }

    insertToList(rows.get(rowIndex), value, columnIndex);
  }

  void insertHeader(String value, int position) {
    insertToList(headers, value, position);
  }

  private void insertToList(List<String> list, String value, int position){
    while (list.size() <= position) {
      list.add(null);
    }

    list.set(position, value);
  }
}
