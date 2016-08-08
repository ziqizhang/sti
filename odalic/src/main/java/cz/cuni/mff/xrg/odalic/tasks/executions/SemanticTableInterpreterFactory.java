package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.Map;
import java.util.Set;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;


/**
 * This factory class loosely encapsulates (notice the {@link #setColumnIgnoresForInterpreter(Set)} method) the process of interpreter creation.
 * 
 * @author Václav Brodec
 *
 */
public interface SemanticTableInterpreterFactory {

  /**
   * Lazily initializes the interpreter.
   * 
   * @return the interpreter implementations
   */
  Map<String, SemanticTableInterpreter> getInterpreters();

  /**
   * Sets the ignored columns for the created interpreter.
   * 
   * @param columnIgnores positions of ignored columns
   */
  void setColumnIgnoresForInterpreter(Set<? extends ColumnIgnore> columnIgnores);

}
