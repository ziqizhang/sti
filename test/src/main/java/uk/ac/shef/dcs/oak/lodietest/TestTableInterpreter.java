package uk.ac.shef.dcs.oak.lodietest;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.TableInterpreter;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter.FilterPolicyNumericValues;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.concept.ConceptFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.concept.ConceptFinderSolr;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.concept.ConceptFinderSparql;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity.EntityFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity.EntityFinderSolr;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity.EntityFinderSparql;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label.*;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property.*;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinderSolr;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinderSparql;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass.EntitySuperConceptFinderSolr;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass.EntitySuperConceptFinderSparql;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass.SuperConceptFinder;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodietest.experiment.table.gs.LimayeDatasetLoader;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchSchemaIndexProxy;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 12/03/13
 * Time: 12:09
 */
public class TestTableInterpreter {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        //create required CandidateFinder and RelationFinders
        //String endpoint="http://sparql.sindice.com/sparql";
        //String endpoint="http://galaxy.dcs.shef.ac.uk:8893/sparql";
        String endpoint = "http://dbpedia.org/sparql";

        QueryCache cache = new QueryCache("D:\\work\\billiontripleindex\\solrindex_cache\\zookeeper\\solr", "collection1");

        SolrServer instanceServer = new HttpSolrServer("http://localhost:8983/solr");
        File configFile = new File("D:\\work\\billiontripleindex\\solrindex_schema\\solr" + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer("D:\\work\\billiontripleindex\\solrindex_schema\\solr",
                configFile);
        SolrServer schemaClassServer = new EmbeddedSolrServer(container, SolrSearchSchemaIndexProxy.SCHEMA_INDEX_CORE_CLASS);
        SolrServer schemaPropertyServer = new EmbeddedSolrServer(container, SolrSearchSchemaIndexProxy.SCHEMA_INDEX_CORE_PROPERTY);
        SolrServer schemaStmtServer = new EmbeddedSolrServer(container, SolrSearchSchemaIndexProxy.SCHEMA_INDEX_CORE_STATEMENT);


        RelationFinder sparqlRelationFinder = new RelationFinderSparql(cache, endpoint);
        RelationFinder solrRelationFinderSchema = new RelationFinderSolr(cache, schemaStmtServer);
        RelationFinder solrRelationFinderInstance = new RelationFinderSolr(cache, instanceServer);

        ConceptFinder sparqlHeaderCandidateFinderFromClass = new ConceptFinderSparql(cache, endpoint);
        PropertyFinder sparqlHeaderCandidateFinderFromProperty = new PropertyFinderSparql(cache, endpoint);
        ConceptFinder solrHeaderCandidateFinderFromClass = new ConceptFinderSolr(schemaClassServer);
        PropertyFinder solrHeaderCandidateFinderFromProperty = new PropertyFinderSolr(schemaPropertyServer);

        EntityFinder sparqlCellCandidateFinder = new EntityFinderSparql(cache, endpoint);
        EntityFinder solrCellCandidateFinder = new EntityFinderSolr(cache, instanceServer);

        LabelFinder sparqlLabelFinder = new LabelFinderSparql(cache, endpoint);
        LabelFinder solrLabelFinderForClass = new ConceptLabelFinderSolr(schemaClassServer);
        LabelFinder solrLabelFinderForProperty = new PropertyLabelFinderSolr(schemaPropertyServer);
        LabelFinder solrLabelFinderForEntity = new EntityLabelFinderSolr(cache, instanceServer);

        SuperConceptFinder sparlqSuperclassFinderForEntity = new EntitySuperConceptFinderSparql(cache, endpoint);
        SuperConceptFinder solrSuperclassFinderForEntity = new EntitySuperConceptFinderSolr(cache, instanceServer);
        PredicateOfObjectAndPredDomainFinderSparql predicateOfObjectFinder = new PredicateOfObjectAndPredDomainFinderSparql(cache, endpoint);

        CandidateFinder domainOfPropFinder = new PropertyDomainFinderSparql(cache, endpoint);

        //RelationFinder relationFinder = new RelationFinderSolr(cache, instanceServer);
        RelationFinder relationFinder = new RelationFinderSparql(cache, endpoint);
        /*try {
            relationFinder.findRelationBetween("<http://eunis.eea.europa.eu/species/175275>", "<http://eunis.eea.europa.eu/sites/PLH060097>");
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/

        //create table interpreter object
        TableInterpreter interpreter = new TableInterpreter(
                new ConceptFinder[]{sparqlHeaderCandidateFinderFromClass},
                new PropertyFinder[]{sparqlHeaderCandidateFinderFromProperty},
                new EntityFinder[]{sparqlCellCandidateFinder},
                new LabelFinder[]{sparqlLabelFinder},
                new LabelFinder[]{sparqlLabelFinder},
                new LabelFinder[]{sparqlLabelFinder},
                new SuperConceptFinder[]{sparlqSuperclassFinderForEntity},
                new RelationFinder[]{relationFinder},
                new PredicateOfObjectAndPredDomainFinderSparql[]{predicateOfObjectFinder},
                new CandidateFinder[]{domainOfPropFinder});

        //load an example table
        LTable table = LimayeDatasetLoader.readTable(
                "E:\\data\\table annotation\\tablesForAnnotation\\relationGT\\c2\\r37\\l\\i\\s/List_of_Nigerian_state_capitals_1056.html_0.xml",
                "E:\\data\\table annotation\\workspace\\WWT_GroundTruth\\annotation\\relationGT\\c2\\r37\\l\\i\\s/List_of_Nigerian_state_capitals_1056.html_0.xml",
                "D:\\work\\lodiedata\\limayetable");


        interpreter.interpret(table, null, new FilterPolicyNumericValues());
        System.out.println();

        /*String cleanTableRepos = args[0];
        String annotationRepos = args[1].replaceAll("\\\\", "/");

        File[] files = FileUtils.listFilesRecursively(new File(annotationRepos), new SuffixFileFilter(".xml"));
        for (File annotated : files) {
            String path = annotated.getAbsolutePath().replaceAll("\\\\", "/");
            String relative = path.substring(annotationRepos.length());

            String cleanFile = cleanTableRepos + relative;
            if (!(new File(cleanFile)).exists()) {
                System.err.println("clean file for annotation does not exist: " + cleanFile);
                continue;
            }

LTable table = LimayeDatasetLoader.readTable(cleanFile, annotated.toString(), args[2]);

            System.out.println(cleanFile);
        }*/


        /*LTable table = readTable("E:\\data\\table annotation\\tablesForAnnotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
        "E:\\data\\table annotation\\workspace\\WWT_GroundTruth\\annotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
        "D:\\work\\lodiedata\\limayetable");*/
        System.out.println();

        System.exit(0);
        //interpret the table
    }


}
