package uk.ac.shef.dcs.sti.experiment.gs;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.algorithm.tm.TripleGenerator;
import uk.ac.shef.dcs.sti.io.LTableAnnotationWriter;
import uk.ac.shef.dcs.sti.experiment.LimayeDatasetLoader;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseQueryHelper;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.sti.rep.LTable;
import uk.ac.shef.dcs.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.util.FileUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 09/03/14
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */
public class GSBuilder_Limaye {

    private FreebaseQueryHelper queryHelper;

    public GSBuilder_Limaye(FreebaseQueryHelper queryHelper) {
        this.queryHelper = queryHelper;
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
         find_missed_files_by_folder("E:\\Data\\table_annotation\\limaye\\all_tables_freebase_groundtruth",
                 "E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_xml_only",
                 "E:\\Data\\table_annotation\\limaye/gs_limaye_empty.missed");
        System.exit(0);

        /* find_missed_files("E:\\Data\\table annotation\\limaye/gs_limaye.e8031313", "E:\\Data\\table annotation\\limaye/gs_limaye.missed");
        System.exit(0);*/


        FreebaseQueryHelper queryHelper = new FreebaseQueryHelper(args[3]);
        LTableAnnotationWriter writer = new LTableAnnotationWriter(new TripleGenerator("http://www.freebase.com", "http://dcs.shef.ac.uk"));
        String in_raw_file_folder = args[0];
        String in_gs_file_folder = args[1];
        String outFolder = args[2];
        int startfrom = new Integer(args[4]);
        List<String> missedFile = new ArrayList<String>();
        if (args.length == 6) {
            missedFile = FileUtils.readList(args[5], true);
        }

        GSBuilder_Limaye gsBuilder = new GSBuilder_Limaye(queryHelper);
        int count = 0;
        File[] all = new File(in_gs_file_folder).listFiles();
        List<File> sorted = new ArrayList<File>(Arrays.asList(all));
        System.out.println(all.length);
        for (File f : sorted) {
            try {
                File raw_file = new File(in_raw_file_folder + "/" + f.getName());
                if (!raw_file.exists()) {
                    System.out.println("no gs for: " + f);
                    continue;
                }

                if (missedFile.size() > 0) {
                    boolean missed = false;
                    for (String mf : missedFile) {
                        String fp = f.toString().replaceAll("\\\\", "/").toLowerCase();
                        if (fp.endsWith(mf)) {
                            missed = true;
                            break;
                        }
                    }
                    if (!missed)
                        continue;
                }

                count++;
                if (startfrom > count)
                    continue;

                String inFile = f.toString();
                System.out.println(count + "_" + inFile + " " + new Date());
                LTable table = LimayeDatasetLoader.readTable(raw_file.toString(), null, null);
                LTableAnnotation annotations = gsBuilder.readTableAnnotation(inFile, table);
                gsBuilder.save(table, annotations, outFolder, writer);
            } catch (Exception e) {
                System.err.println("ERROR:" + f);
                e.printStackTrace();
            }
        }
    }

    public void save(LTable table, LTableAnnotation annotations, String outFolder, LTableAnnotationWriter writer) throws FileNotFoundException {
        String fileId = table.getSourceId();
        fileId = fileId.replaceAll("\\\\", "/");
        int trim = fileId.lastIndexOf("/");
        if (trim != -1)
            fileId = fileId.substring(trim + 1).trim();
        writer.writeHTML(table, annotations, outFolder + File.separator + fileId);
        String annotation_keys = outFolder + File.separator + fileId + ".keys";
        PrintWriter p = new PrintWriter(annotation_keys);
        for (int row = 0; row < table.getNumRows(); row++) {
            for (int col = 0; col < table.getNumCols(); col++) {
                CellAnnotation[] anns = annotations.getContentCellAnnotations(row, col);
                if (anns != null && anns.length > 0) {
                    p.println(row + "," + col + "," + anns[0].getAnnotation().getId());
                }
            }
        }
        p.close();
    }

    public LTableAnnotation readTableAnnotation(String tableAnnotationFilename,
                                                LTable table) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document domAnnotatedTable = docBuilder.parse(tableAnnotationFilename);

        //read the data rows annotations
        List<Node> dataRowAnnotations = DomUtils.findAll(domAnnotatedTable, "//cellAnnotatoons/row");
        int total_columns = 0;
        for (int i = 0; i < dataRowAnnotations.size(); i++) {
            Node row = dataRowAnnotations.get(i);
            List<Node> cols = DomUtils.findAll(row, "entity");
            int columns = 0;
            for (int j = 0; j < cols.size(); j++) {
                Node htmlCell = cols.get(j);
                if (htmlCell.getTextContent() == null || htmlCell.getTextContent().length() == 0) {
                    continue;
                }
                columns++;
            }
            if (columns > total_columns)
                total_columns = columns;
        }

        LTableAnnotation annotations = new LTableAnnotation(table.getNumRows(), table.getNumCols());
        for (int i = 0; i < dataRowAnnotations.size(); i++) {
            Node row = dataRowAnnotations.get(i);
            List<Node> cols = DomUtils.findAll(row, "entity");
            for (int j = 0; j < cols.size(); j++) {
                Node htmlCell = cols.get(j);
                if (htmlCell.getTextContent() == null || htmlCell.getTextContent().length() == 0) {
                    continue;
                }
                String wikipedia_title = htmlCell.getTextContent().trim();
                try {
                    CellAnnotation[] cellAnnotations = createCellAnnotation(wikipedia_title);
                    System.out.println("\t row=" + i + ",col=" + j);
                    if (cellAnnotations != null)
                        annotations.setContentCellAnnotations(i, j, cellAnnotations);
                    else {
                        System.out.println();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return annotations;
    }

    public CellAnnotation[] createCellAnnotation(String wikipedia_title) throws IOException {
        String wiki_page_id = queryWikipediaPageId(wikipedia_title);
        List<String> list = queryHelper.mqlapi_topic_mids_with_wikipedia_pageid(wiki_page_id);
        if (list == null || list.size() == 0)
            return null;

        Entity ec = new Entity(list.get(0), wikipedia_title);
        CellAnnotation ca = new CellAnnotation(wikipedia_title, ec, 1.0, new HashMap<String, Double>());
        return new CellAnnotation[]{ca};
    }

    private static String queryWikipediaPageId(String wikipedia_title) throws IOException {
        String query = "https://en.wikipedia.org/w/api.php?action=query&titles=" + wikipedia_title;
        URL u = new URL(query);
        URLConnection connection = u.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        String result = response.toString();

        int start = result.indexOf("page pageid=");
        if (start != -1) {
            String pageIdLine = result.substring(start);
            int firstQuote = pageIdLine.indexOf("&quot;");
            if (firstQuote != -1) {
                pageIdLine = pageIdLine.substring(firstQuote + 6);
                int secondQuote = pageIdLine.indexOf("&quot;");
                if (secondQuote != -1) {
                    pageIdLine = pageIdLine.substring(0, secondQuote).trim();
                    try {
                        return String.valueOf(Long.valueOf(pageIdLine));
                    } catch (Exception e) {
                    }
                }
            }
        }

        return null;
    }

    public static void find_missed_files(String inLogFile, String outListFile) throws IOException {


        PrintWriter p = new PrintWriter(outListFile);
        for (String l : FileUtils.readList(inLogFile, false)) {
            if (l.startsWith("ERROR:")) {
                int start = l.indexOf(":");
                l = l.substring(start + 1).trim();
                p.println(l);
            }
        }
        p.close();

    }

    public static void find_missed_files_by_folder(String inFolder_with_annotations, String inFolder_raw, String out_missed_file) throws IOException {

        PrintWriter p = new PrintWriter(out_missed_file);
        List<String> annotated = new ArrayList<String>();
        for (File f : new File(inFolder_with_annotations).listFiles()) {
            annotated.add(f.getName());
        }

        for (File f : new File(inFolder_raw).listFiles()) {
            if (annotated.contains(f.getName()+".cell.keys"))
                continue;
            else
                p.println(f.getName());
        }

        p.close();
    }
}
