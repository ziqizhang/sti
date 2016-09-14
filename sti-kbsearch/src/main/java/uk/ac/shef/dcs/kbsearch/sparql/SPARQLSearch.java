package uk.ac.shef.dcs.kbsearch.sparql;

import javafx.util.Pair;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.optimizer.Pattern;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.Levenshtein;

import uk.ac.shef.dcs.kbsearch.KBDefinition;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.KBSearchUtis;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * test queries:
 * <p>
 * SELECT DISTINCT ?s ?o WHERE {
 * ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o .
 * FILTER ( regex (str(?o), "\\bcat\\b", "i") ) }
 * <p>
 * <p>
 * SELECT DISTINCT ?s WHERE {
 * ?s <http://www.w3.org/2000/01/rdf-schema#label> "Nature Cat"@en .
 * }
 * <p>
 * SELECT DISTINCT ?p ?o WHERE {
 * wd:Q21043336 ?p ?o .
 * }
 */
public abstract class SPARQLSearch extends KBSearch {

  static final String REGEX_QUERY = "SELECT DISTINCT ?s ?o WHERE {%1$s .\n%2$sFILTER ( regex (str(?o), \"%3$s\", \"i\") )}";
  static final String REGEX_QUERY_CONTAINS = "SELECT DISTINCT ?s ?o WHERE {%1$s .\n%2$s}";
  static final String EXACT_MATCH_QUERY = "SELECT DISTINCT ?s WHERE {%1$s .}";
  static final String EXACT_MATCH_WITH_OPTIONAL_TYPES_QUERY = "SELECT DISTINCT ?s ?o WHERE {%1$s .\nOPTIONAL {?s a ?o}}";
  static final String LABEL_QUERY = "SELECT DISTINCT ?o WHERE {%1$s .}";

  static final String REGEX_WHERE = "?s <%1$s> ?o";
  static final String REGEX_WHERE_CONTAINS = "?s <%1$s> ?o . ?o <bif:contains> '\"%2$s\"'";
  static final String REGEX_FILTER = "\\b%1$s\\b";
  static final String REGEX_TYPES = "?s a <%1$s>";
  static final String MATCH_WHERE = "?s <%1$s> \"%2$s\"%3$s";
  static final String LABEL_WHERE = "<%2$s> <%1$s> ?o";

  /**
   * Escape patterns from http://www.w3.org/TR/rdf-sparql-query/#grammarEscapes
   */
  private static final Map<Character, String> SPARQL_ESCAPE_REPLACEMENTS;

  static {
    Map<Character, String> map = new HashMap<>();

    map.put('\t', "\\t");
    map.put('\n', "\\n");
    map.put('\r', "\\r");
    map.put('\b', "\\b");
    map.put('\f', "\\f");
    map.put('\"', "\\\"");
    map.put('\'', "\\'");
    map.put('\\', "\\\\");

    SPARQL_ESCAPE_REPLACEMENTS = Collections.unmodifiableMap(map);
  }

  protected StringMetric stringMetric = new Levenshtein();

  /**
   * @param kbDefinition    the definition of the knowledge base.
   * @param fuzzyKeywords   given a query string, kbsearch will firstly try to fetch results matching the exact query. when no match is
   *                        found, you can set fuzzyKeywords to true, to let kbsearch to break the query string based on conjunective words.
   *                        So if the query string is "tom and jerry", it will try "tom" and "jerry"
   * @param cachesBasePath  Base path for the initialized solr caches.
   * @throws IOException
   */
  public SPARQLSearch(KBDefinition kbDefinition,
                      Boolean fuzzyKeywords,
                      String cachesBasePath) throws IOException {
    super(kbDefinition, fuzzyKeywords, cachesBasePath);
  }

  protected String createRegexQuery(String content, String... types) {
    StringBuilder typesUnion = new StringBuilder();
    if (types.length > 0) {
      String typesFilter = createFilter(Arrays.asList(types), REGEX_TYPES);
      typesUnion.append(typesFilter);
      typesUnion.append(" .\n");
    }

    if (kbDefinition.getUseBifContains()) {
      String where = createFilter(kbDefinition.getPredicateLabel(), REGEX_WHERE_CONTAINS, escapeSPARQLLiteral(content));

      String query = String.format(REGEX_QUERY_CONTAINS, where, typesUnion);
      return query;
    }
    else {
      String where = createFilter(kbDefinition.getPredicateLabel(), REGEX_WHERE);
      String filter = escapeSPARQLLiteral(String.format(REGEX_FILTER, java.util.regex.Pattern.quote(content)));

      String query = String.format(REGEX_QUERY, where, typesUnion, filter);
      return query;
    }
  }

  protected String createExactMatchQueries(String content) {
    String filter = createFilter(kbDefinition.getPredicateLabel(), MATCH_WHERE, escapeSPARQLLiteral(content), kbDefinition.getLanguageSuffix());
    String query = String.format(EXACT_MATCH_QUERY, filter);

    return query;
  }

  protected String createExactMatchWithOptionalTypes(String content) {
    String filter = createFilter(kbDefinition.getPredicateLabel(), MATCH_WHERE, escapeSPARQLLiteral(content), kbDefinition.getLanguageSuffix());
    String query = String.format(EXACT_MATCH_WITH_OPTIONAL_TYPES_QUERY, filter);

    return query;
  }

  protected String createGetLabelQuery(String content) {
    content = content.replaceAll("\\s+", "");
    String filter = createFilter(kbDefinition.getPredicateLabel(), LABEL_WHERE, content);
    String query = String.format(LABEL_QUERY, filter);

    return query;
  }

  String createFilter(Collection<String> values, String pattern, String... args) {
    if (values == null || values.size() == 0) {
      return "";
    }

    if (values.size() == 1) {
      return String.format(pattern, Stream.concat(values.stream(), Stream.of(args)).toArray());
    }

    final String newPattern = String.format("{%1$s}", pattern);
    Stream<String> conditions = values.stream().map(item -> String.format(newPattern, Stream.concat(Stream.of(item), Stream.of(args)).toArray()));
    return String.join(" UNION ", conditions.collect(Collectors.toList()));
  }

  /**
   * @param sparqlQuery
   * @param string
   * @return
   */
  protected List<Pair<String, String>> queryByLabel(String sparqlQuery, String string) {
    log.info("SPARQL query: \n" + sparqlQuery);

    org.apache.jena.query.Query query = QueryFactory.create(sparqlQuery);
    QueryExecution qexec = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), query);

    List<Pair<String, String>> out = new ArrayList<>();
    ResultSet rs = qexec.execSelect();
    while (rs.hasNext()) {

      QuerySolution qs = rs.next();
      RDFNode subject = qs.get("?s");
      RDFNode object = qs.get("?o");
      out.add(new Pair<>(subject.toString(), object != null ? object.toString() : string));
    }
    return out;
  }

  protected abstract List<String> queryForLabel(String sparqlQuery, String resourceURI) throws KBSearchException;

  /**
   * Compares the similarity of the object value of certain resource (entity) and the cell value text (original label).
   * Then, it also sorts the list of candidates based on the scores.
   *
   * @param candidates
   * @param originalQueryLabel
   */
  protected void rank(List<Pair<String, String>> candidates, String originalQueryLabel) {
    final Map<Pair<String, String>, Double> scores = new HashMap<>();
    for (Pair<String, String> p : candidates) {
      String label = p.getValue();
      double s = stringMetric.compare(label, originalQueryLabel);
      scores.put(p, s);
    }

    Collections.sort(candidates, (o1, o2) -> {
      Double s1 = scores.get(o1);
      Double s2 = scores.get(o2);
      return s2.compareTo(s1);
    });
  }

  @Override
  public List<Entity> findEntityCandidates(String content) throws KBSearchException {
    return findEntityCandidatesOfTypes(content);
  }

  @Override
  public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException {
    final String sparqlQuery;
    if (types.length > 0) {
      sparqlQuery = createExactMatchQueries(content);
    } else {
      sparqlQuery = createExactMatchWithOptionalTypes(content);
    }

    return queryEntityCandidates(content, sparqlQuery, types);
  }

  @SuppressWarnings("unchecked")
  private List<Entity> queryEntityCandidates(String content, String sparqlQuery, String... types)
      throws KBSearchException {
    String queryCache = createSolrCacheQuery_findResources(content);

    content = StringEscapeUtils.unescapeXml(content);
    int bracket = content.indexOf("(");
    if (bracket != -1) {
      content = content.substring(0, bracket).trim();
    }
    if (StringUtils.toAlphaNumericWhitechar(content).trim().length() == 0)
      return new ArrayList<>();


    List<Entity> result = null;
    if (!ALWAYS_CALL_REMOTE_SEARCHAPI) {
      try {
        result = (List<Entity>) cacheEntity.retrieve(queryCache);
        if (result != null) {
          log.debug("QUERY (entities, cache load)=" + queryCache + "|" + queryCache);
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
        log.error(e.getLocalizedMessage(), e);
      }
    }
    if (result == null) {
      result = new ArrayList<>();
      try {
        //1. try exact string
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
          log.debug("(query by regex. This can take a long time)");
          sparqlQuery = createRegexQuery(content, types);
          queryResult = queryByLabel(sparqlQuery, content);
        }
        //3. rank result by the degree of matches
        rank(queryResult, content);

        //firstly fetch candidate freebase topics. pass 'true' to only keep candidates whose name overlap with the query term
        log.debug("(DB QUERY =" + queryResult.size() + " results)");
        for (Pair<String, String> candidate : queryResult) {
          //Next get attributes for each topic
          String label = candidate.getValue();
          if (label == null)
            label = content;
          Entity ec = new Entity(candidate.getKey(), label);
          List<Attribute> attributes = findAttributesOfEntities(ec);
          ec.setAttributes(attributes);
          for (Attribute attr : attributes) {
            adjustValueOfURLResource(attr);
            if (KBSearchUtis.contains(kbDefinition.getPredicateType(), attr.getRelationURI()) &&
                !ec.hasType(attr.getValueURI())) {
              ec.addType(new Clazz(attr.getValueURI(), attr.getValue()));
            }
          }
          result.add(ec);
        }

        cacheEntity.cache(queryCache, result, AUTO_COMMIT);
        log.debug("QUERY (entities, cache save)=" + queryCache + "|" + queryCache);
      } catch (Exception e) {
        throw new KBSearchException(e);
      }
    }

    //filter entity's clazz, and attributes
    String id = "|";
    for (Entity ec : result) {
      id = id + ec.getId() + ",";
      //ec.setTypes(FreebaseSearchResultFilter.filterClazz(ec.getTypes()));
      List<Clazz> filteredTypes = resultFilter.filterClazz(ec.getTypes());
      ec.clearTypes();
      for (Clazz ft : filteredTypes)
        ec.addType(ft);
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private void adjustValueOfURLResource(Attribute attr) throws KBSearchException {
    String value = attr.getValue();
    if (value.startsWith("http")) {
      String queryCache = createSolrCacheQuery_findLabelForResource(value);


      List<String> result = null;
      if (!ALWAYS_CALL_REMOTE_SEARCHAPI) {
        try {
          result = (List<String>) cacheEntity.retrieve(queryCache);
          if (result != null) {
            log.debug("QUERY (resource labels, cache load)=" + queryCache + "|" + queryCache);
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
          log.debug("QUERY (entities, cache save)=" + queryCache + "|" + queryCache);
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
    return findAttributes(ec.getId(), cacheEntity);
  }

  @SuppressWarnings("unchecked")
  private List<Attribute> findAttributes(String id, SolrCache cache) throws KBSearchException {
    if (id.length() == 0)
      return new ArrayList<>();

    String queryCache = createSolrCacheQuery_findAttributesOfResource(id);
    List<Attribute> result = null;
    if (!ALWAYS_CALL_REMOTE_SEARCHAPI) {
      try {
        result = (List<Attribute>) cache.retrieve(queryCache);
        if (result != null)
          log.debug("QUERY (attributes of id, cache load)=" + queryCache + "|" + queryCache);
      } catch (Exception e) {
        log.error(e.getLocalizedMessage(), e);
      }
    }

    if (result == null) {
      result = new ArrayList<>();
      String query = "SELECT DISTINCT ?p ?o WHERE {\n" +
          "<" + id + "> ?p ?o .\n" +
          "}";

      Query sparqlQuery = QueryFactory.create(query);
      QueryExecution qexec = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), sparqlQuery);

      ResultSet rs = qexec.execSelect();
      while (rs.hasNext()) {
        QuerySolution qs = rs.next();
        RDFNode predicate = qs.get("?p");
        RDFNode object = qs.get("?o");
        if (object != null) {
          Attribute attr = new SPARQLAttribute(predicate.toString(), object.toString());
          result.add(attr);
        }
      }

      try {
        cache.cache(queryCache, result, AUTO_COMMIT);
        log.debug("QUERY (attributes of id, cache save)=" + query + "|" + query);
      } catch (Exception e) {
        log.error(e.getLocalizedMessage(), e);
      }
    }

    //filtering
    result = resultFilter.filterAttribute(result);
    return result;
  }

  @Override
  public List<Attribute> findAttributesOfClazz(String clazzId) throws KBSearchException {
    return findAttributes(clazzId, cacheEntity);
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId) throws KBSearchException {
    return findAttributes(propertyId, cacheEntity);
  }

  protected String createSolrCacheQuery_findLabelForResource(String url) {
    return "LABEL_" + url;
  }

  private String escapeSPARQLLiteral(String value){
    StringBuilder builder = new StringBuilder(value);

    for(int index = builder.length() - 1; index >= 0; index--) {
      String replacement = SPARQL_ESCAPE_REPLACEMENTS.get(value.charAt(index));

      if (replacement != null) {
        builder.deleteCharAt(index);
        builder.insert(index, replacement);
      }
    }

    return builder.toString();
  }
}
