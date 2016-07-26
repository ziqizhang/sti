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
import uk.ac.shef.dcs.sti.parser.table.context.TableContextExtractorGeneric;
import uk.ac.shef.dcs.sti.parser.table.context.TableContextExtractorIMDB;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorHTML;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorIMDB;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorWikipedia;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetector;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizer;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreator;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizerDiscardIrregularRows;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidator;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidatorGeneric;

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
public class TableParserWikipedia extends TableParser implements Browsable{
    private WikiModel model;

    public TableParserWikipedia(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator,
                                TableValidator... validators) {
        super(normalizer, detector, creator, validators);
        model = new WikiModel("/${image}", "/${title}");
    }

    public TableParserWikipedia() {
        this(new TableNormalizerDiscardIrregularRows(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorWikipedia(false, false),
                new TableValidatorGeneric());
    }


    @Override
    public List<Table> extract(String inFile, String sourceId) throws STIException {
        //String html = model.render(input);
        List<Table> rs = new ArrayList<>();

        Document doc = createDocument(inFile, sourceId);

        List<TContext> contexts = new ArrayList<>();
        try {
            contexts= new TableContextExtractorGeneric().extract(new File(sourceId), doc);
        } catch (STIException e) {
            e.printStackTrace();
        }
        TContext[] contexts_array = new TContext[contexts.size()];
        for (int i = 0; i < contexts.size(); i++)
            contexts_array[i] = contexts.get(i);

        int tableCount = 0;
        List<Node> tables = DomUtils.findAll(doc, "//TABLE[@class='wikitable']");
        for (Node tableElement : tables) {
            tableCount++;

            Table table = extractTable(tableElement, String.valueOf(tableCount),
                    sourceId, contexts_array);
            if (table != null)
                rs.add(table);


        }
        return rs;
    }


    @Override
    public List<String> extract(String inFile, String sourceId, String outputFolder) throws STIException {
        Document doc = createDocument(inFile, sourceId);

        List<Node> tables = DomUtils.findAll(doc, "//TABLE[@class='wikitable']");
        List<String> xpaths = BrowsableHelper.createBrowsableElements(tables, doc);

        BrowsableHelper.output(inFile, outputFolder, doc);
        return xpaths;
    }
}
