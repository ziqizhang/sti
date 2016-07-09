package uk.ac.shef.dcs.sti.parser.table;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorIMDB;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetector;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizer;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreator;
import uk.ac.shef.dcs.sti.parser.table.context.TableContextExtractorIMDB;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizerDiscardIrregularRows;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidatorGeneric;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.joox.JOOX.$;

/**
 * for parsing IMDB cast table on movie pages, e.g.,
 * http://www.imdb.com/title/tt0371746/
 *
 * see TableContextExtractorIMDB for the extraction of context for the table
 */
public class TableParserIMDB extends TableParser implements Browsable {

    public TableParserIMDB(){
        super(new TableNormalizerDiscardIrregularRows(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorIMDB(),
                new TableValidatorGeneric());
    }

    public TableParserIMDB(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator, TableValidator... validators) {
        super(normalizer, detector, creator, validators);
    }

    private Document createDocument(String inFile, String sourceId) throws STIException{
        String input;
        try {
            input = FileUtils.readFileToString(new File(inFile));
        } catch (IOException e) {
            throw new STIException(e);
        }
        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId,"UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
        }

        return doc;
    }

    @Override
    public List<Table> extract(String inFile, String sourceId) throws STIException {
        List<Table> rs = new ArrayList<>();
        Document doc = createDocument(inFile, sourceId);

        List<Node> tables = DomUtils.findAll(doc, "//TABLE[@class='cast_list']");
        List<TContext> contexts = new ArrayList<>();
        try {
            contexts = new TableContextExtractorIMDB().extract(new File(sourceId), doc);
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


    @Override
    public List<String> extract(String inFile, String sourceId, String outputFolder) throws STIException {
        Document doc = createDocument(inFile, sourceId);

        List<Node> tables = DomUtils.findAll(doc, "//TABLE[@class='cast_list']");
        int count=1;
        for(Node n: tables){
            String xpath = $(n).xpath();
            Node parent = n.getParentNode();
            if(parent!=null) {
                Element checkbox = doc.createElement("input");
                checkbox.setAttribute("type","checkbox");
                checkbox.setAttribute("name","table"+count);
                checkbox.setAttribute("checked","true");
                //todo: continue here
                parent.insertBefore(checkbox, n);
            }
        }

        return new ArrayList<>();
    }
}
