package uk.ac.shef.dcs.oak.sti.xtractor.csv;

import cern.colt.matrix.ObjectMatrix2D;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.any23.extractor.html.DomUtils;

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.AbstractCsvReader;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.QuoteMode;
import org.supercsv.util.CsvContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.shef.dcs.oak.sti.PlaceHolder;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableColumnHeader;
import uk.ac.shef.dcs.oak.sti.rep.LTableContentCell;

/**
 * Parse csv file.
 * //TODO specify delimiter automatically - now it has to be manually tuned. - check first row, try to detect - how many times there is ",", ";".
 * //not now, as it is ";"
 * //TODO detect automatically the header - so far we assume header is there
 * //not now, as there is always header
 * 
 * @author Škoda Petr
 */
public class ParserCsv implements Parser {

    private static final Logger log = LoggerFactory.getLogger(ParserCsv.class);

    private final ParserCsvConfig config;

    public ParserCsv(ParserCsvConfig config) {
        this.config = config;
    }

    @Override
    public LTable parse(File inFile) throws ParseFailed, NoCSVDataException {

        log.debug("Starting parsing file: " + inFile.getName());
        LTable resultingTable = null;

        final CsvPreference csvPreference;
        // We will use quates only if they are provided
        if (config.quoteChar == null || config.quoteChar.isEmpty()) {
            // We do not use quates.
            log.info("We do not use quotes.");
            final QuoteMode customQuoteMode = new QuoteMode() {
                @Override
                public boolean quotesRequired(String csvColumn, CsvContext context, CsvPreference preference) {
                    return false;
                }
            };
            // Quate char is never used.
            csvPreference = new CsvPreference.Builder(' ', config.delimiterChar.charAt(0),
                    "\\n").useQuoteMode(customQuoteMode).build();

        } else {
            csvPreference = new CsvPreference.Builder(
                    config.quoteChar.charAt(0),
                    config.delimiterChar.charAt(0),
                    "\\n").build();
        }

        //try to detect encoding

        //Charset decoder 1
        TestDetector td = new TestDetector();
        String encoding = td.detect(inFile);
        Charset charset = Charset.forName(encoding);
        // Charset charset = Charset.forName("ISO-8859-1");

        //Charset decoder 2
        //        String[] charsetsToBeTested = { "UTF-8", "ISO-8859-1", "ASCII" };
        //        CharsetDetector cd = new CharsetDetector();
        //        Charset charset = cd.detectCharset(inFile, charsetsToBeTested);

        if (charset != null) {
            log.info("Charset is: " + charset.toString());

        } else {
            log.error("Unrecognized charset.");
        }

        try {

            //get number of columns/rows

            FileInputStream fileInputStreamStat = new FileInputStream(inFile);

            InputStreamReader inputStreamReaderStat = getInputStream(fileInputStreamStat, charset);

            BufferedReader bufferedReaderStat = new BufferedReader(inputStreamReaderStat);

            CsvListReader csvStatsListReader = new CsvListReader(bufferedReaderStat, csvPreference);

            log.debug("Analyzing the CSV file");

            //get header and set cursor after header
            List<String> headerStat = null;
            int nColumns = 0;
            if (config.hasHeader) {
                headerStat = Arrays.asList(csvStatsListReader.getHeader(true));
                nColumns = headerStat.size();
            }

            //count number of rows, columns
            int nRows = 0;
            List<String> lines;
            while ((lines = csvStatsListReader.read()) != null) {
                nRows++;
                if (lines.size() != nColumns) {
                    log.warn("Number of columns of row " + nRows + " is different then in the header!");
                    nColumns = lines.size();
                }

            }

            log.info("Number of columns: " + nColumns);
            log.info("Number of rows: " + nRows);

            if (config.rowLimit < nRows) {
                nRows = config.rowLimit;
                log.info("Number of processed rows (limit): " + nRows);
            }

            FileInputStream fileInputStream = new FileInputStream(inFile);

            InputStreamReader inputStreamReader = getInputStream(fileInputStream, charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            CsvListReader csvListReader = new CsvListReader(bufferedReader, csvPreference);

            log.debug("Creating resulting table");
            //prepare the target table
            resultingTable = new LTable(String.valueOf(inFile.getName().hashCode()), inFile.getName(), nRows, nColumns);
            resultingTable.setEncoding(charset);

            //
            // ignore initial ? lines
            //
            for (int i = 0; i < config.numberOfStartLinesToIgnore; ++i) {
                bufferedReader.readLine();
            }
            //
            // get header
            //
            List<String> header;
            if (config.hasHeader) {

                header = Arrays.asList(csvListReader.getHeader(true));

                for (int c = 0; c < header.size(); c++) {
                    String headerCell = header.get(c);
                    //set headers
                    LTableColumnHeader h = new LTableColumnHeader(headerCell);
                    resultingTable.setColumnHeader(c, h);
                }

            } else {

                header = null;

                for (int c = 0; c < nColumns; c++) {
                    //set dummy headers TODO number of columns
                    LTableColumnHeader h = new LTableColumnHeader(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue());
                    resultingTable.setColumnHeader(c, h);
                }

            }

            //
            // read rows and parse
            //
            int rowNumPerFile = 0;
            List<String> row = csvListReader.read();
            if (row == null) {
                // no data
                log.warn("No data found!");
                throw new NoCSVDataException("No data");
            }

            // configure parser
            //            TableToRdfConfigurator.configure(tableToRdf, header, (List) row, 0);
            // initial checks

            //        if (header != null && header.size() != row.size()) {
            //            throw new ParseFailed("Diff number of cells in header ("
            //                    + header.size() + ") and data (" + data.size() + ")");
            //        }

            if (config.rowLimit == null) {
                log.debug("Row limit: not used");
            } else {
                log.debug("Row limit: {}", config.rowLimit);
            }
            while (row != null && (config.rowLimit == null || rowNumPerFile < config.rowLimit)) {

                log.debug("Row: " + row);
                addRow(resultingTable, (List) row, rowNumPerFile);
                // read next row
                //                rowNumber++;
                rowNumPerFile++;
                row = csvListReader.read();
                // log
                if ((rowNumPerFile % 1000) == 0) {
                    log.debug("Row number {} processed.", rowNumPerFile);
                }
            }

            //resultingTable.setNumRows(rowNumPerFile);

        } catch (IOException ex) {
            throw new ParseFailed("Parse of '" + inFile.toString() + "' failed", ex);
        } catch (SuperCsvException ex) {
            // here's what you're after!
            log.error(ex.getLocalizedMessage());
            CsvContext context = ex.getCsvContext();
            log.warn(String.format(
                    "Something went wrong on lineNo=%s, rowNo=%s, colNo=%s",
                    context.getLineNumber(),
                    context.getRowNumber(),
                    context.getColumnNumber()));
        }

        return resultingTable;

    }

    private void addRow(LTable resultingTable, List<Object> row, int rowNumber) {
        //
        //        //
        //        // trim string values
        //        //
        //        if (config.trimString) {
        //            List<Object> newRow = new ArrayList<>(row.size());
        //            for (Object item : row) {
        //                if (item instanceof String) {
        //                    final String itemAsString = (String) item;
        //                    newRow.add(itemAsString.trim());
        //                } else {
        //                    newRow.add(item);
        //                }
        //            }
        //            row = newRow;
        //        }

        int c = 0;
        for (Object item : row) {
            if (item instanceof String) {
                final String itemAsString = (String) item;
                LTableContentCell cell = new LTableContentCell(itemAsString);
                cell.setText(itemAsString);
                log.debug("About to store cell " + item + " under: " + rowNumber + " " + c);
                resultingTable.setContentCell(rowNumber, c, cell);
                c++;
            }
            else {
                log.debug("Cell item " + item + " is not String, set to empty String");
                LTableContentCell cell = new LTableContentCell("");
                resultingTable.setContentCell(rowNumber, c, cell);
                c++;
            }

        }

    }

    //    public LTable create(String tableId, String sourceId, LTableContext... contexts) {
    //        LTable table = new LTable(tableId, sourceId, preTable.rows() - 1, preTable.columns());
    //        //        for (LTableContext ctx : contexts)
    //        //            table.addContext(ctx);
    //
    //        //        //firstly add the header row
    //        //        for (int c = 0; c < preTable.columns(); c++) {
    //        //            Object o = preTable.get(0, c);
    //        //            if (o == null) { //a null value will be inserted by TableHODetector if no user defined header was found
    //        //                //todo: header column type
    //        //                LTableColumnHeader header = new LTableColumnHeader(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue());
    //        //                table.setColumnHeader(c, header);
    //        //
    //        //            } else {
    //        //                //todo: header column type
    //        //                Node e = (Node) o;
    //        //                String text = e.getTextContent();
    //        //                String xPath = DomUtils.getXPathForNode(e);
    //        //
    //        //                LTableColumnHeader header = new LTableColumnHeader(text);
    //        //                header.setHeaderXPath(xPath);
    //        //                table.setColumnHeader(c, header);
    //        //            }
    //        //        }
    //
    //        //then go thru each other rows
    //        for (int r = 1; r < preTable.rows(); r++) {
    //            for (int c = 0; c < preTable.columns(); c++) {
    //                //get url
    //                Node e = (Node) preTable.get(r, c);
    //                String text = "";
    //                String xPath = "";
    //                if (c == 0) {
    //                    NodeList nl = e.getChildNodes();
    //                    for (int i = 0; i < nl.getLength(); i++) {
    //                        Node an = nl.item(i);
    //                        if (an.getNodeName().equalsIgnoreCase("A")) {
    //                            String link = an.getAttributes().getNamedItem("href").getTextContent();
    //                            text = "http://www.imdb.com" + link;
    //                            xPath = DomUtils.getXPathForNode(an);
    //                            break;
    //                        }
    //                    }
    //                } else {
    //                    e = (Node) preTable.get(r, c);
    //                    text = e.getTextContent().trim();
    //                    xPath = DomUtils.getXPathForNode(e);
    //                }
    //
    //                LTableContentCell cell = new LTableContentCell(text);
    //                cell.setText(text);
    //
    //                table.setContentCell(r - 1, c, cell);
    //
    //                //handle the table row once
    //                if (c == 0 && xPath != null) {
    //                    String rowXPath = XPathUtils.trimXPathLastTag("TR", xPath);
    //                    table.getRowXPaths().put(r, rowXPath);
    //                }
    //            }
    //        }
    //
    //        if (table.getRowXPaths().size() > 0) {
    //            String rowXPath = table.getRowXPaths().get(0);
    //            if (rowXPath == null && table.getRowXPaths().size() > 1)
    //                rowXPath = table.getRowXPaths().get(1);
    //            if (rowXPath == null) {
    //            }
    //            //System.out.println();
    //            else {
    //                String tableXPath = XPathUtils.trimXPathLastTag("TABLE", rowXPath);
    //                table.setTableXPath(tableXPath);
    //            }
    //        }
    //
    //        return table;
    //    }

    /**
     * Create {@link InputStreamReader}. If "UTF-8" as encoding is given then {@link BOMInputStream} is used
     * as intermedian between given fileInputStream and output {@link InputStreamReader} to remove possible
     * BOM mark at the start of "UTF" files.
     * 
     * @param fileInputStream
     * @return
     * @throws UnsupportedEncodingException
     */
    private InputStreamReader getInputStream(FileInputStream fileInputStream, Charset charset) throws UnsupportedEncodingException {
        if (charset.toString().compareToIgnoreCase("UTF-8") == 0) {
            return new InputStreamReader(new BOMInputStream(fileInputStream, false), charset);
        } else {
            return new InputStreamReader(fileInputStream, charset);
        }
    }

    /**
     * Create {@link InputStreamReader}. If "UTF-8" as encoding is given then {@link BOMInputStream} is used
     * as intermedian between given fileInputStream and output {@link InputStreamReader} to remove possible
     * BOM mark at the start of "UTF" files.
     * 
     * @param fileInputStream
     * @return
     * @throws UnsupportedEncodingException
     */
    //    private InputStreamReader getInputStream(FileInputStream fileInputStream) throws UnsupportedEncodingException {
    //        if (config.encoding.compareToIgnoreCase("UTF-8") == 0) {
    //            return new InputStreamReader(new BOMInputStream(fileInputStream, false), config.encoding);
    //        } else {
    //            return new InputStreamReader(fileInputStream, config.encoding);
    //        }
    //    }

}
