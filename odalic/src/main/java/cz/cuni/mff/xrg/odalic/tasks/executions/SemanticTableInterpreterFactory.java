package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.IOException;
import java.util.Map;

import uk.ac.shef.dcs.sti.STIException;
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
   * @throws IOException when the initialization process fails to load its configuration 
   * @throws STIException when the interpreters fail to initialize
   */
  Map<String, SemanticTableInterpreter> getInterpreters() throws STIException, IOException;
}
