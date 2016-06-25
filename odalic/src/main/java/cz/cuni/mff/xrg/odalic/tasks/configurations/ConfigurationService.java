package cz.cuni.mff.xrg.odalic.tasks.configurations;

public interface ConfigurationService {

  Configuration getForTaskId(String taskId);

  void setForTaskId(String taskId, Configuration execution);
}
