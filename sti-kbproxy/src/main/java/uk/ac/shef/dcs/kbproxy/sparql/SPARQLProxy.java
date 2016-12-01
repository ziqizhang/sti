package uk.ac.shef.dcs.kbproxy.sparql;

import javafx.util.Pair;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.Levenshtein;

import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.KBProxyUtils;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.net.URI;
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
public abstract class SPARQLProxy extends KBProxy {

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

  static final String INSERT_BASE = "INSERT DATA {GRAPH <%1$s> {%2$s .}}";
  static final String INSERT_CHECK = "SELECT ?c WHERE { <%1$s> ?p ?c.} LIMIT 1";

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
   * @param fuzzyKeywords   given a query string, kbproxy will firstly try to fetch results matching the exact query. when no match is
   *                        found, you can set fuzzyKeywords to true, to let kbproxy to break the query string based on conjunective words.
   *                        So if the query string is "tom and jerry", it will try "tom" and "jerry"
   * @param cachesBasePath  Base path for the initialized solr caches.
   * @throws IOException
   */
  public SPARQLProxy(KBDefinition kbDefinition,
                     Boolean fuzzyKeywords,
                     String cachesBasePath) throws IOException {
    super(kbDefinition, fuzzyKeywords, cachesBasePath);
  }

  protected String createRegexQuery(String content, Integer limit, String... types) {
    StringBuilder typesUnion = new StringBuilder();
    if (types.length > 0) {
      String typesFilter = createFilter(Arrays.asList(types), REGEX_TYPES);
      typesUnion.append(typesFilter);
      typesUnion.append(" .\n");
    }

    String query;
    if (kbDefinition.getUseBifContains()) {
      String where = createFilter(kbDefinition.getPredicateLabel(), REGEX_WHERE_CONTAINS, escapeSPARQLLiteral(content));

      query = String.format(REGEX_QUERY_CONTAINS, where, typesUnion);
    }
    else {
      String where = createFilter(kbDefinition.getPredicateLabel(), REGEX_WHERE);
      String filter = escapeSPARQLLiteral(String.format(REGEX_FILTER, java.util.regex.Pattern.quote(content)));

      query = String.format(REGEX_QUERY, where, typesUnion, filter);
    }

    if (limit != null){
      query += " LIMIT " + limit.toString();
    }

    return query;
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

  protected abstract List<String> queryForLabel(String sparqlQuery, String resourceURI) throws KBProxyException;

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
  @SuppressWarnings("unchecked")
  public List<Entity> findEntityByFulltext(String pattern, int limit) throws KBProxyException {
    String queryCache = createSolrCacheQuery_fulltextSearch(pattern, limit);

    try {
      List<Entity> result = (List<Entity>) cacheEntity.retrieve(queryCache);

      if (result != null) {
        return  result;
      }

      String query = createRegexQuery(pattern, limit);
      List<Pair<String, String>> queryResult = queryByLabel(query, pattern);

      result = queryResult.stream().map(pair -> new Entity(pair.getKey(), pair.getValue())).collect(Collectors.toList());
      cacheEntity.cache(queryCache, result, AUTO_COMMIT);

      return result;
    }
    catch (Exception e){
      throw new KBProxyException(e);
    }
  }

  @Override
  public List<Entity> findEntityCandidates(String content) throws KBProxyException {
    return findEntityCandidatesOfTypes(content);
  }

  @Override
  public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBProxyException {
    final String sparqlQuery;
    if (types.length > 0) {
      sparqlQuery = createExactMatchQueries(content);
    } else {
      sparqlQuery = createExactMatchWithOptionalTypes(content);
    }

    return queryEntityCandidates(content, sparqlQuery, types);
  }

  @Override
  public boolean isInsertSupported() {
    return kbDefinition.isInsertSupported();
  }

  @Override
  public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass) throws KBProxyException {
    if (!isInsertSupported()){
      throw new KBProxyException("Insertion of new classes is not supported for the " + kbDefinition.getName() + " knowledge base.");
    }

    if (isNullOrEmpty(label)){
      throw new KBProxyException("Label of the new class must not be empty.");
    }

    String url = checkOrGenerateUrl(kbDefinition.getInsertSchemaElementPrefix(), uri);

    if (isNullOrEmpty(superClass)){
      superClass = kbDefinition.getInsertRootClass();
    }

    StringBuilder tripleDefinition = createTripleDefinitionBase(url, label);
    appendCollection(tripleDefinition, kbDefinition.getInsertAlternativeLabel(), alternativeLabels, true);
    appendValue(tripleDefinition, kbDefinition.getInsertSubclassOf(), superClass, false);
    appendValue(tripleDefinition, kbDefinition.getInsertInstanceOf(), kbDefinition.getInsertClassType(), false);

    insert(tripleDefinition.toString());
    return new Entity(url, label);
  }

  @Override
  public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels, Collection<String> classes) throws KBProxyException {
    if (!isInsertSupported()){
      throw new KBProxyException("Insertion of new concepts is not supported for the " + kbDefinition.getName() + " knowledge base.");
    }

    if (isNullOrEmpty(label)){
      throw new KBProxyException("Label of the new concept must not be empty.");
    }

    String url = checkOrGenerateUrl(kbDefinition.getInsertDataElementPrefix(), uri);

    StringBuilder tripleDefinition = createTripleDefinitionBase(url, label);
    appendCollection(tripleDefinition, kbDefinition.getInsertAlternativeLabel(), alternativeLabels, true);
    boolean typeSpecified = appendCollection(tripleDefinition, kbDefinition.getInsertInstanceOf(), classes, false);
    if (!typeSpecified){
      appendValue(tripleDefinition, kbDefinition.getInsertInstanceOf(), kbDefinition.getInsertRootClass(), false);
    }

    insert(tripleDefinition.toString());
    return new Entity(url, label);
  }

  private boolean appendCollection(StringBuilder tripleDefinition, String predicate, Collection<String> values, boolean isLiteral) {
    if (values == null) {
      return false;
    }

    boolean valueAppended = false;
    for (String value : values){
      if (isNullOrEmpty(value)){
        continue;
      }

      valueAppended = true;
      appendValue(tripleDefinition, predicate, value, isLiteral);
    }

    return  valueAppended;
  }

  private void appendValue(StringBuilder tripleDefinition, String predicate, String value, boolean isLiteral) {
    tripleDefinition.append(" ; <");
    tripleDefinition.append(predicate);

    if (isLiteral){
      tripleDefinition.append("> \"");
      tripleDefinition.append(value);
      tripleDefinition.append("\"");
    }
    else {
      tripleDefinition.append("> <");
      tripleDefinition.append(value);
      tripleDefinition.append(">");
    }
  }

  private StringBuilder createTripleDefinitionBase(String url, String label){
    StringBuilder tripleDefinition = new StringBuilder("<");
    tripleDefinition.append(url);
    tripleDefinition.append("> <");
    tripleDefinition.append(kbDefinition.getInsertLabel());
    tripleDefinition.append("> \"");
    tripleDefinition.append(escapeSPARQLLiteral(label));
    tripleDefinition.append("\"");

    return tripleDefinition;
  }

  private void insert(String tripleDefinition) {
    String sparqlQuery = String.format(INSERT_BASE, kbDefinition.getInsertGraph(), tripleDefinition);
    log.info("SPARQL query: \n" + sparqlQuery);

    UpdateRequest query = UpdateFactory.create(sparqlQuery);
    UpdateProcessor queryExecution = UpdateExecutionFactory.createRemote(query, kbDefinition.getSparqlEndpoint());

    queryExecution.execute();
  }

  private String checkOrGenerateUrl(URI baseURI, URI uri) throws KBProxyException {
    if (uri == null) {
      return combineURI(baseURI, UUID.randomUUID().toString());
    } else {
      String uriString;
      if (uri.isAbsolute()) {
        uriString = uri.toString();
      }
      else {
        uriString = combineURI(baseURI, uri.toString());
      }

      String sparqlQuery = String.format(INSERT_CHECK, uriString);
      log.info("SPARQL query: \n" + sparqlQuery);

      org.apache.jena.query.Query query = QueryFactory.create(sparqlQuery);
      QueryExecution queryExecution = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), query);

      ResultSet resultSet = queryExecution.execSelect();
      if (resultSet.hasNext()) {
        throw new KBProxyException("The knowledge base " + kbDefinition.getName() + " already contains a resource with url: " + uriString);
      }

      return uriString;
    }
  }

  private String combineURI(URI baseUri, String uri) {
    String baseString = baseUri.toString();
    if (baseString.charAt(baseString.length() - 1) == '/') {
      return baseString + uri;
    }
    else {
      return baseString + "/" + uri;
    }
  }

  private boolean isNullOrEmpty(String string){
    return string == null || string.isEmpty();
  }

  @SuppressWarnings("unchecked")
  private List<Entity> queryEntityCandidates(String content, String sparqlQuery, String... types)
      throws KBProxyException {
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
          sparqlQuery = createRegexQuery(content, null, types);
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
            if (KBProxyUtils.contains(kbDefinition.getPredicateType(), attr.getRelationURI()) &&
                !ec.hasType(attr.getValueURI())) {
              ec.addType(new Clazz(attr.getValueURI(), attr.getValue()));
            }
          }
          result.add(ec);
        }

        cacheEntity.cache(queryCache, result, AUTO_COMMIT);
        log.debug("QUERY (entities, cache save)=" + queryCache + "|" + queryCache);
      } catch (Exception e) {
        throw new KBProxyException(e);
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
  private void adjustValueOfURLResource(Attribute attr) throws KBProxyException {
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
          throw new KBProxyException(e);
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
  public List<Attribute> findAttributesOfEntities(Entity ec) throws KBProxyException {
    return findAttributes(ec.getId(), cacheEntity);
  }

  @SuppressWarnings("unchecked")
  private List<Attribute> findAttributes(String id, SolrCache cache) throws KBProxyException {
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
  public List<Attribute> findAttributesOfClazz(String clazzId) throws KBProxyException {
    return findAttributes(clazzId, cacheEntity);
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId) throws KBProxyException {
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
