package uk.ac.shef.dcs.sti.parser.table;

import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.joox.JOOX.$;

/**
 * Created by - on 26/07/2016.
 */
public class BrowsableHelper {
    static List<String> createBrowsableElements(List<Node> tables, Document doc){
        int count=1;
        List<String> xpaths = new ArrayList<>();
        for(Node tableNode: tables){
            String xpath = $(tableNode).xpath();
            Node prevTableParent = tableNode.getParentNode();
            Node prevTableNextSib=tableNode.getNextSibling();
            Node newTableParent=doc.createElement("div");
            if(prevTableParent!=null) {
                Element checkbox = doc.createElement("input");
                checkbox.setAttribute("type","checkbox");
                checkbox.setAttribute("name","table"+count);
                checkbox.setAttribute("class","targetTables");
                checkbox.setAttribute("checked","true");
                prevTableParent.insertBefore(checkbox, tableNode);

                Element span =doc.createElement("span");
                span.setAttribute("style","background-color:red");
                span.setTextContent("check this box to annotate table#"+count);
                prevTableParent.insertBefore(span, tableNode);

                prevTableParent.removeChild(tableNode);
                newTableParent.appendChild(tableNode);
                if(prevTableNextSib==null)
                    prevTableParent.appendChild(newTableParent);
                else
                    prevTableParent.insertBefore(newTableParent, prevTableNextSib);
            }

            int lastSlash=xpath.lastIndexOf("/TABLE");
            if(lastSlash!=-1){
                String lastSuffix=xpath.substring(lastSlash);
                xpath=xpath.substring(0, lastSlash);
                xpath+="/DIV["+count+"]";//+lastSuffix;
            }

            xpaths.add(xpath);
            count++;
        }
        return xpaths;
    }

    static void output(String inFile, String outputFolder, Document doc) throws STIException {
        File in = new File(inFile);
        File outFile = new File(outputFolder+ File.separator+in.getName());
        if(in.toString().equals(outFile.toString())){
            //rename input file
            in.renameTo(new File(in.toString()+".original"));
        }
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String value=result.getWriter().toString();
            PrintWriter p = new PrintWriter(outFile);
            p.println(value);
            p.close();
        }catch (Exception ioe){
            throw new STIException(ioe);
        }
    }

    static Document createDocument(TagSoupParser parser, String inFile, String sourceId) throws STIException{
        String input;
        try {
            input = FileUtils.readFileToString(new File(inFile));
        } catch (IOException e) {
            throw new STIException(e);
        }
        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId,"UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
        }

        return doc;
    }
}
