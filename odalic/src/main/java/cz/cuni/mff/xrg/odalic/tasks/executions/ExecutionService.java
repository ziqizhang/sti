package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Manages the {@link Task} execution.
 * 
 * @author Václav Brodec
 *
 */
public interface ExecutionService {
  /**
   * Submits execution of the task.
   * 
   * @param id task ID
   * @throws IllegalStateException if the task has already been submitted for execution
   */
  void submitForTaskId(String id) throws IllegalStateException;
  
  /**
   * Attempts to cancel execution of the task.
   * 
   * @param id task ID
   * @param IllegalStateException if the task has already finished
   */
  void cancelForTaskId(String id) throws IllegalStateException;
  
  /**
   * Indicates whether the task is done.
   * 
   * @param id task ID
   * @return true if done, false otherwise
   * 
   */
  boolean isDoneForTaskId(String id);
  
  /**
   * Indicates whether the task was voluntarily canceled.
   * 
   * @param id task ID
   * @return true if canceled, false otherwise
   */
  boolean isCanceledForTaskId(String id);
  
  /**
   * Gets result of the task. Blocks until the result is available or the execution canceled.
   * 
   * @param id task ID
   * @return annotations for the task input
   * @throws IllegalArgumentException when the task has not been scheduled
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException  if the computation was cancelled
   */
  Result getResultForTaskId(String id) throws IllegalArgumentException, InterruptedException, ExecutionException, CancellationException;

  /**
   * Indicates the state of scheduling.
   * 
   * @param id task ID
   * @return true, if the execution has been scheduled for the task, false otherwise
   */
  boolean hasBeenScheduledForTaskId(String id);
  
  /**
   * Indicates whether the task has finished without a problem.
   * 
   * @param id task ID
   * @return true if successful, false otherwise
   */
  boolean isSuccessForTasksId(String id);
  
  /**
   * Indicates whether the task has finished with a non-fatal problem.
   * 
   * @param id task ID
   * @return true if warned, false otherwise
   */
  boolean isWarnedForTasksId(String id);
  
  /**
   * Indicates whether the task has failed.
   * 
   * @param id task ID
   * @return true if failed, false otherwise
   */
  boolean hasFailedForTasksId(String id);
}
