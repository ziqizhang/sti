package uk.ac.shef.dcs.oak.lodie.table.interpreter.content;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.lodie.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.triplesearch.freebase.EntityCandidate_FreebaseTopic;
import uk.ac.shef.dcs.oak.triplesearch.freebase.FreebaseQueryHelper;
import uk.ac.shef.dcs.oak.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 */
public class KBSearcher_Freebase extends KBSearcher {

    private boolean commit;
    private FreebaseQueryHelper searcher;
    private static Logger log = Logger.getLogger(KBSearcher_Freebase.class.getName());

    public KBSearcher_Freebase(String freebase_properties, String solrHomePath, boolean split_at_conjunection) throws IOException {
        super(solrHomePath, "collection1", split_at_conjunection);
        searcher = new FreebaseQueryHelper(freebase_properties);
        if (TableMinerConstants.COMMIT_SOLR_PER_FILE)
            commit = false;
        else
            commit = true;
    }

    public KBSearcher_Freebase(String freebase_properties, SolrServer server, boolean split_at_conjunection) throws IOException {
        super(server, split_at_conjunection);
        searcher = new FreebaseQueryHelper(freebase_properties);
        if (TableMinerConstants.COMMIT_SOLR_PER_FILE)
            commit = false;
        else
            commit = true;
    }


    @Override
    public List<EntityCandidate> find_matchingEntitiesForCell(LTableContentCell tcc) throws IOException {
        String query = createQuery_findEntities(tcc);
        return find_matchingEntitiesForText_clientFilterTypes(query);
    }

    @Override
    public List<EntityCandidate> find_matchingEntities_with_type_forCell(LTableContentCell tcc, String... types) throws IOException {
        String query = createQuery_findEntities(tcc);
        return find_matchingEntitiesForText_clientFilterTypes(query, types);
    }

    protected List<EntityCandidate> find_matchingEntitiesForText_clientFilterTypes(String text, String... types) throws IOException {
        boolean forceQuery = false;
        /*if (text.contains("Follow the")||text.contains("Wind"))
            //forceQuery = true;
            System.out.println();*/

        //rebuild due to invalid type?

        String original = text;

        text = StringEscapeUtils.unescapeXml(text);
        int bracket = text.indexOf("(");
        if (bracket != -1) {
            text = text.substring(0, bracket).trim();
        }
        List<String> query_tokens = StringUtils.toAlphaNumericTokens(text, true);
        if (query_tokens.size() == 0)
            return new ArrayList<EntityCandidate>();


        /*try {
            if (!original.equals(text)) {
                List<EntityCandidate> result = (List<EntityCandidate>) solrCache.retrieve(toSolrKey(text));
                if (result != null) {

                    for (EntityCandidate ec : result) {
                        for (String typeid : ec.getTypeIds()) {
                            if (typeid.startsWith("/m/")) {
                                forceQuery = true;
                                break;
                            }
                        }
                        if (forceQuery)
                            break;
                    }
                }
            }
        } catch (Exception e) {
        }
*/
        if (TableMinerConstants.FORCE_SEARCHAPI_QUERY)
            forceQuery = true;

        List<EntityCandidate> result = null;
        if (!forceQuery) {
            try {
                result = (List<EntityCandidate>) solrCache.retrieve(toSolrKey(text));
                if (result != null)
                    log.warning("QUERY (cache load)=" + toSolrKey(text) + "|" + text);
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = new ArrayList<EntityCandidate>();
            //List<EntityCandidate_FreebaseTopic> topics = searcher.searchapi_topics_with_name_and_type(text, "any",true,15,types);
            List<EntityCandidate_FreebaseTopic> topics = searcher.searchapi_topics_with_name_and_type(text, "any", true, 20); //search api does not retrieve complete types, find types for them
            for (EntityCandidate_FreebaseTopic ec : topics) {
                //find_triplesForEntityId()
                //instantiate facts and types
                List<String[]> facts = find_triplesForEntity(ec);
                ec.setFacts(facts);
                for (String[] f : facts) {
                    if (f[0].equals("/type/object/type") &&
                            f[3].equals("n") &&
                            !ec.hasTypeId(f[2]))
                        ec.addType(new String[]{f[2], f[1]});
                }
            }

            Iterator<EntityCandidate_FreebaseTopic> it = topics.iterator();
            while (it.hasNext()) {
                EntityCandidate_FreebaseTopic ec = it.next();
                List<String> cell_text_tokens = StringUtils.toAlphaNumericTokens(ec.getName(), true);
                int size = cell_text_tokens.size();
                cell_text_tokens.removeAll(query_tokens);
                if (cell_text_tokens.size() == size)
                    it.remove(); //the entity name does not contain any query word. remove it
            }

            if (topics.size() == 0 && split_at_conjunction) { //does the query has conjunection? if so, we may need to try again with split queries
                String[] queries = text.split("\\band\\b");
                if (queries.length < 2) {
                    queries = text.split("\\bor\\b");
                    if (queries.length < 2) {
                        queries = text.split("/");
                        if (queries.length < 2) {
                            queries = text.split(",");
                        }
                    }
                }
                if (queries.length > 1) {
                    for (String q : queries) {
                        q = q.trim();
                        if (q.length() < 1) continue;
                        result.addAll(find_matchingEntitiesForText_clientFilterTypes(q, types));
                    }
                }
            }

            result.addAll(topics);
            try {
                solrCache.cache(toSolrKey(text), result, commit);
                /* debug_helper_method(result);
                for(EntityCandidate ec: result){
                    if(ec.getFacts()!=null&&ec.getFacts().size()>0)
                        debug_fact_writer(ec.getId(),ec.getFacts());
                }*/
                log.warning("QUERY (cache save)=" + toSolrKey(text) + "|" + text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int beforeFiltering = result.size();
        if (types.length > 0) {
            Iterator<EntityCandidate> it = result.iterator();
            while (it.hasNext()) {
                EntityCandidate ec = it.next();
                boolean typeSatisfied = false;
                for (String t : types) {
                    if (ec.hasTypeId(t)) {
                        typeSatisfied = true;
                        break;
                    }
                }
                if (!typeSatisfied)
                    it.remove();
            }
        }

        String id = "|";
        for (EntityCandidate ec : result) {
            id = id + ec.getId() + ",";
            Iterator<String[]> it = ec.getTypes().iterator();
            while (it.hasNext()) {
                String[] s = it.next();
                if (KB_InstanceFilter.ignoreType(s[0], s[1]))
                    it.remove();
            }
        }

        System.out.println("(QUERY_KB:" + beforeFiltering + " => " + result.size() + id);
        return result;
    }


    protected List<EntityCandidate> find_matchingEntitiesForText_serverFilterTypes(String text, String... types) throws IOException {
        boolean forceQuery = false;
        /*if (text.contains("Nirmal Kumar Sidhanta"))
            forceQuery = true;*/
        if (TableMinerConstants.FORCE_SEARCHAPI_QUERY)
            forceQuery = true;

        text = StringEscapeUtils.unescapeXml(text);
        String cacheQuery = text + Arrays.asList(types).toString();
        int bracket = text.indexOf("(");
        if (bracket != -1) {
            text = text.substring(0, bracket).trim();
        }
        List<String> query_tokens = StringUtils.toAlphaNumericTokens(text, true);
        if (query_tokens.size() == 0)
            return new ArrayList<EntityCandidate>();

        List<EntityCandidate> result = null;

        if (!forceQuery) {
            try {
                result = (List<EntityCandidate>) solrCache.retrieve(toSolrKey(cacheQuery));
                if (types.length > 0) {
                    Iterator<EntityCandidate> it = result.iterator();
                    while (it.hasNext()) {
                        EntityCandidate ec = it.next();
                        boolean typeSatisfied = false;
                        for (String t : types) {
                            if (ec.hasTypeId(t)) {
                                typeSatisfied = true;
                                break;
                            }
                        }
                        if (!typeSatisfied)
                            it.remove();
                    }
                }
                if (result != null)
                    log.warning("QUERY (cache load)=" + cacheQuery);
            } catch (Exception e) {
            }
        }
        if (result == null) {

            result = new ArrayList<EntityCandidate>();
            //List<EntityCandidate_FreebaseTopic> topics = searcher.searchapi_topics_with_name_and_type(text, "any",true,15,types);
            List<EntityCandidate_FreebaseTopic> topics = searcher.searchapi_topics_with_name_and_type(text, "any", true, 20, types); //search api does not retrieve complete types, find types for them
            for (EntityCandidate_FreebaseTopic ec : topics) {
                //find_triplesForEntityId()
                //instantiate facts and types
                List<String[]> facts = find_triplesForEntity(ec);
                ec.setFacts(facts);
                for (String[] f : facts) {
                    if (f[0].equals("/type/object/type") &&
                            f[3].equals("n") &&
                            !ec.hasTypeId(f[2]))
                        ec.addType(new String[]{f[2], f[1]});
                }
            }

            Iterator<EntityCandidate_FreebaseTopic> it = topics.iterator();
            while (it.hasNext()) {
                EntityCandidate_FreebaseTopic ec = it.next();
                List<String> cell_text_tokens = StringUtils.toAlphaNumericTokens(ec.getName(), true);
                int size = cell_text_tokens.size();
                cell_text_tokens.removeAll(query_tokens);
                if (cell_text_tokens.size() == size)
                    it.remove(); //the entity name does not contain any query word. remove it
            }

            if (topics.size() == 0 && split_at_conjunction) { //does the query has conjunection? if so, we may need to try again with split queries
                String[] queries = text.split("\\band\\b");
                if (queries.length < 2) {
                    queries = text.split("\\bor\\b");
                    if (queries.length < 2) {
                        queries = text.split("/");
                    }
                }
                if (queries.length > 1) {
                    for (String q : queries) {
                        q = q.trim();
                        if (q.length() < 1) continue;
                        result.addAll(find_matchingEntitiesForText_serverFilterTypes(q, types));
                    }
                }
            }

            result.addAll(topics);
            try {
                solrCache.cache(toSolrKey(cacheQuery), result, commit);
                /* debug_helper_method(result);
                for(EntityCandidate ec: result){
                    if(ec.getFacts()!=null&&ec.getFacts().size()>0)
                        debug_fact_writer(ec.getId(),ec.getFacts());
                }*/
                log.warning("QUERY (cache save)=" + toSolrKey(cacheQuery) + "|" + cacheQuery);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (EntityCandidate ec : result) {
            Iterator<String[]> it = ec.getTypes().iterator();
            while (it.hasNext()) {
                String[] s = it.next();
                if (KB_InstanceFilter.ignoreType(s[0], s[1]))
                    it.remove();
            }
        }
        return result;
    }


    @Override
    public List<String[]> find_triplesForEntity(EntityCandidate ec) throws IOException {
        boolean forceQuery = false;

        /*  if(ec.getId().equals("/m/06qw_")||ec.getId().endsWith("/m/0859_")){
            forceQuery=true;
        }*/
        if (TableMinerConstants.FORCE_TOPICAPI_QUERY)
            forceQuery = true;

        String query = createQuery_findFacts(ec);
        if (query.length() == 0)
            return new ArrayList<String[]>();
        List<String[]> result = null;
        try {
            result = (List<String[]>) solrCache.retrieve(toSolrKey(query));
            if (result != null)
                log.warning("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
        } catch (Exception e) {
        }
        if (result == null || forceQuery) {

            List<String[]> facts = searcher.topicapi_facts_of_id(ec.getId());

            Iterator<String[]> it = facts.iterator();
            while (it.hasNext()) {
                String[] fact = it.next();
                if (KB_InstanceFilter.ignorePredicate_from_triple(fact[0]))
                    it.remove();
            }
            result = new ArrayList<String[]>();
            result.addAll(facts);
            try {
                solrCache.cache(toSolrKey(query), result, commit);
                //debug_helper_method(ec.getId(), result);
                //debug_fact_writer(ec.getId(),result);
                log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public double find_granularityForType(String type) throws IOException {
        Double result = null;

        try {
            Object o = solrCache.retrieve(toSolrKey(type));
            if (o != null) {
                log.warning("QUERY (cache load)=" + toSolrKey(type) + "|" + type);
                return (Double) o;
            }
        } catch (Exception e) {
        }

        if (result == null) {
            try {
                double granularity = searcher.find_granularityForType(type);
                result = Double.valueOf(granularity);
                try {
                    solrCache.cache(toSolrKey(type), result, commit);
                /* debug_helper_method(result);
                for(EntityCandidate ec: result){
                    if(ec.getFacts()!=null&&ec.getFacts().size()>0)
                        debug_fact_writer(ec.getId(),ec.getFacts());
                }*/
                    log.warning("QUERY (cache save)=" + toSolrKey(type) + "|" + type);
                } catch (Exception e) {
                    System.out.println("FAILED:" + type);
                    e.printStackTrace();
                }
            } catch (IOException ioe) {
                log.warning("ERROR(Instances of Type): Unable to fetch freebase page of instances of type: " + type);
            }
        }
        if (result == null)
            return -1.0;
        return result;
    }

    @Override
    public List<String[]> find_expected_types_of_relation(String majority_relation_name) throws IOException {
        List<String[]> triples = find_triplesForEntity(new EntityCandidate(majority_relation_name, majority_relation_name));
        List<String[]> types = new ArrayList<String[]>();
        for (String[] t : triples) {
            if (t[0].equals("/type/property/expected_type")) {
                types.add(new String[]{t[2], t[1]});
            }
        }
        return types;
    }

    public List<String[]> find_typesForEntityId(String id) throws IOException {
        String query = createQuery_findTypes(id);
        List<String[]> result = null;
        try {
            result = (List<String[]>) solrCache.retrieve(toSolrKey(query));
            if (result != null) {
                log.warning("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
                // debug_fact_writer(id+"_TYPE",result);
            }
        } catch (Exception e) {
        }
        if (result == null) {

            result = new ArrayList<String[]>();
            List<String[]> facts = searcher.topicapi_types_of_id(id);
            Iterator<String[]> it = facts.iterator();
            while (it.hasNext()) {
                String[] type_triple = it.next();
                if (KB_InstanceFilter.ignoreType(type_triple[2], type_triple[1]))
                    it.remove();
            }
            result.addAll(facts);
            try {
                solrCache.cache(toSolrKey(query), result, commit);
                // debug_helper_method(id, facts);
                log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String toSolrKey(String text) {
        //return String.valueOf(text.hashCode());
        return String.valueOf(text);
    }

    /*private boolean debug_helper_method(List<EntityCandidate> list) {
        for (EntityCandidate ec : list) {
            if (ec.getId().equals("/m/06qw_")){
                if(ec.getFacts()==null||ec.getFacts().size()==0)
                    System.out.println("++++++cache saved (no facts)" + ec.getId() + ",=" + ec.getTypeIds());
                else{
                    String bugged_fact=debug_helper_method_2(ec.getFacts());
                    System.out.println("++++++cache saved, bugged fact("+bugged_fact+")" + ec.getId() + ",=" + ec.getTypeIds());
                }

            }
            return true;
        }
        return false;
    }

    private boolean debug_helper_method(String id, List<String[]> facts) {
        if (id.equals("/m/06qw_")){
            String type = "";
            for (String[] ft : facts) {
                if (ft[0].equals("/type/object/type") && ft[3].equals("n")) {
                    type = type + "," + ft[2];
                }
            }
            String bugged_fact=debug_helper_method_2(facts);

            System.out.println("*******cache saved facts (buggged fact="+bugged_fact+") " + id + ",=" + type);
        }
        return false;
    }

    private String debug_helper_method_2(List<String[]> facts){
        for(String[] f: facts){
            if(f[1].contains("Wind instrument")||
                    f[1].contains("/m/0859_")||
                    f[1].contains("/music/performance_role"))
                return f[1];
        }
        return "";
    }

    private void debug_fact_writer(String entity_id, List<String[]> facts) throws IOException {
        PrintWriter p = new PrintWriter(new FileWriter("debug_facts.txt",true));
        String s = "";
        for(String[] f: facts){
            s=s+f[1]+"|";
        }
        p.println(entity_id+"\t\t\t"+s);
        p.close();
    }*/
}
