package uk.ac.shef.dcs.sti.parser.list.splitter;

import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.core.model.ListItem;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 14:49
 */
public interface ListItemSplitter {

    ListItem tokenize(Node liElementJSoup);
}
