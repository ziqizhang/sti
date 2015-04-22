package uk.ac.shef.dcs.oak.lodietest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinderSolr;

import java.io.IOException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 28/02/13
 * Time: 16:30
 */
public class TestAny {
    public static void main(String[] args) throws IOException, SolrServerException {
        Model m = ModelFactory.createDefaultModel().read("http://dbpedia.org/ontology/");
        StmtIterator it = m.listStatements();
        while (it.hasNext())
            System.out.println(it.next());
        System.out.println(m);
        /*SolrServer server = new HttpSolrServer("http://localhost:7777/solr");
        QueryCache cache = new QueryCache("D:\\work\\billiontripleindex\\solrindex_cache\\zookeeper\\solr","collection1");
        EntityFinderSolr eFinder = new EntityFinderSolr(cache,server);
        try {
            eFinder.findCandidates("monkey");

            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolrServerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/

        /*SolrServer server = new HttpSolrServer("http://localhost:7777/solr");
        QueryCache cache = new QueryCache("D:\\work\\billiontripleindex\\solrindex_cache\\zookeeper\\solr", "collection1");
        EntityLabelFinderSolr finder = new EntityLabelFinderSolr(cache, server);
        try {
            finder.findCandidates("http://dbpedia.org/resource/Dillon,_Virginia");

            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolrServerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/


        // ConceptFinder cFinder = new ConceptFinderSparql("http://sparql.sindice.com/sparql");
        /*File configFile = new File("D:\\work\\billiontripleindex\\solrindex_schema\\zookeeper\\solr" + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer("D:\\work\\billiontripleindex\\solrindex_schema\\zookeeper\\solr",
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, SolrSearchSchemaIndexProxy.SCHEMA_INDEX_CORE_CLASS);
        ConceptLabelFinderSolr finder = new ConceptLabelFinderSolr(null, server);
        finder.findCandidates("http://purl.org/acco/ns#CompoundPrice");
        server.shutdown();

        QueryCache cache = new QueryCache("D:\\work\\billiontripleindex\\solrindex_cache\\zookeeper\\solr",
                "collection1");*/
        /*CandidateFinder cFinder = new ConceptFinderSparql(cache, "http://sparql.sindice.com/sparql");
        System.out.println(cFinder.findCandidates("species"));*//*

        *//*CandidateFinder eFinder = new EntityFinderSparql(cache, "http://sparql.sindice.com/sparql");
        System.out.println(eFinder.findCandidates("Monkey Test Dog And Cat"));*/

        /*CandidateFinder eFinder = new PropertyFinderSparql(cache, "http://sparql.sindice.com/sparql");
                System.out.println(eFinder.findCandidates("place of birth"));
        cache.shutdown();*/

        /* CandidateFinder eFinder = new LabelFinderSparql(cache, "http://sparql.sindice.com/sparql");
        System.out.println(eFinder.findCandidates("http://dbpedia.org/property/classis"));
        cache.shutdown();*/

        QueryCache cache = new QueryCache("D:\\work\\billiontripleindex\\solrindex_cache\\zookeeper\\solr", "collection1");
        /*RelationFinder rFinder = new RelationFinderSparql(cache,"http://sparql.sindice.com/sparql");
        System.out.println(rFinder.findRelationBetween("<http://dbpedia.org/resource/Monkey>", "<http://dbpedia.org/ontology/Mammal>"));
        cache.shutdown();
*/

        SolrServer server = new HttpSolrServer("http://localhost:8983/solr");

        RelationFinder finder = new RelationFinderSolr(cache, server);
        try {
            finder.findRelationBetween("<http://eunis.eea.europa.eu/species/175275>", "<http://eunis.eea.europa.eu/sites/PLH060097>");
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolrServerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
