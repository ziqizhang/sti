package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.AnnotatedTableValue;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;


public final class AnnotatedTableAdapter extends XmlAdapter<AnnotatedTableValue, AnnotatedTable> {

  @Override
  public AnnotatedTableValue marshal(AnnotatedTable bound) throws Exception {
    return new AnnotatedTableValue(bound);
  }

  @Override
  public AnnotatedTable unmarshal(AnnotatedTableValue value) throws Exception {
    return new AnnotatedTable(value.getUrl(), value.getTableSchema());
  }
}
