package uk.ac.shef.dcs.sti.todo.evaluation;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.util.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 29/04/14
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public class DataStats_TableSize_NameLength_Analysis_LimayeNew {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {

        /////////////limayeall
        String cleanTableRepos = // "E:\\Data\\table_annotation\\limaye\\all_tables_raw(regen)";
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\raw";
        String annotationRepos = "E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(regen)";

        File[] files = FileUtils.listFilesRecursively(new File(cleanTableRepos), new SuffixFileFilter(".xml"));
        int count = 0;
        for (File clean : files) {
            String path = clean.getAbsolutePath().replaceAll("\\\\", "/");
            String relative = path.substring(cleanTableRepos.length());

            String annotated = annotationRepos + relative + ".cell.keys";
            if (!(new File(annotated)).exists()) {
                //System.err.println("annotated file does not exist: " + annotated);
                continue;
            }

            count++;
            System.out.println(count);

            PrintWriter p = new PrintWriter(new FileWriter("D:\\Work\\lodie\\tmp_result/out.csv", true));
            PrintWriter p2 = new PrintWriter(new FileWriter("D:\\Work\\lodie\\tmp_result/out_name.csv", true));
            try{
            checkGroundTruth(clean.toString(), annotated, p, p2);
            }catch(Exception e){
                e.printStackTrace();
            }
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
                                          PrintWriter p2) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document domCleanTable = docBuilder.parse(tableFilename);

        //read the table content
        List<Node> tableContent = DomUtils.findAll(domCleanTable, "//logicalTable/content");
        if (tableContent == null || tableContent.size() == 0)
            return null;
        boolean firstRowHeader = false;
        List<String[]> rows = new ArrayList<String[]>();
        List<String[]> rows_with_other_text = new ArrayList<String[]>();
        NodeList rowNodes = tableContent.get(0).getChildNodes();

        for (int i = 0; i < rowNodes.getLength(); i++) {
            Node row = rowNodes.item(i);
            if (row.getNodeName().equals("#text"))
                continue;
            if (row.getNodeName().equals("header")) {
                firstRowHeader = true;
            }

            List<Node> columns = DomUtils.findAll(row, "cell");
            String[] cells = new String[columns.size()];
            String[] cells_other_text = new String[columns.size()];
            for (int j = 0; j < columns.size(); j++) {
                Node cell = columns.get(j);
                List<Node> html = DomUtils.findAll(cell, "html");
                String textContent = "";
                if (html != null && html.size() > 0) {
                    textContent = extract_text_content_from_html(html);
                }

                if (textContent.equals("")) {
                    List<Node> text = DomUtils.findAll(cell, "text");

                    if (text != null && text.size() > 0) {
                        textContent = text.get(0).getTextContent();
                    }
                }

                List<Node> other_text = DomUtils.findAll(cell, "wikipedia");
                if (other_text != null && other_text.size() > 0) {
                    String othertextContent = extract_text_content_from_html(other_text);
                    cells_other_text[j] = othertextContent;
                }
                cells[j] = textContent;
            }
            rows.add(cells);
            rows_with_other_text.add(cells_other_text);
        }

        int totalCol = 0;
        for (String[] row : rows) {
            if (row.length > totalCol)
                totalCol = row.length;
        }

        Table table = null;
        int rowModifier = 0;
        if (firstRowHeader) {
            table = new Table(String.valueOf(tableFilename.hashCode()), tableFilename, rows.size() - 1, totalCol);
            rowModifier = 1;
            if (rows.get(0).length < totalCol) {
                System.err.println("WARNING:Artificial header added, check manually. " + tableFilename);
                String[] headers = rows.get(0);
                String[] modified = new String[totalCol];
                for (int i = 0; i < modified.length; i++) {
                    if (i < headers.length)
                        modified[i] = headers[i];
                    else
                        modified[i] = STIEnum.TABLE_HEADER_UNKNOWN.getValue();
                }
                rows.set(0, modified);
            }
            for (int j = 0; j < totalCol; j++) {
                TColumnHeader header = new TColumnHeader(rows.get(0)[j]);
                table.setColumnHeader(j, header);
            }
        } else {//no header, need to add false headers
            table = new Table(String.valueOf(tableFilename.hashCode()), tableFilename, rows.size(), totalCol);
            for (int j = 0; j < totalCol; j++) {
                TColumnHeader header = new TColumnHeader(STIEnum.TABLE_HEADER_UNKNOWN.getValue());
                table.setColumnHeader(j, header);
            }
        }

        for (int r = rowModifier; r < rows.size(); r++) {
            String[] cells = rows.get(r);
            String[] cells_other_text = rows_with_other_text.get(r);
            for (int c = 0; c < cells.length; c++) {
                TCell cell = new TCell(cells[c]);
                String other = cells_other_text[c];
                if(other!=null&&other.length()>0)
                    cell.setOtherText(other);
                table.setContentCell(r - rowModifier, c, cell);
            }
        }

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
                int length = 0;
                if (textContent.length() > 20 && !textContent.contains(" ") && textContent.contains("/")) {
                    length = 0; //url, long string
                } else {
                    textContent = textContent.replaceAll("[\\-_]"," ").trim();

                    ////////////////////////printing average name length /////////////////////////
                    length = textContent.split("\\s+").length;
                    if (length > 10) {
                        textContent = table.getContentCell(i, j).getOtherText();
                        textContent = textContent.replaceAll("[\\-_]"," ").trim();

                        if (textContent.length() > 0) {
                            length = textContent.split("\\s+").length;
                            if (length != 0)
                                System.out.print(".");
                        }
                    }
                }

                p2.println(length);
                if (length > 10)
                    System.out.println(">10:" + tableFilename);
                if (length > 20)
                    System.out.println(">20:" + tableFilename);
                if (length > 120)
                    System.out.println(">120:" + tableFilename);
            }

            //printing num of rows, and columns that have entity annotations
            if (columns_with_annotations.size() > 6)
                System.out.println("c>6:" + tableFilename);
            if (columns_with_annotations.size() > 10)
                System.out.println("c>6:" + tableFilename);
            p.println(rows_with_annotations.size() + "," + columns_with_annotations.size());
        }
        return table;
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
