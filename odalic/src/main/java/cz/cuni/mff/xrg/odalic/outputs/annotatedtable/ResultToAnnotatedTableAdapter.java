package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

public interface ResultToAnnotatedTableAdapter {
  
  AnnotatedTable toAnnotatedTable(Result result, Input input, Configuration configuration);
}
