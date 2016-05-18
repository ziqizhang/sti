package uk.ac.shef.dcs.sti.todo.evaluation;

import org.apache.any23.util.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.parser.table.TableParser;
import uk.ac.shef.dcs.sti.parser.table.TableParserMusicBrainz;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorMusicBrainz;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizerSimple;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidatorGeneric;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 29/04/14
 * Time: 12:37
 * To change this template use File | Settings | File Templates.
 */
public class DataStats_TableSize_NameLength_Analysis_MB_IMDB {

    private static TableParser xtractor;

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, STIException {

        /////////////imdb
        /*String cleanTableRepos = "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\raw\\imdb_raw";
        String annotationRepos = "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted";
        xtractor = new TableXtractorIMDB(new TableNormalizerFrequentRowLength(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorIMDB(),
                new TabValGeneric());*/

        //////////////musicbrainz
        String cleanTableRepos = "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\raw";
        String annotationRepos = "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted";
        xtractor = new TableParserMusicBrainz(new TableNormalizerSimple(),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorMusicBrainz(),
                new TableValidatorGeneric());


        File[] files = new File(cleanTableRepos).listFiles();
        int count = 0;
        for (File clean : files) {
            String path = clean.getAbsolutePath().replaceAll("\\\\", "/");
            String relative = path.substring(cleanTableRepos.length());

            String annotated = annotationRepos + relative + ".keys";
            if (!(new File(annotated)).exists()) {
                //System.err.println("annotated file does not exist: " + annotated);
                continue;
            }

            count++;
            System.out.println(count);

            PrintWriter p = new PrintWriter(new FileWriter("D:\\Work\\lodie\\tmp_result/out.csv", true));
            PrintWriter p2 = new PrintWriter(new FileWriter("D:\\Work\\lodie\\tmp_result/out_name.csv", true));
            checkGroundTruth(clean.toString(), annotated, p, p2);
            p.close();
            p2.close();
        }

        /*Table table = readTable("E:\\data\\table annotation\\tablesForAnnotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
                "E:\\data\\table annotation\\workspace\\WWT_GroundTruth\\annotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
                "D:\\work\\lodiedata\\limayetable");*/
        System.out.println();
    }

    //tableAnnotationFileanem and htmlRepository can both be null, then they are ignored
    public static Table checkGroundTruth(String tableFilename, String tableAnnotationFilename, PrintWriter p,
                                          PrintWriter p2) throws IOException, ParserConfigurationException, SAXException, STIException {

        Table table = null;
        String fileContent = FileUtils.readFileContent(new File(tableFilename));
        List<Table> tables = xtractor.extract(fileContent, tableFilename);
        if (tables.size() > 0) {
            table = tables.get(0);


            if (tableAnnotationFilename == null)
                return table;


            if (new File(tableAnnotationFilename).exists()) {
                String[] entity_annotations = FileUtils.readFileLines(new File(tableAnnotationFilename));

                Set<Integer> columns_with_annotations = new HashSet<Integer>();
                Set<Integer> rows_with_annotations = new HashSet<Integer>();
                for (String l : entity_annotations) {
                    if (l.trim().length() < 1)
                        continue;
                    String position = l.split("=")[0].trim();
                    int i = Integer.valueOf(position.split(",")[0].trim());
                    int j = Integer.valueOf(position.split(",")[1].trim());
                    columns_with_annotations.add(j);
                    rows_with_annotations.add(i);
                    String textContent = table.getContentCell(i, j).getText();
                    textContent = textContent.replaceAll("[\\-_]"," ").trim();

                    ////////////////////////printing average name length /////////////////////////
                    int length =textContent.split("\\s+").length;
                    p2.println(length);

                    if (length > 10)
                        System.out.println(">10:" + tableFilename);
                    if (length > 20)
                        System.out.println(">20:" + tableFilename);
                    if (length > 120)
                        System.out.println(">120:" + tableFilename);
                }

                //printing num of rows, and columns that have entity annotations
                p.println(rows_with_annotations.size() + "," + columns_with_annotations.size());
                if (columns_with_annotations.size() > 6)
                    System.out.println("c>6:" + tableFilename);
                if (columns_with_annotations.size() > 10)
                    System.out.println("c>6:" + tableFilename);
                //p.println(rows_with_annotations.size() + "," + columns_with_annotations.size());
            }
            return table;
        }
        return null;
    }

    private static String extract_text_content_from_html(List<Node> html) {
        String content = html.get(0).getTextContent();
        int start = content.indexOf("<td>");
        if (start == -1)
            start = content.indexOf("<th>");
        if (start == -1)
            start = content.indexOf("<thead>");
        if (start != -1) {
            content = content.substring(start + 4);
            int end = content.indexOf("</td>");
            if (end == -1)
                end = content.indexOf("</th>");
            if (end == -1)
                end = content.indexOf("</thead>");
            if (end != -1)
                content = content.substring(0, end).trim();
        }
        content = StringEscapeUtils.unescapeHtml4(content);
        return content;
    }

}
