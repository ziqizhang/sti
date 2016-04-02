package uk.ac.shef.dcs.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.any23.util.FileUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.*;
import uk.ac.shef.dcs.sti.core.feature.FreebaseConceptBoWCreator;
import uk.ac.shef.dcs.sti.core.feature.FreebaseRelationBoWCreator;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.stopping.IInf;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.sti.core.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.core.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.sampler.OSPD_nonEmpty;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.TripleGenerator;
import uk.ac.shef.dcs.sti.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.xtractor.TableNormalizerFrequentRowLength;
import uk.ac.shef.dcs.sti.xtractor.TableObjCreatorIMDB;
import uk.ac.shef.dcs.sti.xtractor.TableXtractorIMDB_noTripleContext;
import uk.ac.shef.dcs.sti.xtractor.validator.TabValGeneric;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 */
public class Test_ISWC_TableInterpretation_IMDB {
    private static Logger log = Logger.getLogger(TestTableInterpretation_IMDB.class.getName());

    public static int[] IGNORE_COLUMNS = new int[]{0, 2, 3};

    public static void main(String[] args) throws IOException {
        String inFolder = args[0];
        String outFolder = args[1];
        String propertyFile = args[2]; //"D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties"
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
        String cacheFolder = args[3];  //String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";
        String nlpResources = args[4]; //"D:\\Work\\lodie\\resources\\nlp_resources";
        int start = Integer.valueOf(args[5]);
        boolean relationLearning = Boolean.valueOf(args[6]);
        //cache target location

        File configFile = new File(cacheFolder + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolder,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");

        //object to fetch things from KB
        FreebaseSearch freebaseMatcher = new FreebaseSearch(propertyFile, true, server,null,null);
        List<String> stopWords = uk.ac.shef.dcs.sti.util.FileUtils.readList(nlpResources + "/stoplist.txt", true);
        //object to find main subject column
        SubjectColumnDetector main_col_finder = new SubjectColumnDetector(
                new TContentTContentRowRankerImpl(),
                IInf.class.getName(),
                new String[]{"0.0", "1", "0.01"},
                server,
                nlpResources, false, stopWords,
                MultiKeyStringSplitter.split(properties.getProperty("BING_API_KEYS")));


        //stop words and stop properties (freebase) are used for disambiguation
        //List<String> stopProperties = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stopproperties_freebase.txt", true);

        //object to computeElementScores columns, and disambiguate entities
        TCellDisambiguator disambiguator = new TCellDisambiguator(freebaseMatcher, new TMPEntityScorer(
                stopWords,
                new double[]{1.0, 0.5, 0.5, 1.0, 1.0}, //row,column, tablecontext other,refent, tablecontext pagetitle (unused)
                nlpResources));
        ClazzScorer class_scorer = new TMPClazzScorer(nlpResources,
                new FreebaseConceptBoWCreator(),
                stopWords,
                new double[]{1.0, 1.0, 1.0, 1.0}         //all 1.0
        );

        TContentCellRanker selector = new OSPD_nonEmpty();
        LEARNINGPreliminaryColumnClassifier column_learnerSeeding = new LEARNINGPreliminaryColumnClassifier(
                selector,
                IInf.class.getName(),
                new String[]{"0.0", "2", "0.01"},
                freebaseMatcher,
                disambiguator,
                class_scorer
        );

        LEARNINGPreliminaryDisamb column_updater = new LEARNINGPreliminaryDisamb(
                freebaseMatcher, disambiguator, class_scorer
        );


        LEARNING columnInterpreter = new LEARNING(column_learnerSeeding, column_updater, TableMinerConstants.TCELLDISAMBIGUATOR_MAX_REFERENCE_ENTITIES);

        //object to computeElementScores relations between columns
        RelationScorer relation_scorer = new TMPRelationScorer(nlpResources,
                new FreebaseRelationBoWCreator(),
                stopWords,
                new double[]{1.0, 1.0, 0.0, 0.0, 1.0}    //entity, header text, column, title&caption, other
        );
        TColumnColumnRelationEnumerator interpreter_relation = new TColumnColumnRelationEnumerator(
                new TMPAttributeValueMatcher(0.0, stopWords),
                relation_scorer
        );

        //object to consolidate previous output, further computeElementScores columns and disamgiuate entities
        LiteralColumnTagger interpreter_with_knownRelations = new LiteralColumnTaggerImpl(
                IGNORE_COLUMNS
        );


        UPDATE updater = new UPDATE(selector, freebaseMatcher, disambiguator, class_scorer, stopWords, nlpResources);
        TMPInterpreter interpreter = new TMPInterpreter(
                main_col_finder,
                columnInterpreter,
                interpreter_with_knownRelations,
                interpreter_relation,
                IGNORE_COLUMNS, new int[0],
                updater, relation_scorer);

        TAnnotationWriter writer = new TAnnotationWriter(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));


        TableXtractorIMDB_noTripleContext xtractor = new TableXtractorIMDB_noTripleContext(new TableNormalizerFrequentRowLength(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorIMDB(),
                new TabValGeneric());
        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        System.out.println(all.size());

        for (File f : all) {
            count++;
            if (count - 1 < start)
                continue;
            /*if(count>1)
                continue;*/
            /*boolean found = false;
            for (int od : onlyDo) {
                if (od == count)
                    found = true;
            }
            if (!found)
                continue;*/

            boolean complete = false;
            String inFile = f.toString();
            try {
                String fileContent = FileUtils.readFileContent(new File(inFile));
                List<Table> tables = xtractor.extract(fileContent, inFile);

                if (tables.size() == 0)
                    continue;

                Table table = tables.get(0);

                String sourceTableFile = inFile;
                if (sourceTableFile.startsWith("\"") && sourceTableFile.endsWith("\""))
                    sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length() - 1).trim();
                System.out.println(count + "_" + sourceTableFile + " " + new Date());
                log.info(">>>" + count + "_" + sourceTableFile);

                complete = process(interpreter, table, sourceTableFile, writer, outFolder, relationLearning);
                //server.commit();
                if (TableMinerConstants.COMMIT_SOLR_PER_FILE)
                    server.commit();
                if (!complete) {
                    System.out.println("\t\t\t missed: " + count + "_" + sourceTableFile);
                    PrintWriter missedWriter = null;
                    try {
                        missedWriter = new PrintWriter(new FileWriter("ti_imdb(iswc)_missed.csv", true));
                        missedWriter.println(count + "," + inFile);
                        missedWriter.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                //gs annotator

            } catch (Exception e) {
                e.printStackTrace();
                PrintWriter missedWriter = null;
                try {
                    missedWriter = new PrintWriter(new FileWriter("ti_imdb_missed(iswc).csv", true));
                    missedWriter.println(count + "," + inFile);
                    missedWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                e.printStackTrace();
                server.shutdown();
                System.exit(1);
            }

        }
        server.shutdown();
        System.out.println(new Date());
    }


    public static boolean process(TMPInterpreter interpreter, Table table, String sourceTableFile, TAnnotationWriter writer,
                                  String outFolder, boolean relationLearning) throws FileNotFoundException {
        String outFilename = sourceTableFile.replaceAll("\\\\", "/");
        try {
            TAnnotation annotations = interpreter.start(table, relationLearning);

            int startIndex = outFilename.lastIndexOf("/");
            if (startIndex != -1) {
                outFilename = outFilename.substring(startIndex + 1).trim();
            }
            writer.writeHTML(table, annotations, outFolder + "/" + outFilename + ".html");
            write_iswc_output(table, annotations, new File(outFilename).getName(), "film-imdb-actor.txt.tableminer");

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


    public static void write_iswc_output(Table table, TAnnotation tableAnnotation, String imdb_file_name, String outFile) throws IOException {
        List<TColumnHeaderAnnotation> has = tableAnnotation.getWinningHeaderAnnotations(1);
        if (has != null && has.size() > 0) {
            boolean correct = false;
            for (TColumnHeaderAnnotation ha : has) {
                if (ha.getAnnotation_url().equals("/tv/tv_actor")
                        || ha.getAnnotation_url().equals("/film/actor")
                        || ha.getAnnotation_url().equals("/m/0np9r")
                        || ha.getAnnotation_url().equals("/m/02hrh1q")) {
                    correct = true;
                    break;
                }
            }

            if (correct) {
                FileOutputStream fileStream = new FileOutputStream(new File(outFile), true);
                OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");
                PrintWriter p = new PrintWriter(writer);

                String append = "";
                int count = 0;
                for (int r = 0; r < table.getNumRows(); r++) {
                    TCell tcc = table.getContentCell(r, 1);
                    if (tcc != null && tcc.getText() != null && tcc.getText().length() > 0) {
                        count++;
                        append += tcc.getText() + "\t";
                    }
                }
                p.println(imdb_file_name + "\t" + count + "\t" + append);
                p.close();
            }

        }
    }
}
