package cz.cuni.mff.xrg.odalic.feedbacks.input;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.*;
import java.util.Map;

public class DefaultCsvInputParser implements InputParser {

  @Override
  public Input parse(String content, String fileIdentifier) throws IOException {
    CsvConfiguration defaultConfiguration = new CsvConfiguration();

    try (Reader reader = new StringReader(content)) {
      return parse(reader, defaultConfiguration, fileIdentifier);
    }
  }

  public Input parse(InputStream stream, CsvConfiguration configuration, String fileIdentifier) throws IOException {
    try (Reader reader = new InputStreamReader(stream, configuration.getEncoding())) {
      return parse(reader, configuration, fileIdentifier);
    }
  }

  public Input parse(Reader reader, CsvConfiguration configuration, String fileIdentifier) throws IOException {
    CSVFormat format = configuration.toApacheConfiguration();
    CSVParser parser = format.parse(reader);

    SimpleInput result = new SimpleInput(fileIdentifier);
    HandleHeaders(result, parser);

    int row = 0;

    for(CSVRecord record : parser) {
      HandleInputRow(result, record, row);
      row++;
    }

    return  result;
  }

  private void HandleInputRow(SimpleInput input, CSVRecord row, int rowIndex) {
    int column = 0;

    for (String value : row) {
      input.insertCell(value, rowIndex, column);
      column++;
    }
  }

  private void HandleHeaders(SimpleInput input, CSVParser parser) {
    Map<String, Integer> headerMap = parser.getHeaderMap();

    for(Map.Entry<String, Integer> headerEntry : headerMap.entrySet()) {
      input.insertHeader(headerEntry.getKey(), headerEntry.getValue());
    }
  }
}
