/*
package uk.ac.shef.dcs.oak.lodie.test;

import com.google.api.client.http.HttpResponseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.content.KBSearcher_Freebase;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret.*;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.io.LTableAnnotationWriter;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.selector.LTableContentCell_Sampler_nonEmpty;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.selector.LTableContentRow_Sampler_nonEmpty;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.stopping.EntropyConvergence;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.util.FileUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

*/
/**
 *//*

public class TestTableInterpretation {

    private static Logger log = Logger.getLogger(TestTableInterpretation.class.getName());

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {

        String nlpResources = "D:\\Work\\lodie\\resources\\nlp_resources";
        //cache target location
        String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";
        File configFile = new File(cacheFolder + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolder,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");

        //object to fetch things from KB
        String freebaseProperties = "D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties";
        KBSearcher_Freebase freebaseMatcher = new KBSearcher_Freebase(freebaseProperties, server, true);

        //object to find main subject column
        MainColumnFinder main_col_finder = new MainColumnFinder(
                new LTableContentRow_Sampler_nonEmpty(),
                EntropyConvergence.class.getName(),
                new String[]{"0.0", "1", "0.01"},
                server,
                nlpResources,
                "fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0=");


        //stop words and stop properties (freebase) are used for disambiguation
        //List<String> stopProperties = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stopproperties_freebase.txt", true);
        List<String> stopWords = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stoplist.txt", true);
        //object to score columns, and disambiguate entities
        Disambiguator disambiguator = new Disambiguator(freebaseMatcher, new DisambiguationScorer_Overlap(
                stopWords,
                new double[]{1.0, 0.5, 0.25, 1.0},
                nlpResources));
        ClassificationScorer class_scorer = new ClassificationScorer_Vote(nlpResources,
                new Creator_ConceptHierarchicalBOW_Freebase(),
                stopWords,
                new double[]{1.0, 1.0, 0.5, 1.0, 0.25}
        );

        ColumnLearner_LEARN column_learner = new ColumnLearner_LEARN(
                new LTableContentCell_Sampler_nonEmpty(),
                EntropyConvergence.class.getName(),
                new String[]{"0.0", "2", "0.01"},
                freebaseMatcher,
                disambiguator,
                class_scorer
        );

        ColumnLearner_UPDATE column_updater = new ColumnLearner_UPDATE(
                freebaseMatcher, disambiguator, class_scorer
        );


        ColumnInterpreter columnInterpreter = new ColumnInterpreter(column_learner, column_updater,
                TableMinerConstants.MAX_REFERENCE_ENTITY_FOR_DISAMBIGUATION);

        //object to interpret relations between columns
        HeaderBinaryRelationScorer relation_scorer = new HeaderBinaryRelationScorer_Vote(nlpResources,
                new Creator_RelationHierarchicalBOW_Freebase(),
                stopWords,
                new double[]{1.0, 1.0, 0.5, 1.0, 0.25}
        );
        BinaryRelationInterpreter interpreter_relation = new BinaryRelationInterpreter(
                new RelationTextMatch_Scorer(0.0, stopWords),
                relation_scorer
        );

        //object to consolidate previous output, further score columns and disamgiuate entities
        ColumnInterpreter_relDepend interpreter_with_knownRelations = new ColumnInterpreter_relDepend_v2(
                freebaseMatcher,
                class_scorer,
                column_updater,
                TableMinerConstants.MAX_REFERENCE_ENTITY_FOR_DISAMBIGUATION
        );


        MainInterpreter interpreter = new MainInterpreter(
                main_col_finder,
                columnInterpreter,
                interpreter_with_knownRelations,
                interpreter_relation,
                new int[0], new int[0]);

        LTableAnnotationWriter writer = new LTableAnnotationWriter(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));

        List<String> tasks =
                FileUtils.readList("E:\\Data\\table annotation\\corpus_analysis/100_tables_for_studying_subject_columns_(noise_removed).csv", false);
        int count = 0;
        int[] keep = new int[]{35,68,69,71};
        try {
            for (String task : tasks) {
                count++;

                boolean found = false;
                for (int k : keep) {
                    if (count == k) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    continue;

                int lastComma = task.lastIndexOf(",");
                String sourceTableFile = task.substring(0, lastComma).trim();
                if (sourceTableFile.startsWith("\"") && sourceTableFile.endsWith("\""))
                    sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length() - 1).trim();
                System.out.println(count + "_" + sourceTableFile + " " + new Date());
                log.info(">>>" + count + "_" + sourceTableFile);
                LTable table = LimayeDatasetLoader.readTable(sourceTableFile, null, null);
                boolean complete = process(interpreter, table, sourceTableFile, writer, "E:\\Data\\table annotation\\corpus_analysis\\100_tables_annotated");
                if (!complete)
                    System.out.println("\t\t\t missed: " + count+"_"+sourceTableFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            server.shutdown();
            System.exit(1);
        }

        server.shutdown();
    }

    public static boolean process(MainInterpreter interpreter, LTable table, String sourceTableFile, LTableAnnotationWriter writer,
                                  String outFolder) throws FileNotFoundException {
        String outFilename = sourceTableFile.replaceAll("\\\\", "/");
        try {
            LTableAnnotation annotations = interpreter.start(table);

            int startIndex = outFilename.lastIndexOf("/");
            if (startIndex != -1) {
                outFilename = outFilename.substring(startIndex + 1).trim();
            }
            writer.writeHTML(table, annotations, outFolder + "/" + outFilename + ".html");

        } catch (Exception ste) {
            if (ste instanceof SocketTimeoutException || ste instanceof HttpResponseException) {
                ste.printStackTrace();
                System.out.println("Remote server timed out, continue 10 seconds. Missed." + outFilename);
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                }
                return false;
            } else
                ste.printStackTrace();

        }
        return true;
    }
}
*/
