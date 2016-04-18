package uk.ac.shef.dcs.sti.parser.table.normalizer;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * transforms a Node that embeds a list structure to a representation of table structure
 */
public class TableNormalizerListTransformer implements TableNormalizer {

    @Override
    public List<List<Node>> normalize(Node listNode) {
        List<List<Node>> elements = new ArrayList<>();

        List<Node> lis = DomUtils.findAllByTag(listNode, "LI");

        for (Node element : lis) {  //loop thru each row
            if (element.getTextContent().length() > 0) {  //if this row has content, go ahead
                List<Node> row = new ArrayList<>();
                row.add(element);
                elements.add(row);
                boolean hasTH = false;
            }
        }
        return elements;
    }
}
