/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest.values.util;

import cz.cuni.mff.xrg.odalic.api.rest.values.StateValue;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

/**
 * Task states utility class.
 * 
 * @author Václav Brodec
 *
 */
public class States {

  private States() {}

  /**
   * Queries the execution service and derives the correct {@link StateValue}.
   * 
   * @param executionService task execution service
   * @param taskId task ID
   * @return state value
   */
  public static StateValue queryStateValue(ExecutionService executionService, String taskId) {
    final boolean scheduled = executionService.hasBeenScheduledForTaskId(taskId);
    if (!scheduled) {
      return StateValue.READY;
    }
    
    final boolean done = executionService.isDoneForTaskId(taskId);
    final boolean canceled = executionService.isCanceledForTaskId(taskId);
    
    if (done) {
      if (canceled) {
        return StateValue.READY;
      } else {
        if (executionService.hasFailedForTasksId(taskId)) {
          return StateValue.ERROR;
        }
        
        if (executionService.isWarnedForTasksId(taskId)) {
          return StateValue.WARNING;
        }
        
        return StateValue.SUCCESS;
      }
    } else {
      return StateValue.RUNNING;
    }
  }

}
