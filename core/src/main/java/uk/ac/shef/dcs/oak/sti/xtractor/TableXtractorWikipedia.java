package uk.ac.shef.dcs.oak.sti.xtractor;

import info.bliki.wiki.model.WikiModel;
import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableContext;
import uk.ac.shef.dcs.oak.sti.xtractor.validator.TableValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/10/12
 * Time: 12:08
 */
public class TableXtractorWikipedia extends TableXtractor {
    private WikiModel model;

    public TableXtractorWikipedia(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator,
                                  TableValidator... validators) {
        super(normalizer, detector, creator, validators);
        model = new WikiModel("/${image}", "/${title}");
    }


    @Override
    public List<LTable> extract(String input, String sourceId) {
        String html = model.render(input);
        List<LTable> rs = new ArrayList<LTable>();

        parser = new TagSoupParser(new ByteArrayInputStream(html.getBytes()), sourceId);
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        int tableCount = 0;
        List<Node> tables = DomUtils.findAll(doc, "//TABLE[@class='wikitable']"); //todo: change to allow fuzzy match "wikitableSortable"
        for (Node tableElement : tables) {
            tableCount++;

            //todo: extract contexts for table
            LTableContext[] contexts = new LTableContext[0];

            LTable table = extractTable(tableElement, String.valueOf(tableCount),
                    sourceId, contexts);
            if (table != null)
                rs.add(table);


        }
        return rs;
    }


}
