package uk.ac.shef.dcs.sti.parser.table.context;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.any23.Any23Xtractor;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class TableContextExtractorIMDB implements TableContextExtractor{


    public List<TContext> extract(File file, Document doc) throws STIException {
        List<LTriple> triples = Any23Xtractor.extract(file);

        //triple contexts
        List<TContext> contexts = new ArrayList<>();
        for (LTriple lt : triples) {
            if (lt.getsXPath() != null && lt.getsXPath().toLowerCase().contains("table"))
                continue;
            if (lt.getoXPath() != null && lt.getoXPath().toLowerCase().contains("table"))
                continue;
            if (lt.getpXPath() != null && lt.getpXPath().toLowerCase().contains("table"))
                continue;

            String obj_string_value = lt.getTriple().getObject().stringValue();
            if (obj_string_value.startsWith("node") || obj_string_value.startsWith("file:/"))
                continue;

            TContext ltc = new TContext(lt.getTriple().getObject().stringValue(),
                    TContext.TableContextType.CAPTION, 1.0);
            contexts.add(ltc);
        }

        contexts.addAll(extractOtherContexts(doc));

        return contexts;
    }

    private List<TContext> extractOtherContexts(Document doc) throws STIException {
        List<TContext> contexts = new ArrayList<>();

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
            TContext ltc = new TContext(text,
                    TContext.TableContextType.PARAGRAPH_BEFORE, 1.0);

            contexts.add(ltc);
        }

        List<Node> titles = DomUtils.findAll(doc, "//TITLE");
        for (Node n : titles) {
            String text = n.getTextContent().trim();
            if (text.length() < 1)
                continue;
            TContext ltc = new TContext(text,
                    TContext.TableContextType.PAGETITLE, 1.0);
            contexts.add(ltc);
        }

        return contexts;
    }
}
