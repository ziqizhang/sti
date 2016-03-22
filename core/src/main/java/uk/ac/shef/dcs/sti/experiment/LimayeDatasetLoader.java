package uk.ac.shef.dcs.sti.experiment;


import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.util.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.PlaceHolder;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;

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

        /*Table table = readTable("E:\\data\\table annotation\\tablesForAnnotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
                "E:\\data\\table annotation\\workspace\\WWT_GroundTruth\\annotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
                "D:\\work\\lodiedata\\limayetable");*/
        System.out.println();
    }

    //tableAnnotationFileanem and htmlRepository can both be null, then they are ignored
    public static Table readTable(String tableFilename, String tableAnnotationFilename, String htmlRepository) throws IOException, ParserConfigurationException, SAXException {
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
                List<Node> html = DomUtils.findAll(cell, "html");
                String textContent = "";
                if (html != null && html.size() > 0) {
                    textContent=extract_text_content_from_html(html);
                }

                if(textContent.equals("")){
                    List<Node> text = DomUtils.findAll(cell, "text");

                    if (text != null && text.size() > 0) {
                        textContent = text.get(0).getTextContent();
                    }
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

        Table table = null;
        int rowModifier = 0;
        if (firstRowHeader) {
            table = new Table(String.valueOf(tableFilename.hashCode()), tableFilename, rows.size() - 1, totalCol);
            rowModifier = 1;
            if (rows.get(0).length < totalCol){
                System.err.println("WARNING:Artificial header added, check manually. "+tableFilename);
                String[] headers = rows.get(0);
                String[] modified = new String[totalCol];
                for(int i=0; i<modified.length; i++){
                    if(i<headers.length)
                        modified[i]=headers[i];
                    else
                        modified[i]=PlaceHolder.TABLE_HEADER_UNKNOWN.getValue();
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
                TColumnHeader header = new TColumnHeader(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue());
                table.setColumnHeader(j, header);
            }
        }

        for (int r = rowModifier; r < rows.size(); r++) {
            String[] cells = rows.get(r);
            for (int c = 0; c < cells.length; c++) {
                TContentCell cell = new TContentCell(cells[c]);
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
                    if (context != null) {
                        TContext ctx = null;
                        if (i == 1)
                            ctx = new TContext(context, TContext.TableContextType.PAGETITLE, 1.0);
                        else ctx = new TContext(context, TContext.TableContextType.BEFORE, 1.0);

                        table.addContext(ctx);
                    }
                }
            }
        }
        if (table.getContexts().size() > 1)
            table.getContexts().remove(1);  //always ignore the 2nd context as it is the header of the table

        //dump the original html snippet to a human readable html format
        if (htmlRepository != null) {
            List<Node> htmlSnippet = DomUtils.findAll(domCleanTable, "//htmlSnippet");
            if (htmlSnippet != null && htmlSnippet.size() != 0)
                dumpHTMLContent(htmlSnippet.get(0), htmlRepository, tableFilename);
        }
        if (tableAnnotationFilename == null)
            return table;


        if(new File(tableAnnotationFilename).exists()){
        Document domAnnotatedTable = docBuilder.parse(tableAnnotationFilename);

        //read the header annotations
        List<Node> headerAnnotations = DomUtils.findAll(domAnnotatedTable, "//columnAnnotations/colAnnos");
        for (int i = 0; i < headerAnnotations.size(); i++) {
            Node header = headerAnnotations.get(i);
            int col = Integer.valueOf(header.getAttributes().getNamedItem("col").getTextContent());
            //TContentCell headerCell = table.getHeaderForColumn(col);
            NodeList annotations = header.getChildNodes();
            List<HeaderAnnotation> hAnnotations = new ArrayList<HeaderAnnotation>();
            for (int j = 0; j < annotations.getLength(); j++) {
                Node n = annotations.item(j);
                if (n.getNodeName().equals("anno")) {
                    HeaderAnnotation a = new HeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                            n.getAttributes().getNamedItem("name").getTextContent(),
                            n.getAttributes().getNamedItem("name").getTextContent(),
                            Double.valueOf(n.getAttributes().getNamedItem("value").getTextContent().trim()));

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
                CellAnnotation cellAnnotation = new CellAnnotation(
                        table.getContentCell(i, j).getText(), new Entity(htmlCell.getTextContent(), htmlCell.getTextContent()), 1.0, new HashMap<String, Double>()
                );

                table.getTableAnnotations().setContentCellAnnotations(
                        i, j, new CellAnnotation[]{cellAnnotation}
                );
            }

        }
        }
        return table;
    }

    private static String extract_text_content_from_html(List<Node> html) {
        String content = html.get(0).getTextContent();
        int start=content.indexOf("<td>");
        if(start==-1)
            start=content.indexOf("<th>");
        if(start==-1)
            start=content.indexOf("<thead>");
        if(start!=-1){
            content=content.substring(start+4);
            int end = content.indexOf("</td>");
            if(end==-1)
                end=content.indexOf("</th>");
            if(end==-1)
                end=content.indexOf("</thead>");
            if(end!=-1)
                content=content.substring(0,end).trim();
        }
        content= StringEscapeUtils.unescapeHtml4(content);
        return content;
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
