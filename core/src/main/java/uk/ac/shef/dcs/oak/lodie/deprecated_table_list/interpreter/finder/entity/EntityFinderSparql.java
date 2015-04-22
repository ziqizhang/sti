package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;
import uk.ac.shef.dcs.oak.triplesearch.util.StopLists;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/03/13
 * Time: 15:56
 */
public class EntityFinderSparql extends EntityFinder {

    protected String[] sparqlEndPoints;

    public EntityFinderSparql(QueryCache cache, String... sparqlEndPoints) {
        super(cache);
        this.sparqlEndPoints = sparqlEndPoints;
    }

    @Override
    //Currently we do not find equivalent entities
    protected List<ObjObj<String, Map<String, String>>> findCandidatesInKB(String keyword) {
        List<String> keywords = getNGram(keyword);

        List<String> cands = new ArrayList<String>();
        Map<String, List<String>> superclasses = new HashMap<String, List<String>>();
        Map<String, String> classEquiv = new HashMap<String, String>();

        for (String key : keywords) {
            List<String> sparqls = CommonSPARQLQueries.
                    createQueryBatchFindEntityWithKeyword_and_Superclasses_and_Equiv(key);
            for (String endpoint : sparqlEndPoints) {
                for (String query : sparqls) {
                    //System.out.println(query);
                    ResultSet rs = SPARQLQueryAgent.searchSPARQL(query, endpoint);
                    // try {
                    while (rs.hasNext()) {
                        QuerySolution qs = rs.next();
                        if (qs.get("?s").asNode() instanceof Node_Blank)
                            continue;
                        String subject = qs.get("?s").toString();
                        //Object o = qs.get("?o");
                        /*if(o==null)
                        System.out.println();*/
                        String object = qs.get("?o").toString();
                        boolean notEntity = false;
                        for (String c : CommonSPARQLQueries.URI_FOR_CLASS) {
                            if (object.equals(c)) {
                                notEntity = true;
                                break;
                            }
                        }
                        if (notEntity)
                            continue; //todo: this is wrong. if not entity, shoud not add any results in QS

                        if (!cands.contains(subject)) {
                            //System.out.println(/*qs.get("?s").asNode().getBlankNodeLabel()+*/","+qs.get("?s").toString());
                            cands.add(subject);
                        }

                        List<String> superclassesOfSub = superclasses.get(subject);
                        superclassesOfSub = superclassesOfSub == null ? new ArrayList<String>() : superclassesOfSub;
                        if (!superclassesOfSub.contains(object) &&
                                !StopLists.isMeaninglessClass(object))
                            superclassesOfSub.add(object);
                        superclasses.put(subject, superclassesOfSub);

                        RDFNode equiv = qs.get("?e");
                        if (equiv != null)
                            classEquiv.put(object, equiv.toString());

                    }
                }
            }
            /*
            not exhaustively try all n-gram combinations of keywords. as long as one matches, stop
             */
            if (cands.size() > 0)
                break;
        }


        List<ObjObj<String, Map<String, String>>> finalOutput
                = new ArrayList<ObjObj<String, Map<String, String>>>();
        for (String candidate : cands) {
            ObjObj<String, Map<String, String>> entry = new ObjObj<String, Map<String, String>>();
            entry.setMainObject(candidate);

            List<String> sClasses = superclasses.get(candidate);
            if (sClasses == null || sClasses.size() == 0) {
                entry.setOtherObject(new HashMap<String, String>());
                finalOutput.add(entry);
                continue;
            }

            Map<String, String> equivs = new HashMap<String, String>();
            for (String sp : sClasses) {
                String eq = classEquiv.get(sp);
                eq = eq == null ? "" : eq;
                equivs.put(sp, eq);
            }
            entry.setOtherObject(equivs);
            finalOutput.add(entry);

        }

        try {
            cache.cache(String.valueOf(CommonSPARQLQueries.createQueryFindEntityWithKeyword(keyword).hashCode()),
                    finalOutput, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //also cache superclasses of subjects
            for (Map.Entry<String, List<String>> e : superclasses.entrySet()) {
                List<String[]> superAndEquiv = new ArrayList<String[]>();
                for(String s : e.getValue()){
                    String eq = classEquiv.get(s);
                    eq=eq==null?"":eq;
                    superAndEquiv.add(new String[]{s, eq});
                }
                cache.cache(
                        String.valueOf(createCacheQueryId_findSuperclassesAndEquivOfURI(e.getKey()).hashCode())
                        , superAndEquiv, false);
            }
            cache.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return finalOutput;
    }

    @Override
    protected List<ObjObj<String, Map<String, String>>> findCandidatesInCache(String keyword) {
        String sparql = CommonSPARQLQueries.createQueryFindEntityWithKeyword(keyword);
        String idInCache = String.valueOf(sparql.hashCode());
        try {
            return (List<ObjObj<String, Map<String, String>>>) cache.retrieve(idInCache);
            //return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String createCacheQueryId_findSuperclassesAndEquivOfURI(String uri) {
        String sparql = "SELECT DISTINCT ?o ?e WHERE{\n<" + uri + "> a ?o \n" +
                "OPTIONAL{?o <http://www.w3.org/2002/07/owl#equivalentClass> ?e}.}";
        return sparql;
    }


}
