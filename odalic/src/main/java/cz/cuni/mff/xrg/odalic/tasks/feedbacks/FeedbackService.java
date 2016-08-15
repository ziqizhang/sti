package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import java.io.IOException;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * Feedback service handles the CRUD operations for {@link Feedback} instances.
 * 
 * @author Václav Brodec
 *
 */
public interface FeedbackService {

  Feedback getForTaskId(String taskId);

  void setForTaskId(String taskId, Feedback feedback);

  /**
   * Get the task input to base the feedback on.
   * 
   * @param id task ID
   * @return input used as the basis of feedback
   * @throws IOException when I/O exception occurs when reading underlying data
   */
  Input getInputForTaskId(String id) throws IOException;
}
