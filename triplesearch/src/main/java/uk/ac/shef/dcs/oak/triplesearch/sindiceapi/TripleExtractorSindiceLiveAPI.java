package uk.ac.shef.dcs.oak.triplesearch.sindiceapi;

import com.sindice.result.LiveResult;
import com.sindice.result.SearchResult;
import uk.ac.shef.dcs.oak.triplesearch.Triple;
import uk.ac.shef.dcs.oak.triplesearch.TripleSearchException;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 21/09/12
 * Time: 14:26
 * <p/>
 * extracts Triples using sindice live api. this gets the most up to date data and puts workload on sindice server
 */
public class TripleExtractorSindiceLiveAPI implements TripleExtractor {

    private int explicit = 1;
    private int implicit = 1;
    private SindiceAPIProxy sindice;

    public TripleExtractorSindiceLiveAPI(SindiceAPIProxy sindice, int explicit, int implicit) {
        this.sindice = sindice;
    }

//    @Override
    public Set<Triple> extract(SearchResult sindiceResultDocument) throws TripleSearchException {
        LiveResult lr = sindice.retrieveLiveTarget(sindiceResultDocument);
        Set<Triple> set = new HashSet<Triple>();
        if (explicit == 1) {
            for (String t : lr.getExplicitContent()) {
                String[] parts = t.trim().split("\\s+");
                assert parts.length == 3 : "Sindice extracts a triple that does not have s-p-o pattern:" + t;
                set.add(new Triple(parts[0], parts[1], parts[2]));
            }
        }
        if (implicit == 1) {
            for (String t : lr.getImplicitContent()) {
                String[] parts = t.trim().split("\\s+");
                assert parts.length == 3 : "Sindice extracts a triple that does not have s-p-o pattern:" + t;
                set.add(new Triple(parts[0], parts[1], parts[2]));
            }
        }

        return set;
    }
}
