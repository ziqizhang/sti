package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.concurrent.ExecutionException;

import cz.cuni.mff.xrg.odalic.tasks.results.Result;

public interface ExecutionService {
  void submitForTaskId(String id);
  
  void cancelForTaskId(String id);
  
  boolean isDoneForTaskId(String id);
  
  boolean isCancelledForTaskId(String id);
  
  Result getResultForTaskId(String id) throws InterruptedException, ExecutionException;

  boolean hasBeenScheduledForTaskId(String id);
}
