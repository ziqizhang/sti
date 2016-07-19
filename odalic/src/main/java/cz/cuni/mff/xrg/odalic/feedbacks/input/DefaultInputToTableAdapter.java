package cz.cuni.mff.xrg.odalic.feedbacks.input;

import com.sun.syndication.feed.rss.Guid;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.List;
import java.util.UUID;

public class DefaultInputToTableAdapter implements InputToTableAdapter {

  @Override
  public Table toTable(Input input) {
    Table result = new Table(
        UUID.randomUUID().toString(),
        input.fileIdentifier(),
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
