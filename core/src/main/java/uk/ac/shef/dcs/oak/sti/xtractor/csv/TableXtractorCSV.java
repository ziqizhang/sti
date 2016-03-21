package uk.ac.shef.dcs.oak.sti.xtractor.csv;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.oak.sti.STIException;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.shef.dcs.oak.sti.experiment.TestTableInterpretation_CSV;
import uk.ac.shef.dcs.oak.sti.xtractor.Table_ContextExtractor_IMDB;

/**
 * TODO: Detect header?
 */
public class TableXtractorCSV {

    private static final Logger log = Logger.getLogger(TableXtractorCSV.class.getName());

    public TableXtractorCSV() {

    }

    public List<LTable> extract(File file, String sourceId) {
        List<LTable> rs = new ArrayList<LTable>();

        ParserCsvConfig config = new ParserCsvConfig();
        Parser csvParser = new ParserCsv(config);
        try {
            LTable tableParsed = csvParser.parse(file);
            rs.add(tableParsed);
        } catch (ParseFailed ex) {
            log.severe(ex.getLocalizedMessage());
        } catch (NoCSVDataException ex) {
            log.warning(ex.getLocalizedMessage());
        }

        //
        //        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId, "UTF-8");
        //        Document doc = null;
        //        try {
        //            doc = parser.getDOM();
        //        } catch (IOException e) {
        //            return rs;
        //        }
        //
        //        List<Node> tables = DomUtils.findAll(doc, "//TABLE[@class='cast_list']");
        //        List<LTableContext> contexts = new ArrayList<LTableContext>();
        //        try {
        //            contexts = Table_ContextExtractor_IMDB.extract_tripleContexts(sourceId, doc);
        //        } catch (STIException e) {
        //            e.printStackTrace();
        //        }
        //        int tableCount = 0;
        //        for (Node n : tables) {
        //            tableCount++;
        //
        //            LTableContext[] contexts_array = new LTableContext[contexts.size()];
        //            for (int i = 0; i < contexts.size(); i++)
        //                contexts_array[i] = contexts.get(i);
        //            LTable table = extractTable(n, String.valueOf(tableCount),
        //                    sourceId, contexts_array);
        //            if (table != null)
        //                rs.add(table);
        //
        //        }
        return rs;
    }

    private LTable extractTable(Node n, String valueOf, String sourceId, LTableContext[] contexts_array) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
