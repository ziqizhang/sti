package uk.ac.shef.dcs.sti.todo.evaluation;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.List;

/**
 */
public class KeyFileGenerator_from_HTMLOutput {
    protected TagSoupParser parser;

    public static void main(String[] args) throws FileNotFoundException {
        KeyFileGenerator_from_HTMLOutput generator = new KeyFileGenerator_from_HTMLOutput();
        String inFolder = "E:\\Data\\table annotation\\freebase_crawl\\music_record_label\\musicbrainz_computed";
        String outFolder = "E:\\Data\\table annotation\\freebase_crawl\\music_record_label\\musicbrainz_computed_reformatted";
        //String inFolder = "E:\\Data\\table annotation\\freebase_crawl\\film_film\\imdb_computed";
        //String outFolder = "E:\\Data\\table annotation\\freebase_crawl\\film_film\\imdb_compted_reformatted";
        for (File f : new File(inFolder).listFiles()) {
            if (f.toString().endsWith("attributes.html"))
                continue;
            generator.createKeyFiles(f.toString(), outFolder);
        }
    }

    public void createKeyFiles(String inFile, String outFolder) throws FileNotFoundException {
        parser = new TagSoupParser(new FileInputStream(inFile), inFile);
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f = new File(inFile);
        System.out.println(f);
        String name = f.getName();
        if (name.endsWith("htm.html"))
            name = name.substring(0, name.indexOf(".html")).trim();
        String file_header_annotation = outFolder + File.separator + name + ".header.keys";
        String file_relation_annotation = outFolder + File.separator + name + ".relation.keys";
        String file_entity_annotation = outFolder + File.separator + name + ".entity.keys";

        List<Node> table = DomUtils.findAll(doc, "//TABLE");
        Node firstTable = table.get(0);
        Node secondTable = table.get(1);

        generateEntity_annotationKeys(firstTable, file_entity_annotation);
        generateHeader_annotationKeys(firstTable, file_header_annotation);
        generateRelation_annotationKeys(secondTable, file_relation_annotation);
    }

    public void generateEntity_annotationKeys(Node table, String outFile) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(outFile);
        List<Node> first_table_rows = DomUtils.findAll(table, "//TR");
        int count_row = -2;
        boolean prevTableFound = false;
        boolean tableEnds = false;
        for (Node row : first_table_rows) {
            count_row++;
            Node current_row = row;

            if (current_row.getChildNodes().item(1).getNodeName().equalsIgnoreCase("th")) {
                if (!prevTableFound) {
                    prevTableFound = true;
                    continue;
                } else {
                    break;
                }
            }

            //DomUtils.findAll(current_row,"/TH")

            NodeList rowElements = current_row.getChildNodes();
            int count_column = -1, count_white_columns = 0;
            String cellText = null, cellAnnotation = null;

            for (int i = 0; i < rowElements.getLength(); i++) {
                Node cell = rowElements.item(i);
                if (cell.getNodeType() == 3 || !cell.getNodeName().equals("TD"))
                    continue;

                Node color = cell.getAttributes() == null ? null : cell.getAttributes().getNamedItem("bgcolor");
                if (color == null) {
                    //this is the text column
                    if (cell.getNodeName().equals("TD")) {
                        count_column++;
                        cellText = cell.getTextContent();
                        if (cellText.indexOf("[") != -1 && cellText.indexOf("]") != -1)
                            count_white_columns++;
                    }
                } else {
                    //this is the annotation column
                    if (cell.getNodeName().equals("TD")) {
                        count_column++;
                        Node linkNode = null;
                        try {
                            linkNode = cell.getChildNodes().item(1).getChildNodes().item(0);
                        } catch (NullPointerException npe) {
                        }

                        if (linkNode != null) {
                            String link = linkNode.getAttributes() == null ? null : linkNode.getAttributes().getNamedItem("href").getTextContent();
                            cellAnnotation = link;
                            cellAnnotation = cellAnnotation.substring(23);
                        }

                        int revised_column = count_column - count_white_columns;
                        if (cellAnnotation != null)
                            p.println(count_row + "," + revised_column + "=" + cellAnnotation);
                    }
                }
            }

            if (tableEnds)
                break;
        }
        p.close();
    }

    public int generateRelation_annotationKeys(Node table, String outFile) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(outFile);
        List<Node> table_rows = DomUtils.findAll(table, "//TR");
        int mainCol = -1;
        int startRow = -1;

        for (int nodeIndex = 0; nodeIndex < table_rows.size(); nodeIndex++) {
            Node row = table_rows.get(nodeIndex);
            NodeList cells = row.getChildNodes();

            int count_col = -1, count_white_col=0;
            for (int i = 0; i < cells.getLength(); i++) {
                Node cell = cells.item(i);
                if (!cell.getNodeName().equals("TH"))
                    continue;
                count_col++;

                Node color = cell.getAttributes() == null ? null : cell.getAttributes().getNamedItem("bgcolor");
                if (color == null){
                    String headerText = cell.getTextContent().trim();
                    if (!headerText.equals("-"))
                        count_white_col++;
                    continue;
                }
                String colorCode = color.getTextContent();
                if (colorCode.equals("yellow")) {
                    mainCol = count_col-count_white_col;
                    startRow = nodeIndex;
                    break;
                }
            }

            if (mainCol != -1)
                break;
        }
        Node second_table_header = table_rows.get(startRow);
        NodeList
                headerElements = second_table_header.getChildNodes();
        int count_header_col = -1, count_white_col = 0;
        String headerText = null, headerAnnotation = null;
        for (int i = 0; i < headerElements.getLength(); i++) {
            Node header = headerElements.item(i);
            if (!header.getNodeName().equals("TH"))
                continue;
            count_header_col++;

            Node color = header.getAttributes() == null ? null : header.getAttributes().getNamedItem("bgcolor");
            if (color == null) {
                //this is the text column
                headerText = header.getTextContent().trim();
                if (!headerText.equals("-"))
                    count_white_col++;
            } else {
                if(color.getTextContent().equals("yellow")){
                    count_header_col--;
                    continue;
                }

                //this is the annotation column
                headerAnnotation = header.getTextContent();

                headerAnnotation = exractAnnotation(headerAnnotation);
                int revisedCol =count_header_col-count_white_col;
                p.println(mainCol + "," + revisedCol + "=" + headerAnnotation);

            }
        }
        p.close();
        return mainCol;
    }

    public void generateHeader_annotationKeys(Node table, String outFile) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(outFile);
        List<Node> first_table_rows = DomUtils.findAll(table, "//TR");
        Node first_table_header = first_table_rows.get(0);

        NodeList headerElements = first_table_header.getChildNodes();
        int count_header_col = -1, count_white_col = 0;
        String headerText = null, headerAnnotation = null;
        for (int i = 0; i < headerElements.getLength(); i++) {
            Node header = headerElements.item(i);
            if (!header.getNodeName().equalsIgnoreCase("th"))
                continue;

            count_header_col++;

            Node color = header.getAttributes() == null ? null : header.getAttributes().getNamedItem("bgcolor");
            if (color == null) {
                //this is the text column
                headerText = header.getTextContent();
                if (!headerText.trim().equals("-"))
                    count_white_col++;

            } else {
                //this is the annotation column
                headerAnnotation = header.getTextContent();

                int current_real_col = count_header_col - count_white_col;
                headerAnnotation = exractAnnotation(headerAnnotation);
                p.println(current_real_col + "=" + headerAnnotation);

            }
        }
        p.close();
    }

    private String exractAnnotation(String cellText) {
        String value = cellText.split("=")[0].trim();
        int end = value.indexOf("(");
        end=end==-1?value.length():end;
        value = value.substring(0, end).trim();
        return value;
    }

}
