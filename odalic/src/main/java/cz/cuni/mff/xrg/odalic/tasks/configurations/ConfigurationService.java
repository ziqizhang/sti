package cz.cuni.mff.xrg.odalic.tasks.configurations;

/**
 * Configuration service handles the CRUD operations for {@link Configuration} instances.
 * 
 * @author Václav Brodec
 *
 */
public interface ConfigurationService {

  Configuration getForTaskId(String taskId);

  void setForTaskId(String taskId, Configuration execution);
}
