package uk.ac.shef.dcs.sti.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.xtractor.validator.TableValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/10/12
 * Time: 12:07
 */
public class TableXtractorHTML extends TableXtractor {


    public TableXtractorHTML(TableNormalizer normalizer, TableHODetector detector, TableObjCreator creator,TableValidator... validators) {
        super(normalizer, detector, creator,validators);
    }


    @Override
    public List<Table> extract(String input, String sourceId) {
        List<Table> rs = new ArrayList<Table>();
        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId,"UTF-8");
        Document doc = null; try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        List<Node> tables = DomUtils.findAll(doc, "//TABLE");

        int tableCount=0;
        for(Node n: tables){
            tableCount++;
            //todo: extract contexts for table
            TContext[] contexts = new TContext[0];
            Table table =extractTable(n, String.valueOf(tableCount),
                    sourceId,contexts);
            if(table!=null)
                rs.add(table);
        }
        return rs;
    }


}
