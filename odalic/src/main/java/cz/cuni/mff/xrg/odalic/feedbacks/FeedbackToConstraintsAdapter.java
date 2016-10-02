package cz.cuni.mff.xrg.odalic.feedbacks;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;

/**
 * Converts {@link Feedback} to {@link Constraints}.
 * 
 * @author Václav Brodec
 *
 */
public interface FeedbackToConstraintsAdapter {
  /**
   * Converts {@link Feedback} instance to {@link Constraints}.
   * 
   * @param feedback user's feedback
   * @param base select knowledge base to constraint
   * @return algorithm constraints
   */
  Constraints toConstraints(Feedback feedback, KnowledgeBase base);
}
