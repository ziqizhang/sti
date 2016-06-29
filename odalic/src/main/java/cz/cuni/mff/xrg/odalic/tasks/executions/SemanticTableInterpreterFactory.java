package cz.cuni.mff.xrg.odalic.tasks.executions;

import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;


public interface SemanticTableInterpreterFactory {

  SemanticTableInterpreter getInterpreter();

  void setIgnoreColumnsForInterpreter(Integer[] ignoreCols);

}
