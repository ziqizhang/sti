package uk.ac.shef.dcs.kbproxy.sparql;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by - on 10/06/2016.
 */
public class DBpediaProxy extends SPARQLProxy {

  private static final String DBP_SPARQL_ENDPOINT = "dbp.sparql.endpoint";
  private static final String DBP_ONTOLOGY_URL = "dbp.ontology.url";

  private OntModel ontology;

  /**
   * @param fuzzyKeywords   given a query string, kbproxy will firstly try to fetch results matching the exact query. when no match is
   *                        found, you can set fuzzyKeywords to true, to let kbproxy to break the query string based on conjunective words.
   *                        So if the query string is "tom and jerry", it will try "tom" and "jerry"
   * @param cachesBasePath  Base path for the initialized solr caches.
   * @throws IOException
   */
  public DBpediaProxy(KBDefinition kbDefinition,
                      Boolean fuzzyKeywords,
                      String cachesBasePath) throws IOException, KBProxyException {
    super(kbDefinition, fuzzyKeywords, cachesBasePath);
    String ontologyURL = kbDefinition.getOntologyUri();
    if (ontologyURL != null) {
      ontology = loadModel(ontologyURL);
    }
    resultFilter = new DBpediaSearchResultFilter(kbDefinition.getStopListFile());
  }

  @Override
  protected List<String> queryForLabel(Query sparqlQuery, String resourceURI) throws KBProxyException {
    try {
      QueryExecution qexec = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), sparqlQuery);

      List<String> out = new ArrayList<>();
      ResultSet rs = qexec.execSelect();
      while (rs.hasNext()) {
        QuerySolution qs = rs.next();
        RDFNode domain = qs.get(SPARQL_VARIABLE_OBJECT);
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
    } catch (QueryParseException ex) {
      throw new KBProxyException("Invalid query: " + sparqlQuery, ex);
    }
  }

  private OntModel loadModel(String ontURL) {
    OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    base.read(ontURL);
    return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);
  }
}
