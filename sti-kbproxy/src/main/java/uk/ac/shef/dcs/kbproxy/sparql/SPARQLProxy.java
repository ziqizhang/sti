package uk.ac.shef.dcs.kbproxy.sparql;

import javafx.util.Pair;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
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

public abstract class SPARQLProxy extends KBProxy {

  private static final String INSERT_BASE = "INSERT DATA {GRAPH <%1$s> {%2$s .}}";

  private static final String SPARQL_VARIABLE_SUBJECT = "?subject";
  private static final String SPARQL_VARIABLE_PREDICATE = "?predicate";
  protected static final String SPARQL_VARIABLE_OBJECT = "?object";
  private static final String SPARQL_VARIABLE_CLASS = "?class";

  private static final String SPARQL_PREDICATE_TYPE = createSPARQLResource(RDF.type.getURI());
  private static final String SPARQL_PREDICATE_BIF_CONTAINS = "<bif:contains>";

  private static final String SPARQL_FILTER_REGEX = "regex (str(?o), %1$s, \"i\")";

  private static final String SPARQL_STRING_LITERAL = "\"%1$s\"";
  private static final String SPARQL_RESOURCE = "<%1$s>";

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
                     String cachesBasePath) throws IOException, KBProxyException {
    super(kbDefinition, fuzzyKeywords, cachesBasePath);

    if (kbDefinition.getPredicateLabel().size() == 0) {
      throw new KBProxyException("KB definition contains no label predicates.");
    }
  }

  /**
   * Example fulltext query:
   * select distinct ?Subject ?Object where
   * {
   *   {?Subject <http://xmlns.com/foaf/0.1/name> ?Object . ?Object <bif:contains> "Obama"} UNION {?Subject <http://www.w3.org/2000/01/rdf-schema#label> ?Object . ?Object <bif:contains> "Obama"} .
   *   {?Subject a <http://dbpedia.org/ontology/NaturalPlace>} UNION {?Subject a <http://dbpedia.org/ontology/WrittenWork>} .
   *   ?Subject a ?Class .
   *   {?Class a <http://www.w3.org/2002/07/owl#Class>} UNION {?Class a <http://www.w3.org/2000/01/rdf-schema#class>} .
   * } LIMIT 100
   *
   * Example regex query
   * select distinct ?Subject ?Object where
   * {
   *   {?Subject <http://xmlns.com/foaf/0.1/name> ?Object} UNION {?Subject <http://www.w3.org/2000/01/rdf-schema#label> ?Object} .
   *   {?Subject a <http://dbpedia.org/ontology/NaturalPlace>} UNION {?Subject a <http://dbpedia.org/ontology/WrittenWork>} .
   *   ?Subject a ?Class .
   *   {?Class a <http://www.w3.org/2002/07/owl#Class>} UNION {?Class a <http://www.w3.org/2000/01/rdf-schema#class>} .
   *   filter regex (str(?Object), "Obama", "i")
   * } LIMIT 100
   *
   * @param content string to search
   * @param limit maximum number of items to return
   * @param types restricts the result types
   * @return SPARQL select query
   * @throws ParseException Invalid regex expression
   * @throws KBProxyException Invalid KB definition
   */
  protected Query createRegexQuery(String content, Integer limit, String... types) throws ParseException, KBProxyException {
    SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_SUBJECT, SPARQL_VARIABLE_OBJECT);

    if (limit != null){
      builder = builder.setLimit(limit);
    }

    // Label conditions
    SelectBuilder subBuilder = new SelectBuilder().addVar(SPARQL_VARIABLE_SUBJECT).addVar(SPARQL_VARIABLE_OBJECT);
    for (String labelPredicate : kbDefinition.getPredicateLabel()) {
      SelectBuilder unionBuilder = new SelectBuilder();
      unionBuilder = unionBuilder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(labelPredicate), SPARQL_VARIABLE_OBJECT);

      if (kbDefinition.getUseBifContains()) {
        unionBuilder = unionBuilder.addWhere(SPARQL_VARIABLE_OBJECT, SPARQL_PREDICATE_BIF_CONTAINS, createSPARQLLiteral(content, true));
      }
      subBuilder = subBuilder.addUnion(unionBuilder);
    }
    builder = builder.addSubQuery(subBuilder);

    if (!kbDefinition.getUseBifContains()) {
      String regexFilter = String.format(SPARQL_FILTER_REGEX, createSPARQLLiteral(content));
      builder = builder.addFilter(regexFilter);
    }

    // Types restriction
    if (types.length > 0) {
      subBuilder = new SelectBuilder().addVar(SPARQL_VARIABLE_SUBJECT);
      for (String type : types) {
        SelectBuilder unionBuilder = new SelectBuilder();
        unionBuilder = unionBuilder.addWhere(SPARQL_VARIABLE_SUBJECT, SPARQL_PREDICATE_TYPE, createSPARQLResource(type));
        subBuilder = subBuilder.addUnion(unionBuilder);
      }
      builder = builder.addSubQuery(subBuilder);
    }

    // Class restriction
    builder = addClassRestriction(builder);

    return builder.build();
  }

  protected Query createExactMatchQueries(String content) {
    SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_SUBJECT);

    // Label restriction
    builder = addLabelRestriction(content, builder);

    // Class restriction
    builder = addClassRestriction(builder);

    return builder.build();
  }

  protected Query createExactMatchWithOptionalTypes(String content) {
    SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_SUBJECT);

    // Label restriction
    builder = addLabelRestriction(content, builder);

    // Class restriction
    builder = addClassRestriction(builder);

    // Optional type
    builder = builder.addOptional(SPARQL_VARIABLE_SUBJECT, SPARQL_PREDICATE_TYPE, SPARQL_VARIABLE_OBJECT);

    return builder.build();
  }

  protected Query createGetLabelQuery(String resourceUrl) {
    resourceUrl = resourceUrl.replaceAll("\\s+", "");

    SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_OBJECT);
    for (String labelPredicate : kbDefinition.getPredicateLabel()) {
      SelectBuilder unionBuilder = new SelectBuilder();
      unionBuilder = unionBuilder.addWhere(createSPARQLResource(resourceUrl), createSPARQLResource(labelPredicate), SPARQL_VARIABLE_OBJECT);

      builder = builder.addUnion(unionBuilder);
    }

    return builder.build();
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
   * @param label
   * @return
   */
  protected List<Pair<String, String>> queryByLabel(Query sparqlQuery, String label) {
    log.info("SPARQL query: \n" + sparqlQuery.toString());

    QueryExecution execution = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), sparqlQuery);

    List<Pair<String, String>> result = new ArrayList<>();
    ResultSet rs = execution.execSelect();
    while (rs.hasNext()) {

      QuerySolution qs = rs.next();
      RDFNode subject = qs.get(SPARQL_VARIABLE_SUBJECT);
      RDFNode object = qs.get(SPARQL_VARIABLE_OBJECT);
      result.add(new Pair<>(subject.toString(), object != null ? object.toString() : label));
    }
    return result;
  }

  protected boolean Ask(Query sparqlQuery) {
    log.info("SPARQL query: \n" + sparqlQuery.toString());

    QueryExecution queryExecution = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), sparqlQuery);

    return queryExecution.execAsk();
  }

  protected abstract List<String> queryForLabel(Query sparqlQuery, String resourceURI) throws KBProxyException;

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

    candidates.sort((o1, o2) -> {
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

      Query query = createRegexQuery(pattern, limit);
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
    final Query sparqlQuery;
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

      AskBuilder builder = new AskBuilder().addWhere("<" + uriString + ">", SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT);

      Query query = builder.build();
      log.info("SPARQL query: \n" + query.toString());

      QueryExecution queryExecution = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), query);

      boolean exists = queryExecution.execAsk();
      if (exists) {
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
  private List<Entity> queryEntityCandidates(String content, Query sparqlQuery, String... types)
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
          Query sparqlQuery = createGetLabelQuery(value);
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

      SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT)
              .addWhere(createSPARQLResource(id), SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT);


      Query query = builder.build();
      QueryExecution qexec = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), query);

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

  private SelectBuilder addLabelRestriction(String content, SelectBuilder builder) {
    SelectBuilder subBuilder = new SelectBuilder().addVar(SPARQL_VARIABLE_SUBJECT);
    for (String labelPredicate : kbDefinition.getPredicateLabel()) {
      SelectBuilder unionBuilder = new SelectBuilder();
      unionBuilder = unionBuilder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(labelPredicate), createSPARQLLiteral(content, false, true));

      subBuilder = subBuilder.addUnion(unionBuilder);
    }
    return builder.addSubQuery(subBuilder);
  }

  private SelectBuilder addClassRestriction(SelectBuilder builder) {
    if (kbDefinition.getStructureClass().size() > 0) {
      builder = builder.addWhere(SPARQL_VARIABLE_SUBJECT, SPARQL_PREDICATE_TYPE, SPARQL_VARIABLE_CLASS);

      SelectBuilder subBuilder = new SelectBuilder().addVar(SPARQL_VARIABLE_CLASS);
      for (String kbClass : kbDefinition.getStructureClass()) {
        SelectBuilder unionBuilder = new SelectBuilder();
        unionBuilder = unionBuilder.addWhere(SPARQL_VARIABLE_CLASS, SPARQL_PREDICATE_TYPE, createSPARQLResource(kbClass));
        subBuilder = subBuilder.addUnion(unionBuilder);
      }
      builder = builder.addSubQuery(subBuilder);
    }

    return builder;
  }

  private SelectBuilder getSelectBuilder(String... variables) {
    SelectBuilder builder = new SelectBuilder();

    builder = builder.setDistinct(true);

    for (String variable : variables) {
      builder = builder.addVar(variable);
    }

    return builder;
  }

  private static String createSPARQLResource(String url) {
    return String.format(SPARQL_RESOURCE, url);
  }

  private String createSPARQLLiteral(String value, boolean extraQuotes, boolean addLanguageSuffix) {
    String result = String.format(SPARQL_STRING_LITERAL, escapeSPARQLLiteral(value));

    if (extraQuotes) {
      result = "'" + result + "'";
    }

    if (addLanguageSuffix){
      result += kbDefinition.getLanguageSuffix();
    }

    return result;
  }

  private String createSPARQLLiteral(String value, boolean extraQuotes) {
    return  createSPARQLLiteral(value, extraQuotes, false);
  }

  private String createSPARQLLiteral(String value) {
    return  createSPARQLLiteral(value, false);
  }

  private static String escapeSPARQLLiteral(String value){
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
