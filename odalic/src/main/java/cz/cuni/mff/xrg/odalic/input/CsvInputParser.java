package cz.cuni.mff.xrg.odalic.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * CSV input parser.
 * 
 * @author Václav Brodec
 *
 */
public interface CsvInputParser {
  Input parse(String csvContent, String identifier, CsvConfiguration configuration)
      throws IOException;

  Input parse(Reader csvReader, String identifier, CsvConfiguration configuration)
      throws IOException;

  Input parse(InputStream csvStream, String identifier, CsvConfiguration configuration)
      throws IOException;
}
