package cz.cuni.mff.xrg.odalic.input;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

/**
 * Default implementation of the {@link CsvInputParser}.
 * 
 * @author Jan Váňa
 */
public final class DefaultCsvInputParser implements CsvInputParser {

  private final ListsBackedInputBuilder inputBuilder;

  @Autowired
  public DefaultCsvInputParser(ListsBackedInputBuilder inputBuilder) {
    Preconditions.checkNotNull(inputBuilder);
    
    this.inputBuilder = inputBuilder;
  }
  
  @Override
  public Input parse(String content, String identifier, CsvConfiguration configuration) throws IOException {
    try (Reader reader = new StringReader(content)) {
      return parse(reader, identifier, configuration);
    }
  }

  @Override
  public Input parse(InputStream stream, String identifier, CsvConfiguration configuration) throws IOException {
    try (Reader reader = new InputStreamReader(stream, configuration.getCharset())) {
      return parse(reader, identifier, configuration);
    }
  }

  @Override
  public Input parse(Reader reader, String identifier, CsvConfiguration configuration) throws IOException {
    final CSVFormat format = configuration.toApacheConfiguration();
    final CSVParser parser = format.parse(reader);

    inputBuilder.clear();
    inputBuilder.setFileIdentifier(identifier);
    handleHeaders(parser);

    int row = 0;
    for(CSVRecord record : parser) {
      handleInputRow(record, row);
      row++;
    }

    return inputBuilder.build();
  }

  private void handleInputRow(CSVRecord row, int rowIndex) {
    int column = 0;
    for (String value : row) {
      inputBuilder.insertCell(value, rowIndex, column);
      column++;
    }
  }

  private void handleHeaders(CSVParser parser) {
    final Map<String, Integer> headerMap = parser.getHeaderMap();

    for(Map.Entry<String, Integer> headerEntry : headerMap.entrySet()) {
      inputBuilder.insertHeader(headerEntry.getKey(), headerEntry.getValue());
    }
  }
}
