package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.EntityValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;


public class EntityAdapter
    extends XmlAdapter<EntityValue, Entity> {

  @Override
  public EntityValue marshal(Entity bound)
      throws Exception {
    return new EntityValue(bound);
  }

  @Override
  public Entity unmarshal(EntityValue value)
      throws Exception {
    return new Entity(value.getResource(),
        value.getLabel());
  }
}
