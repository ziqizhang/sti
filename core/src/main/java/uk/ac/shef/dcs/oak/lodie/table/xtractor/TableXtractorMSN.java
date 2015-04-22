package uk.ac.shef.dcs.oak.lodie.table.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.oak.lodie.PlaceHolder;
import uk.ac.shef.dcs.oak.lodie.architecture.LodieException;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableColumnHeader;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContext;
import uk.ac.shef.dcs.oak.lodie.table.validator.TableValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 12/06/14
 * Time: 21:55
 * To change this template use File | Settings | File Templates.
 */
public class TableXtractorMSN extends TableXtractor {
    public TableXtractorMSN(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator, TableValidator... validators) {
        super(normalizer, detector, creator, validators);
    }

    @Override
    public List<LTable> extract(String input, String sourceId) {
        List<LTable> rs = new ArrayList<LTable>();
        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId, "UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        List<Node> actors = DomUtils.findAll(doc, "//DIV[@class='MovieFeaturedCast_NameLink']");

        List<LTableContext> contexts = new ArrayList<LTableContext>();
        //  try {
        List<Node> titles = DomUtils.findAll(doc, "//TITLE");
        for (Node n : titles) {
            String text = n.getTextContent().trim();
            if (text.length() < 1)
                continue;
            LTableContext ltc = new LTableContext(text,
                    LTableContext.TableContextType.PAGETITLE, 1.0);
            contexts.add(ltc);
        }

        LTableContext allhtmltext = new LTableContext(Jsoup.parse(input).text(), LTableContext.TableContextType.BEFORE, 0.5);
        /* } catch (LodieException e) {
                    e.printStackTrace();
                }
        */

        if (actors.size() > 0) {
            LTable table = new LTable(sourceId, sourceId, actors.size(), 1);
            table.setColumnHeader(0, new LTableColumnHeader(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue()));
            table.addContext(allhtmltext);
            for (LTableContext ltc : contexts)
                table.addContext(ltc);
            for (int i = 0; i < table.getNumRows(); i++) {
                Node n = actors.get(i);
                String text = n.getTextContent().trim();
                table.setContentCell(i, 0, new LTableContentCell(text));

            }
            rs.add(table);
        }
        return rs;
    }

}
