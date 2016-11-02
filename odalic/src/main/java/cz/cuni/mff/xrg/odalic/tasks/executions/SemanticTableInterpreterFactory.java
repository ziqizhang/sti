package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.Map;

import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;


/**
 * This factory class loosely encapsulates the process of interpreter creation.
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
}
