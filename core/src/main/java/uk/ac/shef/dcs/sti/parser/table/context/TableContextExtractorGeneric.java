package uk.ac.shef.dcs.sti.parser.table.context;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.TContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TableContextExtractorGeneric implements TableContextExtractor{
    public List<TContext> extract(File file, Document doc) throws STIException{
        List<TContext> contexts = new ArrayList<>();

        //paragraph contexts
        List<Node> paragraphs = DomUtils.findAll(doc, "//P");
        List<Node> paragraphs2 = DomUtils.findAll(doc, "P");
        if(paragraphs.size()<paragraphs2.size())
            paragraphs=paragraphs2;
        for(Node n: paragraphs){
            String text  =n.getTextContent().trim();
            if(text.length()<1)
                continue;
            TContext ltc = new TContext(text,
                    TContext.TableContextType.PARAGRAPH_BEFORE,1.0);
            contexts.add(ltc);
        }

        List<Node> titles = DomUtils.findAll(doc, "//TITLE");
        for(Node n: titles){
            String text  =n.getTextContent().trim();
            if(text.length()<1)
                continue;
            TContext ltc = new TContext(text,
                    TContext.TableContextType.PAGETITLE,1.0);
            contexts.add(ltc);
        }
        return contexts;
    }

}
