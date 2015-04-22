package uk.ac.shef.dcs.oak.lodietest.experiment.table.gs;


import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.util.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.oak.lodie.PlaceHolder;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 27/02/13
 * Time: 14:32
 */
@Deprecated
public class LimayeDatasetLoader {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        String cleanTableRepos = args[0];
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

            System.out.println(cleanFile);

            readTable(cleanFile, annotated.toString(), args[2]);
        }

        /*LTable table = readTable("E:\\data\\table annotation\\tablesForAnnotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
                "E:\\data\\table annotation\\workspace\\WWT_GroundTruth\\annotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
                "D:\\work\\lodiedata\\limayetable");*/
        System.out.println();
    }

    public static LTable readTable(String tableFilename, String tableAnnotationFilename, String htmlRepository) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document domCleanTable = docBuilder.parse(tableFilename);

        //read the table content
        List<Node> tableContent = DomUtils.findAll(domCleanTable, "//logicalTable/content");
        if (tableContent == null || tableContent.size() == 0)
            return null;
        boolean firstRowHeader = false;
        List<String[]> rows = new ArrayList<String[]>();
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
            for (int j = 0; j < columns.size(); j++) {
                Node cell = columns.get(j);
                List<Node> text = DomUtils.findAll(cell, "text");
                String textContent = "";
                if (text != null && text.size() > 0) {
                    textContent = text.get(0).getTextContent();
                }
                cells[j] = textContent;
            }
            rows.add(cells);
        }

        int totalCol = 0;
        for (String[] row : rows) {
            if (row.length > totalCol)
                totalCol = row.length;
        }

        LTable table = null;
        int rowModifier=0;
        if (firstRowHeader) {
            table = new LTable(String.valueOf(tableFilename.hashCode()), tableFilename, rows.size()-1, totalCol);
            rowModifier=1;
            for (int j = 0; j < totalCol; j++) {
                LTableColumnHeader header = new LTableColumnHeader(rows.get(0)[j]);
                table.setColumnHeader(j,header);
            }
        } else {//no header, need to add false headers
            table = new LTable(String.valueOf(tableFilename.hashCode()), tableFilename, rows.size(), totalCol);
            for (int j = 0; j < totalCol; j++) {
                LTableColumnHeader header = new LTableColumnHeader(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue());
                table.setColumnHeader(j,header);
            }
        }

        for (int r = rowModifier; r < rows.size(); r++) {
            String[] cells = rows.get(r);
            for (int c = 0; c < cells.length; c++) {
                LTableContentCell cell = new LTableContentCell(cells[c]);
                table.setContentCell(r - rowModifier, c, cell);
            }
        }


        //read the table context
        List<Node> tableContext = DomUtils.findAll(domCleanTable, "//logicalTable/tableContext");
        if (tableContext != null || tableContext.size() != 0) {
            Node ctxParentNode = tableContext.get(0);
            NodeList contexts = ctxParentNode.getChildNodes();
            for (int i = 0; i < contexts.getLength(); i++) {
                Node n = contexts.item(i);
                if (n.getNodeName().equals("#text"))
                    continue;
                List<Node> textNode = DomUtils.findAllByTag(n, "text");
                if (textNode != null && textNode.size() > 0) {
                    String context = textNode.get(0).getTextContent();
                    if (context != null){
                        if(i==0)
                            table.addContext(new LTableContext(context, LTableContext.TableContextType.PAGETITLE, 1.0));
                        else
                            table.addContext(new LTableContext(context, LTableContext.TableContextType.BEFORE, 1.0));
                    }
                }
            }
        }

        //dump the original html snippet to a human readable html format
        List<Node> htmlSnippet = DomUtils.findAll(domCleanTable, "//htmlSnippet");
        if (htmlSnippet != null && htmlSnippet.size() != 0)
            dumpHTMLContent(htmlSnippet.get(0), htmlRepository, tableFilename);


        Document domAnnotatedTable = docBuilder.parse(tableAnnotationFilename);

        //read the header annotations
        List<Node> headerAnnotations = DomUtils.findAll(domAnnotatedTable, "//columnAnnotations/colAnnos");
        for (int i = 0; i < headerAnnotations.size(); i++) {
            Node header = headerAnnotations.get(i);
            int col = Integer.valueOf(header.getAttributes().getNamedItem("col").getTextContent());
            //LTableContentCell headerCell = table.getHeaderForColumn(col);
            NodeList annotations = header.getChildNodes();
            List<CellAnnotation> hAnnotations=new ArrayList<CellAnnotation>();
            for (int j = 0; j < annotations.getLength(); j++) {
                Node n = annotations.item(j);
                if (n.getNodeName().equals("anno")) {
                    CellAnnotation a = new CellAnnotation(table.getColumnHeader(col).getHeaderText(),
                            new EntityCandidate(n.getAttributes().getNamedItem("name").getTextContent(),n.getAttributes().getNamedItem("name").getTextContent()),
                            Double.valueOf(n.getAttributes().getNamedItem("value").getTextContent().trim()), new HashMap<String, Double>());

                    hAnnotations.add(a);
                }
            }
            table.getTableAnnotations().setHeaderAnnotation(col, hAnnotations.toArray(new HeaderAnnotation[0]));
        }
        //read the data rows annotations
        List<Node> dataRowAnnotations = DomUtils.findAll(domAnnotatedTable, "//cellAnnotatoons/row");
        for (int i = 0; i < dataRowAnnotations.size(); i++) {
            Node row = dataRowAnnotations.get(i);
            List<Node> cols = DomUtils.findAll(row, "entity");
            for (int j = 0; j < cols.size(); j++) {
                Node htmlCell = cols.get(j);
                if (htmlCell.getTextContent() == null || htmlCell.getTextContent().length() == 0) {
                    continue;
                }
                CellAnnotation cellAnnotation = new CellAnnotation(table.getContentCell(i+1, j).getText(),
                        new EntityCandidate(
                        table.getContentCell(i+1, j).getText(), htmlCell.getTextContent()),1.0, new HashMap<String, Double>()
                );

                table.getTableAnnotations().setContentCellAnnotations(
                        i+1, j, new CellAnnotation[]{cellAnnotation}
                );
            }

        }

        //todo: read relation annotation


        return table;
    }

    private static void dumpHTMLContent(Node htmlSnippetNode, String htmlRepository, String filePath) throws FileNotFoundException {
        String content = htmlSnippetNode.getTextContent();
        int begin = content.indexOf("CDATA[");
        begin = begin == -1 ? 0 : begin + 7;
        int end = content.lastIndexOf("]]>");
        end = end == -1 ? content.length() : end;
        content = content.substring(begin, end).trim();

        PrintWriter p = new PrintWriter(htmlRepository + File.separator + new File(filePath).getName() + "_" + filePath.hashCode() + ".html");
        p.println("<html><body><p>");
        p.println(filePath);
        p.println("</p>");
        p.println(content);
        p.println("</body></html>");
        p.close();
    }


}
