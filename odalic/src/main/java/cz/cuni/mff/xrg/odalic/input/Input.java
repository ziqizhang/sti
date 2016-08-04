package cz.cuni.mff.xrg.odalic.input;

import java.util.List;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

/**
 * Input interface.
 * 
 * @author Václav Brodec
 *
 */
public interface Input {
  String at(CellPosition position);
  String headerAt(ColumnPosition position);
  List<String> rowAt(RowPosition position);
  int rowsCount();
  int columnsCount();
  List<String> headers();
  List<List<String>> rows();
  String identifier();
}
