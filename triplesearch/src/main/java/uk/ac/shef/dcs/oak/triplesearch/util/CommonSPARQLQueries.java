package uk.ac.shef.dcs.oak.triplesearch.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 28/02/13
 * Time: 15:25
 */
public class CommonSPARQLQueries {

    public static final String[] URI_FOR_CLASS = {
            "http://www.w3.org/2000/01/rdf-schema#Class",
            "http://www.w3.org/2002/07/owl#Class"
    };

    public static StringBuilder createCommonNameSpaces() {
        StringBuilder query = new StringBuilder();
        query.append("PREFIX dbpprop: <http://dbpedia.org/property/>\n").
                append("PREFIX dbpedia: <http://dbpedia.org/resource/>\n").
                append("PREFIX dbpedia-owl: <http://dbpedia.org/> \n").
                append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n").
                append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n").
                append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n").
                append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n").
                append("PREFIX dc: <http://purl.org/dc/elements/1.1/>\n").
                append("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n").
                append("PREFIX fb: <http://rdf.freebase.com/ns/>\n").
                append("PREFIX basekb: <http://rdf.basekb.com/ns/>\n").
                append("PREFIX public: <http://rdf.basekb.com/public/>\n");
        return query;
    }

    public static List<String> createQueryBatchFindLabelsOfResource(String uri) {
        List<String> rs = new ArrayList<String>();

        StringBuilder query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("rdfs:label ?o .").append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("foaf:name ?o .").append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("dc:title ?o .").append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("skos:prefLabel ?o .").append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("skos:altLabel ?o .").append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("fb:type.object.name ?o .").append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("fb:m.06b ?o .").append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("basekb:type.object.name ?o .").append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?o WHERE {").append("\n").
                append("<").append(uri).append("> ").
                append("basekb:m.06b ?o .").append("}");
        rs.add(query.toString());

        return rs;
    }

    /**
     * create sparql query to fetch entities (entity must be ?s in the triple position)
     * WARNING: DO NOT USE THIS ON SINDICE. this query must be split into subparts otherwise will crash server
     *
     * @param keyword
     * @return
     */
    public static String createQueryFindEntityWithKeyword(String keyword) {
        StringBuilder query = createCommonNameSpaces();

        query.append("SELECT DISTINCT ?s WHERE {").append("\n").
                append("?s rdf:type ?o .").append("\n").
                append("?o rdf:type owl:Class .").append("\n").
                append("{?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").   //unlikely to fetch and freebase data because no FB "type" expressed as "owl:Class"
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n");

        //CamelCase
        String keywordCap = StringUtils.capitalize(keyword);
        if (!keyword.equals(keywordCap)) {
            keyword = keywordCap;
            query.append("UNION {?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n");
        }
        query.append("}");

        return query.toString();
    }


    public static List<String> createQueryBatchFindEntityWithKeyword_and_Superclasses_and_Equiv(String keyword) {
        List<String> queries = new ArrayList<String>();
        createQueryBatchFindEntityWithKeyword_and_Superclasses_and_Equiv(keyword, "owl:Class", queries);
        //createQueryBatchFindEntityWithKeyword(keyword, "rdfs:Class", queries);
        return queries;
    }

    public static void createQueryBatchFindEntityWithKeyword_and_Superclasses_and_Equiv(String keyword, String classURI, List<String> result) {
        StringBuilder query = createCommonNameSpaces();

        query.append("SELECT DISTINCT ?s ?o ?e WHERE {").append("\n").
                append("?s rdf:type ?o .").append("\n").
                //append("?o rdf:type " + classURI + " .").append("\n").
                        append("{?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?o owl:equivalentClass ?e .}").
                append("}");
        result.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o ?e WHERE {").append("\n").
                append("?s rdf:type ?o .").append("\n").
                //append("?o rdf:type " + classURI + " .").append("\n").
                        append("{?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?o owl:equivalentClass ?e .}").
                append("}");
        result.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o ?e WHERE {").append("\n").
                append("?s rdf:type ?o .").append("\n").
                //append("?o rdf:type " + classURI + " .").append("\n").
                        append("{?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?o owl:equivalentClass ?e .}").
                append("}");
        result.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o ?e WHERE {").append("\n").
                append("?s rdf:type ?o .").append("\n").
                //append("?o rdf:type " + classURI + " .").append("\n").
                        append("{?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?o owl:equivalentClass ?e .}").
                append("}");
        result.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o ?e WHERE {").append("\n").
                append("?s rdf:type ?o .").append("\n").
                // append("?o rdf:type " + classURI + " .").append("\n").
                        append("{?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?o owl:equivalentClass ?e .}").
                append("}");
        result.add(query.toString());


        //CamelCase
        String keywordCap = StringUtils.capitalize(keyword);
        if (!keyword.equals(keywordCap)) {
            keyword = keywordCap;
            createQueryBatchFindEntityWithKeyword_and_Superclasses_and_Equiv(keyword, classURI, result);
        }
    }

    /**
     * See the query for details
     * <p/>
     * in theory this query gets all concepts matching a keyword, regardless of whether that concept has instances in LD
     * <p/>
     * ONLY FETCHES "OWL:CLASS", NOT "RDFS:CLASS"
     *
     * @param keyword
     * @return
     */
    public static String createQueryFindClassWithKeywordTBox(String keyword) {
        StringBuilder query = createCommonNameSpaces();

        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:Class .").append("\n").
                append("{?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").   //unlikely to fetch and freebase data because no FB "type" expressed as "owl:Class"
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n");

        //CamelCase
        String keywordCap = StringUtils.capitalize(keyword);
        if (!keyword.equals(keywordCap)) {
            keyword = keywordCap;
            query.append("UNION {?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n");
        }
        query.append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        return query.toString();
    }

    /**
     * See the query for details
     * <p/>
     * in theory this query gets all concepts matching a keyword, regardless of whether that concept has instances in LD
     * <p/>
     * <p/>
     * Does the similar job as "createQueryFindClassWithKeywordTBox", difference is that also fetches "RDFS:CLASS"
     * and splits the single big query to several, smaller queries.
     *
     * @param keyword
     * @return
     */
    public static List<String> createQueryBatchFindClassAndEquivWithKeywordTBox(String keyword) {
        List<String> rs = new ArrayList<String>();
        StringBuilder query = createCommonNameSpaces();

        //rdfs:label
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:Class .").append("\n").
                append("{?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdfs:Class .").append("\n").
                append("{?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());

        //foaf:name
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:Class .").append("\n").
                append("{?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdfs:Class .").append("\n").
                append("{?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());

        //dc:title
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:Class .").append("\n").
                append("{?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdfs:Class .").append("\n").
                append("{?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());

        //skos:prefLabl/altLabel
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:Class .").append("\n").
                append("{?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdfs:Class .").append("\n").
                append("{?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());

        //freebase
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:Class .").append("\n").
                append("{?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdfs:Class .").append("\n").
                append("{?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentClass ?o .}\n").
                append("}");
        rs.add(query.toString());

        //CamelCase
        String keywordCap = StringUtils.capitalize(keyword);
        if (!keyword.equals(keywordCap)) {
            keyword = keywordCap;
            rs.addAll(createQueryBatchFindClassAndEquivWithKeywordTBox(keyword));
        }

        return rs;
    }

    /**
     * See the query for details
     * <p/>
     * In theory this query gets only concepts which have instances in the LD
     *
     * @param keyword
     * @return
     */
    public static String createQueryFindClassWithKeywordABox(String keyword) {
        StringBuilder query = createCommonNameSpaces();

        query.append("SELECT DISTINCT ?o ?n WHERE {").append("\n").
                append("?s a ?o.").append("\n").
                append("{?o rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?o rdfs:label \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?o foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?o foaf:name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?o dc:title \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?o dc:title \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?o skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?o skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?o skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?o skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?o fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?o fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n");

        //CamelCase
        String keywordCap = StringUtils.capitalize(keyword);
        if (!keyword.equals(keywordCap)) {
            keyword = keywordCap;
            query.append("UNION {?o rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?o rdfs:label \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?o foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?o foaf:name \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?o dc:title \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?o dc:title \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?o skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?o skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?o skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?o skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?o fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?o fb:type.object.name \"").append(keyword).append("\" ").append("}\n").
                    append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                    append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                    append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n");
            ;
        }
        query.append("OPTIONAL {?s owl:equivalentClass ?n .}\n").
                append("}");
        return query.toString();
    }

    public static List<String> createQueryBatchFindPropertyAndEquivWithKeywordTBox(String keyword) {
        List<String> rs = new ArrayList<String>();
        StringBuilder query = createCommonNameSpaces();

        //rdfs:label
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdf:Property .").append("\n").
                append("{?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:ObjectProperty .").append("\n").
                append("{?s rdfs:label \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s rdfs:label \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());

        //foaf:name
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdf:Property .").append("\n").
                append("{?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:ObjectProperty .").append("\n").
                append("{?s foaf:name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s foaf:name \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());

        //dc:title
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdf:Property .").append("\n").
                append("{?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:ObjectProperty .").append("\n").
                append("{?s dc:title \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s dc:title \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());

        //skos:prefLabl/altLabel
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:ObjectProperty .").append("\n").
                append("{?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdf:Property .").append("\n").
                append("{?s skos:prefLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:prefLabel \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s skos:altLabel \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());

        //freebase
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type owl:ObjectProperty .").append("\n").
                append("{?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());

        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?s ?o WHERE {").append("\n").
                append("?s rdf:type rdf:Property .").append("\n").
                append("{?s fb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s fb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:type.object.name \"").append(keyword).append("\" .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\"@en .").append("}\n").
                append("UNION {?s basekb:m.06b \"").append(keyword).append("\" .").append("}\n").
                append("OPTIONAL {?s owl:equivalentProperty ?o .}\n").
                append("}");
        rs.add(query.toString());

        //CamelCase
        String keywordCap = StringUtils.capitalize(keyword);
        if (!keyword.equals(keywordCap)) {
            keyword = keywordCap;
            rs.addAll(createQueryBatchFindClassAndEquivWithKeywordTBox(keyword));
        }

        return rs;
    }

    public static String createQueryFindRelationBetween(String uri1, String uri2) {
        uri1 = "<" + uri1 + ">";
        uri2 = "<" + uri2 + ">";
        StringBuilder query;
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?p WHERE {").append("\n").
                append(uri1).append(" ?p ").
                append(uri2).
                append("}");
        return query.toString();
    }

    public static String createQueryFindPredicatesOfObjectAndPredDomain(String key) {
        StringBuilder query;
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?p ?d ?e WHERE {").append("\n").
                append("?s ?p ").append(key).append(". \n").
                append("OPTIONAL {?p rdfs:domain ?d} \n").
                append("OPTIONAL {?d owl:equivalentClass ?e}\n").
                append("}");
        return query.toString();
    }

    public static String createQueryFindSharedDomainsBetweenProperties(String uri1, String uri2) {
        uri1 = "<" + uri1 + ">";
        uri2 = "<" + uri2 + ">";
        StringBuilder query;
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?d WHERE {").append("\n").
                append(uri1).append(" rdfs:domain ?d .\n").
                append(uri2).append(" rdfs:domain ?d .\n").
                append("}");
        return query.toString();
    }

    public static String createQueryFindDomainOfProperty(String keyword) {
        keyword = "<" + keyword + ">";

        StringBuilder query;
        query = createCommonNameSpaces();
        query.append("SELECT DISTINCT ?d WHERE {").append("\n").
                append(keyword).append(" rdfs:domain ?d \n").
                append("}");
        return query.toString();
    }

    public static String createQueryIsEquivalent(String iUri, String jUri) {
        return "ASK {<" + iUri + "> <http://www.w3.org/2002/07/owl#equivalentClass> <" + jUri + ">}";
    }


    public static String createQueryIsSubClassOf(String aStr, String bStr) {
        return "ASK {<" + aStr + "> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <" + bStr + ">}";
    }

    /**
     * Because of the STUPID YAGO data we need to make a spelling error in the query to get the fucking data!!!
     *
     * @param aStr
     * @param bStr
     * @return
     */
    public static String createQueryIsSubClassOfStupidYAGO(String aStr, String bStr) {
        return "ASK {<" + aStr + "> <http://www.w3.org/2000/01/rdf-schema#suBClassOf> <" + bStr + ">}";
    }
}
