package uk.ac.shef.dcs.sti.xtractor;

import cern.colt.matrix.ObjectMatrix2D;
import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.shef.dcs.sti.PlaceHolder;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;
import uk.ac.shef.dcs.util.StringUtils;

import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/10/12
 * Time: 15:41
 * <p/>
 * todo: xpaths for table cells not extracted by this class
 * todo: debug this class
 */
public class TableObjCreatorWikipediaGS implements TableObjCreator {

    private boolean only_take_first_link_in_list_like_cell = false;

    public TableObjCreatorWikipediaGS(boolean only_take_first_link_in_list_like_cell) {
        this.only_take_first_link_in_list_like_cell = only_take_first_link_in_list_like_cell;
    }

    @Override
    public LTable create(ObjectMatrix2D preTable, String tableId, String sourceId, LTableContext... context) {
        LTable table = new LTable(tableId, sourceId, preTable.rows() - 1, preTable.columns());

        for (LTableContext ctx : context)
            table.addContext(ctx);

        //firstly add the header row
        for (int c = 0; c < preTable.columns(); c++) {
            Object o = preTable.get(0, c);
            if (o == null) { //a null value will be inserted by TableHODetector if no user defined header was found
                //todo:header type
                LTableColumnHeader header = new LTableColumnHeader(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue());
                table.setColumnHeader(c, header);
            } else {
                Node e = (Node) o;
                String text = e.getTextContent();
                String xPath = DomUtils.getXPathForNode(e);

                LTableColumnHeader header = new LTableColumnHeader(text);
                header.setHeaderXPath(xPath);

                //set header text
                table.setColumnHeader(c, header);

                //now check if for this header there are any annotations (i.e., links)
                List<HeaderAnnotation> annotations = new ArrayList<HeaderAnnotation>();
                List<Node> it = DomUtils.findAllByTag(e, "A");
                if (it.size() > 0) {
                    for (Node ahref : it) {
                        if (ahref.getParentNode().getNodeName().equalsIgnoreCase("sub") || ahref.getParentNode().getNodeName().equalsIgnoreCase("sup"))
                            continue;
                        String uri = null;
                        try {
                            uri = ahref.getAttributes().getNamedItem("href").getNodeValue();
                        } catch (NullPointerException n) {
                        }
                        ;

                        String linkText = ahref.getTextContent();
                        if (linkText.length() == 0)
                            continue;

                        annotations.add(new HeaderAnnotation(linkText, uri, uri, 1.0));
                    }
                }
                //set header annotation
                table.getTableAnnotations().setHeaderAnnotation(c, annotations.toArray(new HeaderAnnotation[0]));
            }

        }

        //then go thru each other rows to extract content cells
        for (int r = 1; r < preTable.rows(); r++) {
            for (int c = 0; c < preTable.columns(); c++) {
                Node e = (Node) preTable.get(r, c);
                extract(e, r, c, table);
            }
        }

        return table;
    }


    private void extract(Node tableCell, int r, int c, LTable table) {
        //todo: cell type
        String cellText = getCellTextOfNode_by_links(tableCell);

        String edited = "";
        for (int i = 0; i < cellText.length(); i++) {
            if (cellText.charAt(i) == 8211) {
                edited += "-";
            } else {
                edited += cellText.charAt(i);
            }
        }
        cellText = edited;


        LTableContentCell cell = new LTableContentCell(cellText);
        r = r - 1;
        table.setContentCell(r, c, cell);

        LinkedHashSet<CellAnnotation> wikiAnnotations = new LinkedHashSet<CellAnnotation>();
        //firstly always add the entire text string in this cell
        List<Node> it = DomUtils.findAllByTag(tableCell, "A");
        if (it.size() > 0) {
            for (Node ahref : it) {
                if (ahref.getParentNode().getNodeName().equalsIgnoreCase("sub") || ahref.getParentNode().getNodeName().equalsIgnoreCase("sup"))
                    continue;
                String uri = null;
                try {
                    uri = ahref.getAttributes().getNamedItem("href").getNodeValue();
                } catch (NullPointerException n) {
                }
                ;

                String text = ahref.getTextContent();
                if (text.length() == 0)
                    continue;

                wikiAnnotations.add(new CellAnnotation
                        (text, new Entity(uri, uri), 1.0, new HashMap<String, Double>()));
                if (only_take_first_link_in_list_like_cell)
                    break;

            }

        }

        /*    it = DomUtils.findAllByTag(tableCell,"LI");
        if (it.size()>0) {
            for(Node listItem: it) {
                String value = listItem.getTextContent();
                if(value.trim().length()>0)
                    texts.add(value.trim());
            }
        }*/

        table.getTableAnnotations().setContentCellAnnotations(r, c, wikiAnnotations.toArray(new CellAnnotation[0]));

    }

    private String getCellTextOfNode_by_links(Node tableCell) {
        NodeList nl = tableCell.getChildNodes();
        String concatenated_content = "";
        int multiLink = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName() != null && n.getNodeName().equalsIgnoreCase("A")) {
                concatenated_content = concatenated_content + n.getTextContent() + "|";
                multiLink++;
            }/*else if(n.getNodeName()!=null &&n.getNodeName().equalsIgnoreCase("SPAN")){
                try{
                    String cl=n.getAttributes().getNamedItem("class").getTextContent();
                    if(cl.equals("sortkey")){}
                    else
                        concatenated_content = n.getTextContent();
                }catch (Exception e){};
            }*/
        }
        if (concatenated_content.length() == 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeName() != null && !n.getNodeName().equalsIgnoreCase("SUP") && !n.getNodeName().equalsIgnoreCase("SUB")) {
                    if (n.getNodeName() != null && n.getNodeName().equalsIgnoreCase("SPAN")) {
                        try {
                            String cl = n.getAttributes().getNamedItem("class").getTextContent();
                            if (cl.equals("sortkey")) {
                            } else
                                concatenated_content = n.getTextContent();
                        } catch (Exception e) {
                        }
                        ;
                    } else {
                        concatenated_content = concatenated_content + n.getTextContent() + "|";
                    }
                }
            }
        }

        if (concatenated_content.endsWith("|"))
            concatenated_content = concatenated_content.substring(0, concatenated_content.length() - 1).trim();

        if (only_take_first_link_in_list_like_cell && multiLink > 1) {
            //test if link structure
            String cellText = tableCell.getTextContent().replaceAll("\\|", " ");
//            String concatenated = concatenated_content;
            String extractedText = StringUtils.toAlphaNumericWhitechar(concatenated_content);
            List<String> cellTextTokens = new ArrayList<String>(Arrays.asList(StringUtils.toAlphaNumericWhitechar(cellText).split("\\s+")));
            List<String> extractedTextTokens = new ArrayList<String>(Arrays.asList(extractedText.split("\\s+")));
            extractedTextTokens.retainAll(cellTextTokens);
            if (extractedTextTokens.size() / (double) cellTextTokens.size() > 0.9) {
                return concatenated_content.split("\\|")[0].trim();
            }
        }

        return concatenated_content.replaceAll("\\|", ", ");
    }

}
