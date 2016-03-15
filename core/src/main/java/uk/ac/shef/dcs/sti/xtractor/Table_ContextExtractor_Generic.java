package uk.ac.shef.dcs.sti.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.rep.LTableContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 31/03/14
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class Table_ContextExtractor_Generic {
    public static List<LTableContext> extractTableContexts(String file, Document doc) throws STIException {

        //triple contexts
        List<LTableContext> contexts = new ArrayList<LTableContext>();

        //paragraph contexts
        List<Node> paragraphs = DomUtils.findAll(doc, "//P");
        List<Node> paragraphs2 = DomUtils.findAll(doc, "P");
        if(paragraphs.size()<paragraphs2.size())
            paragraphs=paragraphs2;
        for(Node n: paragraphs){
            String text  =n.getTextContent().trim();
            if(text.length()<1)
                continue;
            LTableContext ltc = new LTableContext(text,
                    LTableContext.TableContextType.BEFORE,1.0);
            contexts.add(ltc);
        }

        List<Node> titles = DomUtils.findAll(doc, "//TITLE");
        for(Node n: titles){
            String text  =n.getTextContent().trim();
            if(text.length()<1)
                continue;
            LTableContext ltc = new LTableContext(text,
                    LTableContext.TableContextType.PAGETITLE,1.0);
            contexts.add(ltc);
        }
        return contexts;
    }

    public static List<LTableContext> extractTableContexts_generic_everything(String file, Document doc) throws STIException {
        //triple contexts
        List<LTableContext> contexts = new ArrayList<LTableContext>();

        //paragraph contexts
        List<Node> paragraphs = DomUtils.findAll(doc, "//TABLE");
        List<Node> paragraphs2 = DomUtils.findAll(doc, "TABLE");
        if(paragraphs.size()<paragraphs2.size())
            paragraphs=paragraphs2;
        for(Node n: paragraphs){
            String text  =n.getTextContent().trim();
            if(text.length()<1)
                continue;
            LTableContext ltc = new LTableContext(text,
                    LTableContext.TableContextType.BEFORE,1.0);
            contexts.add(ltc);
        }

        List<Node> titles = DomUtils.findAll(doc, "//TITLE");
        for(Node n: titles){
            String text  =n.getTextContent().trim();
            if(text.length()<1)
                continue;
            LTableContext ltc = new LTableContext(text,
                    LTableContext.TableContextType.PAGETITLE,1.0);
            contexts.add(ltc);
        }
        return contexts;
    }


}
