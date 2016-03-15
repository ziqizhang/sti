package uk.ac.shef.dcs.sti.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.any23.Any23Xtractor;
import uk.ac.shef.dcs.sti.rep.LTableContext;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class Table_ContextExtractor_IMDB {


    public static List<LTableContext> extract_tripleContexts(String file, Document doc) throws STIException {
        List<LTriple> triples = Any23Xtractor.extract_from_file(file);

        //triple contexts
        List<LTableContext> contexts = new ArrayList<LTableContext>();
        for (LTriple lt : triples) {
            if (lt.getsXPath() != null && lt.getsXPath().toLowerCase().indexOf("table") != -1)
                continue;
            if (lt.getoXPath() != null && lt.getoXPath().toLowerCase().indexOf("table") != -1)
                continue;
            if (lt.getpXPath() != null && lt.getpXPath().toLowerCase().indexOf("table") != -1)
                continue;

            String obj_string_value = lt.getTriple().getObject().stringValue();
            if (obj_string_value.startsWith("node") || obj_string_value.startsWith("file:/"))
                continue;

            LTableContext ltc = new LTableContext(lt.getTriple().getObject().stringValue(),
                    LTableContext.TableContextType.CAPTION, 1.0);
            contexts.add(ltc);
        }

        contexts.addAll(extract_otherContexts(file,doc));

        return contexts;
    }

    public static List<LTableContext> extract_otherContexts(String file, Document doc) throws STIException {
        //triple contexts
        List<LTableContext> contexts = new ArrayList<LTableContext>();


        //paragraph contexts
        List<Node> paragraphs = DomUtils.findAll(doc, "//DIV[@class='txt-block']");
        //List<Node> paragraphs2 = DomUtils.findAll(doc, "P");
        for (Node n : paragraphs) {
            NodeList children = n.getChildNodes();
            String text = "";
            for (int i = 0; i < children.getLength(); i++) {
                text += children.item(i).getTextContent().trim()+" ";
            }
            text=text.trim();
            if (text.length() < 1)
                continue;
            LTableContext ltc = new LTableContext(text,
                    LTableContext.TableContextType.BEFORE, 1.0);

            contexts.add(ltc);
        }

        List<Node> titles = DomUtils.findAll(doc, "//TITLE");
        for (Node n : titles) {
            String text = n.getTextContent().trim();
            if (text.length() < 1)
                continue;
            LTableContext ltc = new LTableContext(text,
                    LTableContext.TableContextType.PAGETITLE, 1.0);
            contexts.add(ltc);
        }

        return contexts;
    }
}
