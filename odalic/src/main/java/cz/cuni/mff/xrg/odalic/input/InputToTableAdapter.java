package cz.cuni.mff.xrg.odalic.input;

import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * Interface for {@link Input} to {@link Table} conversion.
 * 
 * @author Václav Brodec
 *
 */
public interface InputToTableAdapter {
  Table toTable(Input input);
}
