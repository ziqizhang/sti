package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;


public class ConfigurationAdapter extends XmlAdapter<ConfigurationValue, Configuration> {

  @Override
  public ConfigurationValue marshal(Configuration bound) throws Exception {
    return new ConfigurationValue(bound);
  }

  @Override
  public Configuration unmarshal(ConfigurationValue value) throws Exception {
    return new Configuration(value.getInput(), value.getFeedback());
  }
}
