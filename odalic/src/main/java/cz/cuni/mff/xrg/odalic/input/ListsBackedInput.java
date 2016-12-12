package cz.cuni.mff.xrg.odalic.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

/**
 * An {@link Input} implementation using a list of lists to store the cells.
 * 
 * @author Václav Brodec
 * @author Jan Váňa
 */
@XmlRootElement(name = "input")
@XmlAccessorType(XmlAccessType.NONE)
@Immutable
public final class ListsBackedInput implements Input, Serializable {

  private static final long serialVersionUID = 4101912998363935336L;

  @XmlElement
  private final List<List<String>> rows;
  
  @XmlElement
  private final List<String> headers;
  
  @XmlElement
  private final String fileIdentifier;

  /**
   * 
   * @param fileIdentifier
   * @param headers
   * @param rows Note: Inner lists (concrete rows) can contain null values.
   */
  public ListsBackedInput(String fileIdentifier, List<? extends String> headers, List<? extends List<? extends String>> rows) {
    Preconditions.checkNotNull(fileIdentifier);
    Preconditions.checkNotNull(headers);
    Preconditions.checkNotNull(rows);
    
    this.fileIdentifier = fileIdentifier;
    
    this.headers = ImmutableList.copyOf(headers);
    
    final List<List<String>> mutableRows = new ArrayList<>(rows.size());
    for (final List<? extends String> row : rows) {
      mutableRows.add(Collections.unmodifiableList(new ArrayList<>(row)));
    }
    this.rows = Collections.unmodifiableList(mutableRows);
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

  /**
   * Note: Inner lists (concrete rows) can contain null values.
   */
  @Override
  public List<List<String>> rows() {
    return rows;
  }

  @Override
  public String identifier() {
    return fileIdentifier;
  }
}
