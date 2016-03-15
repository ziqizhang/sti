package uk.ac.shef.dcs.sti.xtractor;

import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.rep.LListItem;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 14:49
 */
public interface ListElementTokenizer {

    LListItem tokenize(Node liElementJSoup);
}
