package uk.ac.shef.dcs.oak.triplesearch.sindiceapi;

import com.sindice.result.SearchResult;
import uk.ac.shef.dcs.oak.triplesearch.Triple;
import uk.ac.shef.dcs.oak.triplesearch.TripleSearchException;

import java.util.Set;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 21/09/12
 * Time: 14:22
 */
public interface TripleExtractor {
    Set<Triple> extract(SearchResult sindiceResultDocument) throws TripleSearchException;
}
