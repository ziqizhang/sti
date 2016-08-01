package uk.ac.shef.dcs.kbsearch.sparql;

import javafx.util.Pair;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by - on 10/06/2016.
 */
public class DBpediaSearch extends SPARQLSearch {

    private static final boolean ALWAYS_CALL_REMOTE_SEARCHAPI = false;
    private static final Logger LOG = Logger.getLogger(DBpediaSearch.class.getName());
    private static final boolean AUTO_COMMIT = true;

    private static final String DBP_SPARQL_ENDPOINT = "dbp.sparql.endpoint";
    private static final String DBP_ONTOLOGY_URL = "dbp.ontology.url";

    private OntModel ontology;

    /**
     * @param fuzzyKeywords   given a query string, kbsearch will firstly try to fetch results matching the exact query. when no match is
     *                        found, you can set fuzzyKeywords to true, to let kbsearch to break the query string based on conjunective words.
     *                        So if the query string is "tom and jerry", it will try "tom" and "jerry"
     * @param cacheEntity     the solr instance to cache retrieved entities from the kb. pass null if not needed
     * @param cacheConcept    the solr instance to cache retrieved classes from the kb. pass null if not needed
     * @param cacheProperty   the solr instance to cache retrieved properties from the kb. pass null if not needed
     * @param cacheSimilarity the solr instance to cache computed semantic similarity between entity and class. pass null if not needed
     * @throws IOException
     */
    public DBpediaSearch(Properties properties,
                         Boolean fuzzyKeywords,
                         EmbeddedSolrServer cacheEntity,
                         EmbeddedSolrServer cacheConcept,
                         EmbeddedSolrServer cacheProperty,
                         EmbeddedSolrServer cacheSimilarity) throws IOException {
        super(properties.getProperty(DBP_SPARQL_ENDPOINT), fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty, cacheSimilarity);
        String ontURL = properties.getProperty(DBP_ONTOLOGY_URL);
        if (ontURL != null)
            ontology = loadModel(ontURL);
        otherCache = new HashMap<>();
        resultFilter = new DBpediaSearchResultFilter(properties.getProperty(KB_SEARCH_RESULT_STOPLIST));
    }

    private OntModel loadModel(String ontURL) {
        OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        base.read(ontURL);
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> findEntityCandidates(String content) throws KBSearchException {
        /*if(content.equals("Ramji Manjhi"))
            System.out.println();*/
        String query = createSolrCacheQuery_findResources(content);
        boolean forceQuery = false;

        content = StringEscapeUtils.unescapeXml(content);
        int bracket = content.indexOf("(");
        if (bracket != -1) {
            content = content.substring(0, bracket).trim();
        }
        if (StringUtils.toAlphaNumericWhitechar(content).trim().length() == 0)
            return new ArrayList<>();
        if (ALWAYS_CALL_REMOTE_SEARCHAPI)
            forceQuery = true;


        List<Entity> result = null;
        if (!forceQuery) {
            try {
                result = (List<Entity>) cacheEntity.retrieve(query);
                if (result != null)
                    LOG.debug("QUERY (entities, cache load)=" + query + "|" + query);
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = new ArrayList<>();
            try {
                //1. try exact string
                String sparqlQuery = createExactMatchQueries(escape(content));
                List<Pair<String, String>> queryResult = queryByLabel(sparqlQuery, content);

                //2. if result is empty, try regex
                if (queryResult.size() == 0 && fuzzyKeywords) {
                    LOG.debug("(query by regex. This can take a long time)");
                    sparqlQuery = createRegexQuery(content);
                    queryResult = queryByLabel(sparqlQuery, content);
                }
                //3. rank result by the degree of matches
                rank(queryResult, content);

                //firstly fetch candidate freebase topics. pass 'true' to only keep candidates whose name overlap with the query term
                LOG.debug("(DBpedia QUERY =" + queryResult.size() + " results)");
                for (Pair<String, String> candidate : queryResult) {
                    //Next get attributes for each topic
                    String label = candidate.getValue();
                    if (label == null)
                        label = content;
                    Entity ec = new Entity(candidate.getKey(), label);
                    List<Attribute> attributes = findAttributesOfEntities(ec);
                    ec.setAttributes(attributes);
                    for (Attribute attr : attributes) {
                        resetResourceValue(attr);
                        if (attr.getRelationURI().endsWith(RDFEnum.RELATION_HASTYPE_SUFFIX_PATTERN.getString()) &&
                                !ec.hasType(attr.getValueURI())) {
                            ec.addType(new Clazz(attr.getValueURI(), attr.getValue()));
                        }
                    }
                    result.add(ec);
                }

                cacheEntity.cache(query, result, AUTO_COMMIT);
                LOG.debug("QUERY (entities, cache save)=" + query + "|" + query);
            } catch (Exception e) {
                throw new KBSearchException(e);
            }
        }

        //filter entity's clazz, and attributes
        String id = "|";
        for (Entity ec : result) {
            id = id + ec.getId() + ",";
            //ec.setTypes(FreebaseSearchResultFilter.filterClazz(ec.getTypes()));
            List<Clazz> filteredTypes = getResultFilter().filterClazz(ec.getTypes());
            ec.clearTypes();
            for (Clazz ft : filteredTypes)
                ec.addType(ft);
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException {
        String queryCache = createSolrCacheQuery_findResources(content);
        boolean forceQuery = false;

        content = StringEscapeUtils.unescapeXml(content);
        int bracket = content.indexOf("(");
        if (bracket != -1) {
            content = content.substring(0, bracket).trim();
        }
        if (StringUtils.toAlphaNumericWhitechar(content).trim().length() == 0)
            return new ArrayList<>();
        if (ALWAYS_CALL_REMOTE_SEARCHAPI)
            forceQuery = true;


        List<Entity> result = null;
        if (!forceQuery) {
            try {
                result = (List<Entity>) cacheEntity.retrieve(queryCache);
                if (result != null) {
                    LOG.debug("QUERY (entities, cache load)=" + queryCache + "|" + queryCache);
                    if (types.length > 0) {
                        Iterator<Entity> it = result.iterator();
                        while (it.hasNext()) {
                            Entity ec = it.next();
                            boolean typeSatisfied = false;
                            for (String t : types) {
                                if (ec.hasType(t)) {
                                    typeSatisfied = true;
                                    break;
                                }
                            }
                            if (!typeSatisfied)
                                it.remove();
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = new ArrayList<>();
            try {
                //1. try exact string
                String sparqlQuery = createExactMatchWithOptionalTypes(content);
                List<Pair<String, String>> resourceAndType = queryByLabel(sparqlQuery, content);
                boolean hasExactMatch = resourceAndType.size() > 0;
                if (types.length > 0) {
                    Iterator<Pair<String, String>> it = resourceAndType.iterator();
                    while (it.hasNext()) {
                        Pair<String, String> ec = it.next();
                        boolean typeSatisfied = false;
                        for (String t : types) {
                            if (t.equals(ec.getValue())) {
                                typeSatisfied = true;
                                break;
                            }
                        }
                        if (!typeSatisfied)
                            it.remove();
                    }
                }//with this query the 'value' of the pair will be the type, now need to reset it to actual value
                List<Pair<String, String>> queryResult = new ArrayList<>();
                if (resourceAndType.size() > 0) {
                    Pair<String, String> matchedResource = resourceAndType.get(0);
                    queryResult.add(new Pair<>(matchedResource.getKey(), content));
                }

                //2. if result is empty, try regex
                if (!hasExactMatch && fuzzyKeywords) {
                    LOG.debug("(query by regex. This can take a long time)");
                    sparqlQuery = createRegexQuery(content, types);
                    queryResult = queryByLabel(sparqlQuery, content);
                }
                //3. rank result by the degree of matches
                rank(queryResult, content);

                //firstly fetch candidate freebase topics. pass 'true' to only keep candidates whose name overlap with the query term
                LOG.debug("(DBpedia QUERY =" + queryResult.size() + " results)");
                for (Pair<String, String> candidate : queryResult) {
                    //Next get attributes for each topic
                    String label = candidate.getValue();
                    if (label == null)
                        label = content;
                    Entity ec = new Entity(candidate.getKey(), label);
                    List<Attribute> attributes = findAttributesOfEntities(ec);
                    ec.setAttributes(attributes);
                    for (Attribute attr : attributes) {
                        resetResourceValue(attr);
                        if (attr.getRelationURI().endsWith(RDFEnum.RELATION_HASTYPE_SUFFIX_PATTERN.getString()) &&
                                !ec.hasType(attr.getValueURI())) {
                            ec.addType(new Clazz(attr.getValueURI(), attr.getValue()));
                        }
                    }
                    result.add(ec);
                }

                cacheEntity.cache(queryCache, result, AUTO_COMMIT);
                LOG.debug("QUERY (entities, cache save)=" + queryCache + "|" + queryCache);
            } catch (Exception e) {
                throw new KBSearchException(e);
            }
        }

        //filter entity's clazz, and attributes
        String id = "|";
        for (Entity ec : result) {
            id = id + ec.getId() + ",";
            //ec.setTypes(FreebaseSearchResultFilter.filterClazz(ec.getTypes()));
            List<Clazz> filteredTypes = getResultFilter().filterClazz(ec.getTypes());
            ec.clearTypes();
            for (Clazz ft : filteredTypes)
                ec.addType(ft);
        }

        return result;
    }

    // if the attribute's value is an URL, fetch the label of that resource, and reset its attr value
    @SuppressWarnings("unchecked")
    private void resetResourceValue(Attribute attr) throws KBSearchException {
        String value = attr.getValue();
        if (value.startsWith("http")) {
            String queryCache = createSolrCacheQuery_findLabelForResource(value);
            boolean forceQuery = false;

            if (ALWAYS_CALL_REMOTE_SEARCHAPI)
                forceQuery = true;

            List<String> result = null;
            if (!forceQuery) {
                try {
                    result = (List<String>) cacheEntity.retrieve(queryCache);
                    if (result != null) {
                        LOG.debug("QUERY (resource labels, cache load)=" + queryCache + "|" + queryCache);
                    }
                } catch (Exception e) {
                }
            }
            if (result == null) {
                try {
                    //1. try exact string
                    String sparqlQuery = createGetLabelQuery(value);
                    result = queryForLabel(sparqlQuery, value);

                    cacheEntity.cache(queryCache, result, AUTO_COMMIT);
                    LOG.debug("QUERY (entities, cache save)=" + queryCache + "|" + queryCache);
                } catch (Exception e) {
                    throw new KBSearchException(e);
                }
            }

            if (result.size() > 0) {
                attr.setValueURI(value);
                attr.setValue(result.get(0));
            } else {
                attr.setValueURI(value);
            }
        }
    }

    @Override
    public List<Attribute> findAttributesOfEntities(Entity ec) throws KBSearchException {
        return find_attributes(ec.getId(), cacheEntity);
    }

    @SuppressWarnings("unchecked")
    private List<Attribute> find_attributes(String id, SolrCache cache) throws KBSearchException {
        if (id.length() == 0)
            return new ArrayList<>();
        boolean forceQuery = false;
        if (ALWAYS_CALL_REMOTE_SEARCHAPI)
            forceQuery = true;

        String queryCache = createSolrCacheQuery_findAttributesOfResource(id);
        List<Attribute> result = null;
        try {
            result = (List<Attribute>) cache.retrieve(queryCache);
            if (result != null)
                LOG.debug("QUERY (attributes of id, cache load)=" + queryCache + "|" + queryCache);
        } catch (Exception e) {
        }
        if (result == null || forceQuery) {
            result = new ArrayList<>();
            String query = "SELECT DISTINCT ?p ?o WHERE {\n" +
                    "<" + id + "> ?p ?o .\n" +
                    "}";

            Query sparqlQuery = QueryFactory.create(query);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery);

            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                RDFNode range = qs.get("?p");
                String r = range.toString();
                RDFNode domain = qs.get("?o");
                if (domain != null) {
                    String d = domain.toString();
                    Attribute attr = new DBpediaAttribute(r, d);
                    result.add(attr);
                }
            }

            try {
                cache.cache(queryCache, result, AUTO_COMMIT);
                LOG.debug("QUERY (attributes of id, cache save)=" + query + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //filtering
        result = getResultFilter().filterAttribute(result);
        return result;
    }

    @Override
    public List<Attribute> findAttributesOfClazz(String clazzId) throws KBSearchException {
        return find_attributes(clazzId, cacheEntity);
    }

    @Override
    public List<Attribute> findAttributesOfProperty(String propertyId) throws KBSearchException {
        return find_attributes(propertyId, cacheEntity);
    }

    @Override
    public double findGranularityOfClazz(String clazz) throws KBSearchException {
        if (ontology == null)
            throw new KBSearchException("Not supported");
        return 0;
    }

    @Override
    public double findEntityClazzSimilarity(String entity_id, String clazz_url) throws KBSearchException {
        if (ontology == null)
            throw new KBSearchException("Not supported");
        return 0;
    }

    @Override
    public void cacheEntityClazzSimilarity(String entity_id, String clazz_url, double score, boolean biDirectional, boolean commit) throws KBSearchException {
        String query = createSolrCacheQuery_findEntityClazzSimilarity(entity_id, clazz_url);
        try {
            cacheSimilarity.cache(query, score, commit);
            LOG.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
            if (biDirectional) {
                query = clazz_url + "<>" + entity_id;
                cacheSimilarity.cache(query, score, commit);
                LOG.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commitChanges() throws KBSearchException {
        try {
            cacheConcept.commit();
            cacheEntity.commit();
            cacheProperty.commit();
            for (SolrCache cache : otherCache.values())
                cache.commit();
        } catch (Exception e) {
            throw new KBSearchException(e);
        }
    }


    @Override
    public void closeConnection() throws KBSearchException {
        try {
            if (cacheEntity != null)
                cacheEntity.shutdown();
            if (cacheConcept != null)
                cacheConcept.shutdown();
            if (cacheProperty != null)
                cacheProperty.shutdown();
        } catch (Exception e) {
            throw new KBSearchException(e);
        }
    }

    protected String createSolrCacheQuery_findLabelForResource(String url) {
        return "LABEL_" + url;
    }

    @Override
    protected List<String> queryForLabel(String sparqlQuery, String resourceURI) throws KBSearchException {
        try {
            org.apache.jena.query.Query query = QueryFactory.create(sparqlQuery);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);

            List<String> out = new ArrayList<>();
            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                RDFNode domain = qs.get("?o");
                String d = null;
                if (domain != null)
                    d = domain.toString();
                if (d != null) {
                    if (d.contains("@")) { //language tag in dbpedia literals
                        if (!d.endsWith("@en"))
                            continue;
                        else {
                            int trim = d.lastIndexOf("@en");
                            if (trim != -1)
                                d = d.substring(0, trim).trim();
                        }
                    }

                }
                out.add(d);
            }

            if (out.size() == 0) { //the resource has no statement with prop "rdfs:label", apply heuristics to parse the
                //resource uri
                int trim = resourceURI.lastIndexOf("#");
                if (trim == -1)
                    trim = resourceURI.lastIndexOf("/");
                if (trim != -1) {
                    String stringValue = resourceURI.substring(trim + 1).replaceAll("[^a-zA-Z0-9]", "").trim();
                    if (resourceURI.contains("yago")) { //this is an yago resource, which may have numbered ids as suffix
                        //e.g., City015467
                        int end = 0;
                        for (int i = 0; i < stringValue.length(); i++) {
                            if (Character.isDigit(stringValue.charAt(i))) {
                                end = i;
                                break;
                            }
                        }
                        if (end > 0)
                            stringValue = stringValue.substring(0, end);
                    }
                    stringValue = StringUtils.splitCamelCase(stringValue);
                    out.add(stringValue);
                }
            }
            return out;
        }
        catch (QueryParseException ex) {
            throw new KBSearchException("Invalid query: " + sparqlQuery, ex);
        }
    }
}
