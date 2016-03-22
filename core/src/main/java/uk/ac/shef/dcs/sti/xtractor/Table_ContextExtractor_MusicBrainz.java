package uk.ac.shef.dcs.sti.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.any23.Any23Xtractor;
import uk.ac.shef.dcs.sti.rep.TContext;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 20/02/14
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
public class Table_ContextExtractor_MusicBrainz {

    public static List<TContext> extractTableContexts(String file, Document doc) throws STIException {
        List<LTriple> triples = Any23Xtractor.extract_from_file(file);

        //triple contexts
        List<TContext> contexts = new ArrayList<TContext>();
        for(LTriple lt : triples){
            if(lt.getsXPath()!=null&&lt.getsXPath().toLowerCase().indexOf("table")!=-1)
                continue;
            if(lt.getoXPath()!=null&&lt.getoXPath().toLowerCase().indexOf("table")!=-1)
                continue;
            if(lt.getpXPath()!=null&&lt.getpXPath().toLowerCase().indexOf("table")!=-1)
                continue;

            String obj_string_value = lt.getTriple().getObject().stringValue();
            if(obj_string_value.startsWith("node")||obj_string_value.startsWith("file:/")||obj_string_value.startsWith("http://"))
                continue;

            TContext ltc = new TContext(lt.getTriple().getObject().stringValue(),
                    TContext.TableContextType.CAPTION, 1.0);
            contexts.add(ltc);
        }

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
                    TContext.TableContextType.BEFORE,1.0);
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
