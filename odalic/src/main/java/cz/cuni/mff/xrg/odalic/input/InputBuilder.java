package cz.cuni.mff.xrg.odalic.input;

import java.util.List;


/**
 * {@link Input} builder interface.
 * 
 * @author Václav Brodec
 *
 */
public interface InputBuilder {

  void insertCell(String value, int rowIndex, int columnIndex);

  void insertHeader(String value, int position);

  void insertToList(List<String> list, String value, int position);

  Input build();

  void clear();

}
