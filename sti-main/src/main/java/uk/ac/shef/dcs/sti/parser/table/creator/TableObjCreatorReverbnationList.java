package uk.ac.shef.dcs.sti.parser.table.creator;

import cern.colt.matrix.ObjectMatrix2D;
import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.util.XPathUtils;

/**
 *
 */
public class TableObjCreatorReverbnationList implements TableObjCreator {
    @Override
    public Table create(ObjectMatrix2D preTable, String tableId, String sourceId, TContext... contexts) {
        Table table = new Table(tableId, sourceId, preTable.rows() - 1, preTable.columns());
        for (TContext ctx : contexts)
            table.addContext(ctx);

        //firstly add the header row
        for (int c = 0; c < preTable.columns(); c++) {
            Object o = preTable.get(0, c);
            if (o == null) { //a null value will be inserted by TableHODetector if no user defined header was found
                //todo: header column type
                TColumnHeader header = new TColumnHeader(STIEnum.TABLE_HEADER_UNKNOWN.getValue());
                table.setColumnHeader(c, header);

            } else {
                //todo: header column type
                Node e = (Node) o;
                String text = e.getTextContent();
                String xPath = DomUtils.getXPathForNode(e);

                TColumnHeader header = new TColumnHeader(text);
                header.setHeaderXPath(xPath);
                table.setColumnHeader(c, header);
            }
        }

        //then go thru each other rows
        for (int r = 1; r < preTable.rows(); r++) {
            for (int c = 0; c < preTable.columns(); c++) {
                //get url
                Node e = (Node) preTable.get(r, c);
                String song = findSongNode(e);
                if(song!=null){
                    TCell cell = new TCell(song);

                    table.setContentCell(r - 1, c, cell);
                }
            }
        }

        if (table.getRowXPaths().size() > 0) {
            String rowXPath = table.getRowXPaths().get(0);
            if (rowXPath == null && table.getRowXPaths().size() > 1)
                rowXPath = table.getRowXPaths().get(1);
            if (rowXPath == null) {
            }
            //System.out.println();
            else {
                String tableXPath = XPathUtils.trimXPathLastTag("TABLE", rowXPath);
                table.setTableXPath(tableXPath);
            }
        }

        return table;
    }

    private String findSongNode(Node e) {
        Node div =null;
        for(int i=0; i< e.getChildNodes().getLength(); i++){
            Node n = e.getChildNodes().item(i);
            if(n.getNodeName().equals("DIV")){
                div=n;
                break;
            }
        }
        if(div==null) return null;
        Node song =null;
        for(int i=0; i<div.getChildNodes().getLength(); i++){
            Node n = div.getChildNodes().item(i);
            if(n.getNodeName().equals("DIV")){
                song=n;
                break;
            }
        }
        if(song==null) return null;

        Node textDiv=null;
        for(int i=0; i<song.getChildNodes().getLength();i++){
            textDiv =song.getChildNodes().item(i);
            if(textDiv.getNodeName().equals("DIV")){
                break;
            }
        }

        if(textDiv==null)
            return null;

        for(int i=0; i<textDiv.getChildNodes().getLength();i++){
            Node n =textDiv.getChildNodes().item(i);
            if(n.getNodeName().equals("#text")){
                return n.getTextContent().trim();
            }
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
