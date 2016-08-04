package cz.cuni.mff.xrg.odalic.input;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * Converts the {@link Table} to {@link ListsBackedInputBuilder}.
 * 
 * @author Jan Váňa
 *
 */
public final class TableToListsBackedInputAdapter implements TableToInputAdapter {

  private final ListsBackedInputBuilder builder;

  public TableToListsBackedInputAdapter(ListsBackedInputBuilder builder) {
    Preconditions.checkNotNull(builder);

    this.builder = builder;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.input.TableToInputAdapter#toInput(uk.ac.shef.dcs.sti.core.model.Table)
   */
  @Override
  public Input toInput(Table table) {
    builder.clear();
    builder.setFileIdentifier(table.getSourceId());

    for (int columnIndex = 0; columnIndex < table.getNumHeaders(); columnIndex++) {
      TColumnHeader header = table.getColumnHeader(columnIndex);
      builder.insertHeader(header.getHeaderText(), columnIndex);
    }

    for (int columnIndex = 0; columnIndex < table.getNumCols(); columnIndex++) {
      for (int rowIndex = 0; rowIndex < table.getNumRows(); rowIndex++) {
        TCell cell = table.getContentCell(rowIndex, columnIndex);
        builder.insertCell(cell.getText(), rowIndex, columnIndex);
      }
    }

    return builder.build();
  }

}
