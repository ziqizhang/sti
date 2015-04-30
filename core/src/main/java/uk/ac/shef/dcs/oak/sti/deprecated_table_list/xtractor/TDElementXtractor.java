package uk.ac.shef.dcs.oak.sti.deprecated_table_list.xtractor;

import org.w3c.dom.Node;
import uk.ac.shef.dcs.oak.sti.table.rep.LTableContentCell;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 14:34
 *
 * Given a Jsoup Element in a table, tokenize it and creates individual text values and assign xpaths. (a single table cell
 * can contain multiple values, links)
 */
@Deprecated
public interface TDElementXtractor {

    LTableContentCell extract(Node tableCell, int r, int c);
}
