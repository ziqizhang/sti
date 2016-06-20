package cz.cuni.mff.xrg.odalic.tasks.executions;

public interface ExecutionService {
  void scheduleExecutionForTaskId(String id, Execution execution);
  
  Execution getExecutionForTaskId(String id);

  void stopExecutionForTaskId(String id);
}
