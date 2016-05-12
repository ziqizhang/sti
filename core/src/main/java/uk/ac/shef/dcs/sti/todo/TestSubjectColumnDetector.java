package uk.ac.shef.dcs.sti.todo;

import javafx.util.Pair;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.parser.table.TableParserMusicBrainz;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizerSimple;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidatorGeneric;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorMusicBrainz;
import uk.ac.shef.dcs.sti.util.FileUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 */
public class TestSubjectColumnDetector {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, STIException {
        /*which_table_has_no_acronym_columns("E:\\Data\\table annotation\\freebase_crawl\\music_record_label/tmp.txt");
        System.exit(0);*/

        /* gs_rewrite("E:\\Data\\table annotation\\corpus_analysis\\90_tables/90_tables_for_studying_subject_columns_(noise_removed).csv",
                "E:\\Data\\table annotation\\corpus_analysis\\90_tables\\91_tables_subcol_gs.csv");
        System.exit(0);*/
        String inFolder = args[0];
        String outFile = args[1];
        String nlpResources = args[2];
        String cacheFolder = args[3];
        Properties properties = new Properties();
        properties.load(new FileInputStream(args[4]));
        List<String> stopWords = FileUtils.readList(nlpResources + "/stoplist.txt", true);
        File configFile = new File(cacheFolder + File.separator + "solr.xml");

        //todo: this class will not work, the following code must be resumed and corrected
        /*CoreContainer container = new CoreContainer(cacheFolder,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");
        SubjectColumnDetector finder = new SubjectColumnDetector(new TContentTContentRowRankerImpl(),
                IInf.class.getName(),
                new String[]{"0.0", "1", "0.01"},
                server,
                nlpResources, true, stopWords,
                MultiKeyStringSplitter.split(properties.getProperty("BING_API_KEYS")) //lodie

        );*/

        EmbeddedSolrServer server=null;
        SubjectColumnDetector finder=null;

        /* List<String> tasks=
            FileUtils.readList("E:\\Data\\table annotation\\corpus_analysis\\90_tables/" +
                    "90_tables_for_studying_subject_columns_(noise_removed).csv",false);
    PrintWriter p = new PrintWriter("E:\\Data\\table annotation\\corpus_analysis\\90_tables" +
            "/evaluate_subject_column_finder.csv");
    int count=0;
    for(String task: tasks){
        count++;
        *//*if(count==1||count==57)
                System.out.println();*//*

            int lastComma = task.lastIndexOf(",");
            String sourceTableFile = task.substring(0, lastComma).trim();
            if(sourceTableFile.startsWith("\"") &&sourceTableFile.endsWith("\""))
                sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length()-1).trim();
            System.out.println(count+"_"+sourceTableFile);

            String groundTruth = task.substring(lastComma+1).trim();

            Table table = LimayeDatasetLoader.readTable(sourceTableFile, null, null);

            List<ObjObj<Integer, ObjObj<Double, Boolean>>> result = finder.compute(table);

            StringBuilder sb = new StringBuilder();
            sb.append("\"").append(sourceTableFile).append("\",").append(groundTruth).append(",").append(result.get(0).getMainObject()+1);
            p.println(sb.toString());
        }
        p.close();

        server.closeConnection();
        System.exit(0);*/
        //String inFolder="E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables";
        TableParserMusicBrainz xtractor = new TableParserMusicBrainz(new TableNormalizerSimple(),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorMusicBrainz(),
                new TableValidatorGeneric());
        PrintWriter p = new PrintWriter(outFile);
        int count = 0;
        /*TableXtractorMusicBrainz xtractor = new TableXtractorMusicBrainz(new TableNormalizerDummy(),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorMusicBrainz(),
                new TabValGeneric());*/

        List<File> sorted = new ArrayList<File>(Arrays.asList(new File(inFolder).listFiles()));
        Collections.sort(sorted);
        for (File f : sorted) {

            count++;
            String task = f.toString();
            /*if(!task.contains("Atlantis"))
                continue;*/
            System.out.println(count + "_" + f);

            /*if(count==1||count==57)
                System.out.println();*/


            String fileContent = org.apache.any23.util.FileUtils.readFileContent(f);
            List<Table> tables = xtractor.extract(fileContent, f.toString());
            if (tables.size() == 0)
                continue;

            Table table = tables.get(0);

            //Table table = LimayeDatasetLoader.readTable(task, null, null);
            // String fileContent = org.apache.any23.util.FileUtils.readFileContent(f);
            //List<Table> tables = xtractor.extract(fileContent, task);
            /* if(tables.size()<1)
            continue;*/
            // Table table = tables.get(0);
            try {
                List<Pair<Integer, Pair<Double, Boolean>>> result = finder.compute(table);

                p.println("\"" + f + "\"," + result.get(0).getKey() + "," + result.get(0).getValue().getValue());
            } catch (Exception e) {
                System.err.println("FAILED:" + f);
            }

        }
        p.close();

        server.close();
        System.exit(0);
    }

    public static void which_table_has_no_acronym_columns(String logFile) throws IOException {

        boolean newFile = false;
        boolean hasAcronym = false;
        for (String l : FileUtils.readList(logFile, false)) {
            if (l.contains("_E:")) {
                if (newFile && !hasAcronym)
                    System.out.println(l);

                hasAcronym = false;
                newFile = true;
                continue;
            } else {
                hasAcronym = true;
            }

        }
    }

    public static void gs_rewrite(String inGsFile, String outGsFile) throws IOException {
        List<String> lines = FileUtils.readList(inGsFile, false);
        List<String> newGs = new ArrayList<String>();
        for (String l : lines) {
            String originalLine = l;

            int split = l.lastIndexOf(",");
            String path = l.substring(0, split).trim();
            path = path.replaceAll("\\\\", "/");
            split = path.lastIndexOf("/");
            path = path.substring(split + 1).trim();
            if (path.endsWith("\""))
                path = path.substring(0, path.length() - 1).trim();
            newGs.add("\"" + path + "\"," + originalLine.substring(originalLine.lastIndexOf(",") + 1).trim());
        }
        Collections.sort(newGs);
        PrintWriter p = new PrintWriter(outGsFile);
        for (String l : newGs)
            p.println(l);
        p.close();
    }
}
