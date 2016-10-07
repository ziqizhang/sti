package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;

import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * CSV exporter.
 * 
 * @author Josef Janoušek
 *
 */
public interface CSVExporter {
  /**
   * Exports Input content to CSV String.
   * 
   * @param content Input content to export
   * @param configuration CSV configuration
   * @return CSV String
   * @throws IOException
   */
  String export(Input content, CsvConfiguration configuration)
      throws IOException;
}
