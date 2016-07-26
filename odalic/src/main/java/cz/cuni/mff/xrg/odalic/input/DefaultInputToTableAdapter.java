package cz.cuni.mff.xrg.odalic.input;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.List;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

/**
 * The default {@link InputToTableAdapter} implementation.
 * 
 * @author Jan Váňa
 *
 */
@Immutable
public class DefaultInputToTableAdapter implements InputToTableAdapter {

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.input.InputToTableAdapter#toTable(cz.cuni.mff.xrg.odalic.input.Input)
   */
  @Override
  public Table toTable(Input input) {
    Table result = new Table(
        UUID.randomUUID().toString(),
        input.identifier(),
        input.rowsCount(),
        input.columnsCount());

    int columnIndex = 0;
    for (String value : input.headers()) {
      TColumnHeader header = new TColumnHeader(value);
      result.setColumnHeader(columnIndex, header);

      columnIndex++;
    }

    int rowIndex = 0;
    for (List<String> row : input.rows()) {
      columnIndex = 0;
      for (String value : row) {
        TCell cell = new TCell(value);
        result.setContentCell(rowIndex, columnIndex, cell);

        columnIndex++;
      }
      rowIndex++;
    }

    return result;
  }

}
