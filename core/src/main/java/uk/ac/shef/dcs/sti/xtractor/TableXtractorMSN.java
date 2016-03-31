package uk.ac.shef.dcs.sti.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.PlaceHolder;
import uk.ac.shef.dcs.sti.rep.TCell;
import uk.ac.shef.dcs.sti.rep.TContext;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.TColumnHeader;
import uk.ac.shef.dcs.sti.xtractor.validator.TableValidator;

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
    public List<Table> extract(String input, String sourceId) {
        List<Table> rs = new ArrayList<Table>();
        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId, "UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        List<Node> actors = DomUtils.findAll(doc, "//DIV[@class='MovieFeaturedCast_NameLink']");

        List<TContext> contexts = new ArrayList<TContext>();
        //  try {
        List<Node> titles = DomUtils.findAll(doc, "//TITLE");
        for (Node n : titles) {
            String text = n.getTextContent().trim();
            if (text.length() < 1)
                continue;
            TContext ltc = new TContext(text,
                    TContext.TableContextType.PAGETITLE, 1.0);
            contexts.add(ltc);
        }

        TContext allhtmltext = new TContext(Jsoup.parse(input).text(), TContext.TableContextType.BEFORE, 0.5);
        /* } catch (LodieException e) {
                    e.printStackTrace();
                }
        */

        if (actors.size() > 0) {
            Table table = new Table(sourceId, sourceId, actors.size(), 1);
            table.setColumnHeader(0, new TColumnHeader(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue()));
            table.addContext(allhtmltext);
            for (TContext ltc : contexts)
                table.addContext(ltc);
            for (int i = 0; i < table.getNumRows(); i++) {
                Node n = actors.get(i);
                String text = n.getTextContent().trim();
                table.setContentCell(i, 0, new TCell(text));

            }
            rs.add(table);
        }
        return rs;
    }

}
