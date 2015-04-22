package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.hierarchy;

import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchSchemaIndexProxy;
import uk.ac.shef.dcs.oak.util.StringUtils;

import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 30/04/13
 * Time: 18:11
 */
public class ConceptHierarchyHelper {

    private SolrServer schemaClassServer;
    private HierarchyGenerator generator;

    public ConceptHierarchyHelper(HierarchyGenerator generator) {
        this.generator=generator;
    }

    public ConceptHierarchyHelper(SolrServer server,HierarchyGenerator generator) {
        this.schemaClassServer = server;
        this.generator=generator;
    }

    public Map<String, Integer> generateHierarchies(Collection<String> conceptURIs) {
        List<String> sortedURIs = new ArrayList<String>(conceptURIs);
        Collections.sort(sortedURIs);
        Map<String, Set<String>> candidates = new HashMap<String, Set<String>>();

        String prevURI = null;
        for (String currentURI : conceptURIs) {
            if (prevURI != null) {
                String ns = StringUtils.findPossibleNameSpace(currentURI);
                Set<String> cands = candidates.get(ns);
                if (cands == null) {
                    cands = new HashSet<String>();
                }
                cands.add(currentURI);
                candidates.put(ns, cands);
            }
        }

        Map<String, Integer> distances = calcuateDistanceToRoot(candidates);

        return distances;
    }

    private Map<String, Integer> calcuateDistanceToRoot(Map<String, Set<String>> candidates) {
        Map<String, Integer> rs = new HashMap<String, Integer>();
        for (Map.Entry<String, Set<String>> e : candidates.entrySet()) {
            Set<String> cand = e.getValue();
            Set<String> notIndexed = new HashSet<String>();

            if (schemaClassServer != null) {
                for (String concept : cand) {
                    int depth=SolrSearchSchemaIndexProxy.searchClassDepth(concept, schemaClassServer);
                    if(depth==-1)
                        notIndexed.add(concept);
                    else
                        rs.put(concept,depth);
                }
            }else{
                notIndexed.addAll(cand);
            }

            //resolve the rest
            List<Path> paths =generator.generatePaths(notIndexed);
            for(Path p: paths){
                for(int i=0; i<p.getSteps().size(); i++){
                    int depth = p.getSteps().size()-1-i;
                    Step s = p.getSteps().get(i);
                    for(String a : s)
                        rs.put(a, depth);
                }
            }
        }

        return rs;
    }
}
