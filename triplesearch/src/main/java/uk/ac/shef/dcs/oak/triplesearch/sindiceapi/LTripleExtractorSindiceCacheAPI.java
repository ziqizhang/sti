//package uk.ac.shef.dcs.oak.triplesearch.sindiceapi;
//
//import com.sindice.Sindice;
//import com.sindice.query.FieldRegexFilter;
//import com.sindice.result.CacheResult;
//import com.sindice.result.SearchResult;
//import uk.ac.shef.dcs.oak.triplesearch.Triple;
//import uk.ac.shef.dcs.oak.triplesearch.TripleSearchException;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * @author annalisa
// * <p/>
// * extracts Triples from sidnice cache. NOTE there can be discrepancy between the index and cache! Documents in the index and
// * matching queries have a cached version on sindice. however, taht cached version is not frequently updated. you may not get
// * the triples you want from the cached document even if that document matches your "triple-based" query.
// * <p/>
// * However this is the fasted method
// */
//
//public class LTripleExtractorSindiceCacheAPI implements TripleExtractor {
//
//    private SindiceAPIProxy sindice;
//    private int explicit = 1;
//    private int implicit = 1;
//
//    public LTripleExtractorSindiceCacheAPI(SindiceAPIProxy sindice) {
//        this.sindice = sindice;
//    }
//
//    /**
//     * @param sindice
//     * @param explicit 1 if extracts explicit content (see sindice api. these are explicit triples); 0 otherwise
//     * @param implicit 1 if extracts implicit content (see sindice api. these are implicit triples); 0 otherwise
//     */
//    public LTripleExtractorSindiceCacheAPI(SindiceAPIProxy sindice, int explicit, int implicit) {
//        this.sindice = sindice;
//    }
//
//    public Set<LTriple> extract(SearchResult sindiceResultDocument) throws TripleSearchException {
//        CacheResult cr = sindice.retrieveCachedTarget(sindiceResultDocument);
//        Set<Triple> triples = new HashSet<Triple>();
//
//        if (explicit == 1) {
//            for (String t : cr.getExplicitContent()) {
//                String[] parts = t.trim().split("\\s+");
//                assert parts.length == 3 : "Sindice extracts a triple that does not have s-p-o pattern:" + t;
//                triples.add(new Triple(parts[0], parts[1], parts[2]));
//            }
//        }
//        if (implicit == 1) {
//            for (String t : cr.getImplicitContent()) {
//                String[] parts = t.trim().split("\\s+");
//                assert parts.length == 3 : "Sindice extracts a triple that does not have s-p-o pattern:" + t;
//                triples.add(new Triple(parts[0], parts[1], parts[2]));
//            }
//        }
//        return triples;
//    }
//}
