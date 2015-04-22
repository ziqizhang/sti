package uk.ac.shef.dcs.oak.lodie.table.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.oak.lodie.architecture.LodieException;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContext;
import uk.ac.shef.dcs.oak.lodie.table.validator.TableValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 31/03/14
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class TableXtractorReverbnation extends TableXtractor {
    public TableXtractorReverbnation(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator, TableValidator... validators) {
        super(normalizer, detector, creator, validators);
    }

    @Override
    public List<LTable> extract(String input, String sourceId) {
        List<LTable> rs = new ArrayList<LTable>();
        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId,"UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        List<Node> tables = DomUtils.findAll(doc, "//UL[@class='profile_songs_container']");
        List<LTableContext> contexts = new ArrayList<LTableContext>();
        try {
            contexts = Table_ContextExtractor_Generic.extractTableContexts(sourceId, doc);
        } catch (LodieException e) {
            e.printStackTrace();
        }
        int tableCount = 0;
        for (Node n : tables) {
            tableCount++;

            LTableContext[] contexts_array = new LTableContext[contexts.size()];
            for (int i = 0; i < contexts.size(); i++)
                contexts_array[i] = contexts.get(i);
            LTable table = extractTable(n, String.valueOf(tableCount),
                    sourceId, contexts_array);
            if (table != null)
                rs.add(table);

        }
        return rs;
    }
}
