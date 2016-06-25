package cz.cuni.mff.xrg.odalic.feedbacks.input;

import uk.ac.shef.dcs.sti.core.model.Table;

public interface TableToInputAdapter {
  Input toInput(Table table);
}
