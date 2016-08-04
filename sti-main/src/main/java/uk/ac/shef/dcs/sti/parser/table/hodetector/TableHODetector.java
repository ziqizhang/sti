package uk.ac.shef.dcs.sti.parser.table.hodetector;

import cern.colt.matrix.ObjectMatrix2D;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/10/12
 * Time: 11:56
 *
 * detect table orientation and header. If table has vertical headers, transpose the table
 * to create a horizontal table; if the table has no headers, create false header row and add it
 * to the table elements.
 *
 * The algorithms for orientation and header are implemented
 * together in this class because usually they are dependent
 */
public interface TableHODetector {

    ObjectMatrix2D detect(List<List<Node>> elements);
}
