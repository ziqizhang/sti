package uk.ac.shef.dcs.sti.parser.list.splitter;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.core.model.ListItem;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 14:50
 */
public class ListItemSplitterByURL implements ListItemSplitter {
    @Override
    public ListItem tokenize(Node liElement) {
        String fulltext = liElement.getTextContent();
        ListItem li = new ListItem(fulltext);

        List<Node> it = DomUtils.findAllByTag(liElement, "A");
        for (Node n : it) {

            if (n.getParentNode().getNodeName().equalsIgnoreCase("sub") || n.getParentNode().getNodeName().equalsIgnoreCase("sup"))
                continue;
            String uri = n.getAttributes().getNamedItem("href").getTextContent();
            String text = n.getTextContent();
            if (text.length() == 0)
                continue;
            li.getValuesAndURIs().put(text, uri);
        }

        if (fulltext.length() > 0 || li.getValuesAndURIs().size() > 0)
            return li;
        return null;
    }
}
