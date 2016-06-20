package cz.cuni.mff.xrg.odalic.tasks.configurations;

public interface ConfigurationService {

  void setConfigurationForTaskId(String id, Configuration execution);

  Configuration getConfigurationForTaskId(String id);

  void deleteConfigurationForTaskId(String id);
}
