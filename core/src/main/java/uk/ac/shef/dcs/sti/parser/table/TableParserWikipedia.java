package uk.ac.shef.dcs.sti.parser.table;

import info.bliki.wiki.model.WikiModel;
import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetector;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizer;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreator;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/10/12
 * Time: 12:08
 */
public class TableParserWikipedia extends TableParser {
    private WikiModel model;

    public TableParserWikipedia(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator,
                                TableValidator... validators) {
        super(normalizer, detector, creator, validators);
        model = new WikiModel("/${image}", "/${title}");
    }


    @Override
    public List<Table> extract(String inFile, String sourceId) throws STIException, IOException {
        String input;
        try {
            input = FileUtils.readFileToString(new File(inFile));
        } catch (IOException e) {
            throw new STIException(e);
        }

        String html = model.render(input);
        List<Table> rs = new ArrayList<>();

        parser = new TagSoupParser(new ByteArrayInputStream(html.getBytes()), sourceId);
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        int tableCount = 0;
        List<Node> tables = DomUtils.findAll(doc, "//TABLE[@class='wikitable']");
        for (Node tableElement : tables) {
            tableCount++;

            //todo: extract contexts for wikitable
            TContext[] contexts = new TContext[0];

            Table table = extractTable(tableElement, String.valueOf(tableCount),
                    sourceId, contexts);
            if (table != null)
                rs.add(table);


        }
        return rs;
    }


}
