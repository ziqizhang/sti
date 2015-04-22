package uk.ac.shef.dcs.oak.lodietest;

import uk.ac.shef.dcs.oak.lodie.architecture.Dataset;
import uk.ac.shef.dcs.oak.lodie.architecture.Document;
import uk.ac.shef.dcs.oak.lodie.architecture.LearningJob;
import uk.ac.shef.dcs.oak.lodie.architecture.LodieException;
import uk.ac.shef.dcs.oak.lodie.seeding.SeedIdentifierSindice;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 24/10/12
 * Time: 16:06
 */
public class TestSeedIdentifier {

    public static void main(String[] args) throws LodieException {
        SeedIdentifierSindice identifier = new SeedIdentifierSindice(5, 1, 600000, 8, "RDFA","MICRODATA","MICROFORMAT");

        //defining the learning job to be "learn instances of Airline and BasketballLeague"
        LearningJob job = new LearningJob("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",true,false);
        job.getConstraints().add("<http://dbpedia.org/ontology/Airline>");
        job.getConstraints().add("<http://dbpedia.org/ontology/BasketballLeague>");

        Dataset<Document> docs = identifier.findDSeed(job);

        for(Document doc: docs){
            Document<LTable> docWithTables = (Document<LTable>)doc;
            List<LTable> extractedTables=docWithTables.getContent();

            //for each table, index bg knowledge
        }

        System.out.println();
    }
}
