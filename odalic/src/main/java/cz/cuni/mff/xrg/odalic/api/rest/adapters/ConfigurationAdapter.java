package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.springframework.beans.factory.annotation.Autowired;

import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;


public class ConfigurationAdapter extends XmlAdapter<ConfigurationValue, Configuration> {

  private final FileService fileService;
  
  @Autowired
  public ConfigurationAdapter(FileService fileService) {
    this.fileService = fileService; 
  }
  
  @Override
  public ConfigurationValue marshal(Configuration bound) throws Exception {
    return new ConfigurationValue(bound);
  }

  @Override
  public Configuration unmarshal(ConfigurationValue value) throws Exception {
    return new Configuration(fileService.getById(value.getInput()), value.getFeedback());
  }
}
