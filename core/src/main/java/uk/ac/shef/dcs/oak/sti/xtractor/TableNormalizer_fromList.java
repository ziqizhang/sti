package uk.ac.shef.dcs.oak.sti.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 31/03/14
 * Time: 15:43
 * To change this template use File | Settings | File Templates.
 */
public class TableNormalizer_fromList implements TableNormalizer {

    @Override
    public List<List<Node>> apply(Node listNode) {
        List<List<Node>> elements = new ArrayList<List<Node>>();

        List<Node> lis = DomUtils.findAllByTag(listNode, "LI");

        for (Node element : lis) {  //loop thru each row
            if (element.getTextContent().length() > 0) {  //if this row has content, go ahead
                List<Node> row = new ArrayList<Node>();
                row.add(element);
                elements.add(row);
                boolean hasTH = false;
            }
        }
        return elements;
    }
}
