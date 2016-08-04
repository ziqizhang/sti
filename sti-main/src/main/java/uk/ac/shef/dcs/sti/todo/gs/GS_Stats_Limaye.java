package uk.ac.shef.dcs.sti.todo.gs;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.util.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.core.model.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**

 */
public class GS_Stats_Limaye {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        String cleanTableRepos = args[0];
        String annotationRepos = args[1].replaceAll("\\\\", "/");

        File[] files = FileUtils.listFilesRecursively(new File(cleanTableRepos), new SuffixFileFilter(".xml"));
        for (File clean : files) {
            String path = clean.getAbsolutePath().replaceAll("\\\\", "/");
            String relative = path.substring(cleanTableRepos.length());

            String annotated = annotationRepos + relative;
            if (!(new File(annotated)).exists()) {
                System.err.println("clean file for annotation does not exist: " + annotated);
                continue;
            }

            System.out.println(clean);

            readTable(clean.toString(), annotated.toString(),"E:\\Data\\table annotation\\workspace\\WWT_GroundTruth\\annotation/stats.csv");
        }

        /*Table table = readTable("E:\\data\\table annotation\\tablesForAnnotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
                "E:\\data\\table annotation\\workspace\\WWT_GroundTruth\\annotation\\wikitables\\c3\\r12\\y\\e\\l/Yellowknife.html_0.xml",
                "D:\\work\\lodiedata\\limayetable");*/
        System.out.println();
    }

    public static Table readTable(String tableFilename, String tableAnnotationFilename,
                                   String stats_file) throws IOException, ParserConfigurationException, SAXException {
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

        Table table = null;
        int rowModifier = 0;
        if (firstRowHeader) {
            table = new Table(String.valueOf(tableFilename.hashCode()), tableFilename, rows.size() - 1, totalCol);
            rowModifier = 1;
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
            for (int c = 0; c < cells.length; c++) {
                TCell cell = new TCell(cells[c]);
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
                        TContext ctx=null;
                        if(i==1)
                            ctx=new TContext(context, TContext.TableContextType.PAGETITLE,1.0);
                        else ctx=new TContext(context, TContext.TableContextType.PARAGRAPH_BEFORE, 1.0);

                        table.addContext(ctx);
                    }
                }
            }
        }
        if(table.getContexts().size()>1)
            table.getContexts().remove(1);  //always isValidAttribute the 2nd context as it is the header of the table

        //dump the original html snippet to a human readable html format

        if(tableAnnotationFilename==null)
            return table;

        Document domAnnotatedTable = docBuilder.parse(tableAnnotationFilename);

        //read the header annotations
        List<Node> headerAnnotations = DomUtils.findAll(domAnnotatedTable, "//columnAnnotations/colAnnos");
        String line="";
        line=String.valueOf(headerAnnotations.size())+",";
        for (int i = 0; i < headerAnnotations.size(); i++) {
            Node header = headerAnnotations.get(i);
            int col = Integer.valueOf(header.getAttributes().getNamedItem("col").getTextContent());
            //TCell headerCell = table.getHeaderForColumn(col);
            NodeList annotations = header.getChildNodes();
            List<TColumnHeaderAnnotation> hAnnotations = new ArrayList<TColumnHeaderAnnotation>();
            for (int j = 0; j < annotations.getLength(); j++) {
                Node n = annotations.item(j);
                if (n.getNodeName().equals("anno")) {
                    TColumnHeaderAnnotation a = new TColumnHeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                            new Clazz(n.getAttributes().getNamedItem("name").getTextContent(),
                            n.getAttributes().getNamedItem("name").getTextContent()),
                            Double.valueOf(n.getAttributes().getNamedItem("value").getTextContent().trim()));

                    hAnnotations.add(a);
                }
            }
            table.getTableAnnotations().setHeaderAnnotation(col, hAnnotations.toArray(new TColumnHeaderAnnotation[0]));
        }
        //read the data rows annotations
        List<Node> dataRowAnnotations = DomUtils.findAll(domAnnotatedTable, "//cellAnnotatoons/row");
        int countCellAnnotations=0;
        for (int i = 0; i < dataRowAnnotations.size(); i++) {
            Node row = dataRowAnnotations.get(i);
            List<Node> cols = DomUtils.findAll(row, "entity");
            for (int j = 0; j < cols.size(); j++) {
                Node htmlCell = cols.get(j);
                if (htmlCell.getTextContent() == null || htmlCell.getTextContent().length() == 0) {
                    continue;
                }
                /*TCellAnnotation cellAnnotation = new TCellAnnotation(
                        table.getContentCell(i + 1, j).getText(), new EntityCandidate(htmlCell.getTextContent(), htmlCell.getTextContent()), 1.0,new HashMap<String, Double>()
                );*/

                countCellAnnotations++;
                /*table.getTableAnnotations().setContentCellAnnotations(
                        i + 1, j, new TCellAnnotation[]{cellAnnotation}
                );*/
            }
        }

        line=line+countCellAnnotations;
        PrintWriter p =new PrintWriter(new FileWriter(stats_file,true));
        p.println(line);
        p.close();


        return table;
    }
}
