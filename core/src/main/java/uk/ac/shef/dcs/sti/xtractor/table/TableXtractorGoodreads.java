package uk.ac.shef.dcs.sti.xtractor.table;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.xtractor.TableHODetector;
import uk.ac.shef.dcs.sti.xtractor.TableNormalizer;
import uk.ac.shef.dcs.sti.xtractor.TableObjCreator;
import uk.ac.shef.dcs.sti.xtractor.Table_ContextExtractor_Generic;
import uk.ac.shef.dcs.sti.xtractor.validator.TableValidator;

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
public class TableXtractorGoodreads extends TableXtractor {
    public TableXtractorGoodreads(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator, TableValidator... validators) {
        super(normalizer, detector, creator, validators);
    }

    @Override
    public List<Table> extract(String input, String sourceId) {
        List<Table> rs = new ArrayList<Table>();

        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId,"UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        List<Node> tables = DomUtils.findAll(doc, "//TABLE[@class='stacked tableList']");
        List<TContext> contexts = new ArrayList<TContext>();
        try {
            contexts = Table_ContextExtractor_Generic.extractTableContexts(sourceId, doc);
        } catch (STIException e) {
            e.printStackTrace();
        }
        int tableCount = 0;
        for (Node n : tables) {
            tableCount++;

            TContext[] contexts_array = new TContext[contexts.size()];
            for (int i = 0; i < contexts.size(); i++)
                contexts_array[i] = contexts.get(i);
            Table table = extractTable(n, String.valueOf(tableCount),
                    sourceId, contexts_array);
            if (table != null)
                rs.add(table);

        }
        return rs;
    }
}
