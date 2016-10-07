package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.TableColumnValue;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableColumn;


public final class TableColumnAdapter extends XmlAdapter<TableColumnValue, TableColumn> {

  @Override
  public TableColumnValue marshal(TableColumn bound) throws Exception {
    return new TableColumnValue(bound);
  }

  @Override
  public TableColumn unmarshal(TableColumnValue value) throws Exception {
    return new TableColumn(value.getName(), value.getTitles(), value.getDescription(),
        value.getDataType(), value.getVirtual(), value.getSuppressOutput(),
        value.getAboutUrl(), value.getSeparator(), value.getPropertyUrl(), value.getValueUrl());
  }
}
