package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity;

import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchTripleIndexProxy;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/03/13
 * Time: 17:34
 * <p/>
 * need to:
 * firstly find all "subjects" that are "owl:classes"
 * secondly find all "subjects" that has predicate "typeof"
 * thirdly do set exclusion to find only "entities"
 */
public class EntityFinderSolr extends EntityFinder {

    private SolrServer server;

    public EntityFinderSolr(QueryCache cache, SolrServer server) {
        super(cache);
        this.server = server;
    }

    @Override
    /**
     * String[0] - entity
     * todo:MAYBE also add equivalent superclasses
     */
    protected List<ObjObj<String, Map<String, String>>> findCandidatesInKB(String text) {

        //firstly get everything that is "a type of" something
        SortedMap<String, List<String>> instances = SolrSearchTripleIndexProxy.searchInstancesByKeywords(text, server);
        //cache
        for (Map.Entry<String, List<String>> e : instances.entrySet()) {
            // System.out.println(count);
            String subject = e.getKey();
            List<String> classesOf = e.getValue();
            try {
                cache.cache(String.valueOf(
                        createCacheQueryId_findSuperClassesOf(subject).hashCode())
                        , classesOf, false);
            } catch (Exception e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        try {
            cache.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //next find everything that is "a type of" owl:Class, so we know that these are all classes.
        SortedMap<String, List<String>> classes = SolrSearchTripleIndexProxy.searchClassesByKeywords(text, server);
        //cache
        for (Map.Entry<String, List<String>> e : classes.entrySet()) {
            String subject = e.getKey();
            List<String> classesOf = e.getValue();
            try {
                cache.cache(
                        String.valueOf(createCacheQueryId_findClassesByKeyword(subject).hashCode())
                        , classesOf, false);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        try {
            cache.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> rs = new ArrayList<String>(instances.keySet());
        rs.removeAll(classes.keySet());
        List<ObjObj<String, Map<String, String>>> output =
                new ArrayList<ObjObj<String, Map<String, String>>>();
        for (String e : rs) {
            List<String> superClasses = instances.get(e);
            Map<String, String> superClassesAndEquiv = new HashMap<String, String>();
            for (String s : superClasses)
                superClassesAndEquiv.put(s, "");

            ObjObj<String, Map<String, String>> toAdd =
                    new ObjObj<String, Map<String, String>>();
            toAdd.setMainObject(e);
            toAdd.setOtherObject(superClassesAndEquiv);
            output.add(toAdd);
        }

        try {
            cache.cache(String.valueOf(createCacheQueryId_findEntitiesByKeyword(text).hashCode())
                    , output, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    @Override
    protected List<ObjObj<String, Map<String, String>>> findCandidatesInCache(String text) {
        String queryId = createCacheQueryId_findEntitiesByKeyword(text);
        try {
            return
                    (List<ObjObj<String, Map<String, String>>>)
                            cache.retrieve(String.valueOf(queryId.hashCode()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
   find all instances that are "typeof" "owl:class/rdfs:class"  ========= only for creating ids for caching purposes
    */
    protected static String createCacheQueryId_findClassesByKeyword(String keyword) {
        String q = "q="
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_SUBJECT_TEXT + ":" + keyword + " AND "
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_PREDICATE + ":" + SolrSearchTripleIndexProxy.URI_RDF_TYPE + " AND "
                + "(" + SolrSearchTripleIndexProxy.TRIPLE_INDEX_OBJECT + ":" + SolrSearchTripleIndexProxy.URI_OWL_CLASS + " OR "
                + SolrSearchTripleIndexProxy.URI_RDF_TYPE + ")" + "|"
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_SUBJECT;
        return String.valueOf(q.hashCode());
    }

    /*
    find all ENTTIIES that are "typeof" something  ==== only for creating ids for caching purposes
     */
    protected static String createCacheQueryId_findEntitiesByKeyword(String keyword) {
        String q = "q="
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_SUBJECT_TEXT + ":" + keyword + " AND "
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_PREDICATE + ":" + SolrSearchTripleIndexProxy.URI_RDF_TYPE + " AND NOT "
                + "(" + SolrSearchTripleIndexProxy.TRIPLE_INDEX_OBJECT + ":" + SolrSearchTripleIndexProxy.URI_OWL_CLASS + " OR "
                + SolrSearchTripleIndexProxy.URI_RDF_TYPE + ")" + "|"
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_SUBJECT;

        return String.valueOf(q.hashCode());
    }

    //only for creating ids for caching purposes
    public static String createCacheQueryId_findSuperClassesOf(String uri) {
        String q = "q="
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_SUBJECT + ":<" + uri + "> AND "
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_PREDICATE + ":<" + SolrSearchTripleIndexProxy.URI_RDF_TYPE + ">|"
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_OBJECT;
        return String.valueOf(q.hashCode());
    }
}
