package cz.cuni.mff.xrg.odalic.feedbacks.input;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;

public class DefaultTableToInputAdapter implements TableToInputAdapter {

  @Override
  public Input toInput(Table table) {
    SimpleInput result = new SimpleInput(table.getSourceId());

    for (int columnIndex = 0; columnIndex < table.getNumHeaders(); columnIndex++) {
      TColumnHeader header = table.getColumnHeader(columnIndex);
      result.insertHeader(header.getHeaderText(), columnIndex);
    }

    for (int columnIndex = 0; columnIndex < table.getNumCols(); columnIndex++) {
      for (int rowIndex = 0; rowIndex < table.getNumRows(); rowIndex++) {
        TCell cell = table.getContentCell(rowIndex, columnIndex);
        result.insertCell(cell.getText(), rowIndex, columnIndex);
      }
    }

    return result;
  }

}
