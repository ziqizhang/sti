package uk.ac.shef.dcs.sti.xtractor.csv;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: Detect header?
 */
public class TableXtractorCSV {

    private static final Logger log = Logger.getLogger(TableXtractorCSV.class.getName());

    public TableXtractorCSV() {

    }

    public List<Table> extract(File file, String sourceId) {
        List<Table> rs = new ArrayList<Table>();

        ParserCsvConfig config = new ParserCsvConfig();
        Parser csvParser = new ParserCsv(config);
        try {
            Table tableParsed = csvParser.parse(file);
            rs.add(tableParsed);
        } catch (ParseFailed ex) {
            log.severe(ex.getLocalizedMessage());
        } catch (NoCSVDataException ex) {
            log.warning(ex.getLocalizedMessage());
        }

        return rs;
    }

    private Table extractTable(Node n, String valueOf, String sourceId, TContext[] contexts_array) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
