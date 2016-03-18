package uk.ac.shef.dcs.sti.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.shef.dcs.sti.PlaceHolder;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.rep.TColumnHeader;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.TContentCell;
import uk.ac.shef.dcs.sti.rep.LTableContext;
import uk.ac.shef.dcs.sti.xtractor.validator.TableValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 13/06/14
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public class TableXtractorRottenTomato extends TableXtractor {
    public TableXtractorRottenTomato(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator, TableValidator... validators) {
        super(normalizer, detector, creator, validators);
    }

    @Override
    public List<Table> extract(String input, String sourceId) {
        List<Table> rs = new ArrayList<Table>();
        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId, "UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        List<Node> tables = DomUtils.findAll(doc, "//DIV[@id='cast-info']");
        if (tables.size() > 0) {
            List<LTableContext> contexts = new ArrayList<LTableContext>();
            try {
                contexts = Table_ContextExtractor_Generic.extractTableContexts(sourceId, doc);
            } catch (STIException e) {
                e.printStackTrace();
            }
            int tableCount = 0;
            for (Node n : tables) {
                tableCount++;

                Node ul = null;
                NodeList list = n.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node nn = list.item(i);
                    if (nn.getNodeName().equals("UL")) {
                        ul = nn;
                        break;
                    }
                }

                if (ul == null)
                    continue;

                List<Node> items = DomUtils.findAll(ul, "LI");
                Table table = new Table(sourceId, sourceId, items.size(), 1);
                for (LTableContext ltc : contexts)
                    table.addContext(ltc);

                table.setColumnHeader(0, new TColumnHeader(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue()));
                int i=0;
                for(Node it: items){
                    String content="";
                    try{
                        content=DomUtils.findAll(it, "DIV/A").get(0).getTextContent();
                    }catch (NullPointerException npe){}

                    TContentCell ltc = new TContentCell(content);
                    table.setContentCell(i, 0, ltc);
                    i++;
                }

                if (table != null)
                    rs.add(table);

            }
        }
        return rs;
    }
}
