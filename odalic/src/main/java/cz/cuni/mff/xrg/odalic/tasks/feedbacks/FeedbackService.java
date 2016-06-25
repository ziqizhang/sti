package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import java.io.IOException;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.feedbacks.input.Input;

public interface FeedbackService {

  Feedback getForTaskId(String taskId);

  void setForTaskId(String taskId, Feedback feedback);

  Input getInputForTaskId(String id) throws IOException;
}
