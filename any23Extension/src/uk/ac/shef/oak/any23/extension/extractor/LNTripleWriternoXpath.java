package uk.ac.shef.oak.any23.extension.extractor;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.any23.extractor.ExtractionContext;
import org.apache.any23.writer.NTriplesWriter;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.any23.writer.Writer;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import uk.ac.shef.dcs.oak.triplesearch.Triple;

/**
 * @author annalisa
 */

@Writer(identifier = "lodie-triples", mimeType = "text/plain")
public class LNTripleWriternoXpath extends NTriplesWriter {

    private List<Triple> triples;

    public LNTripleWriternoXpath(OutputStream out) {
        super(out);
        triples = new ArrayList<Triple>();
    }

    public void receiveTriple(Resource s, URI p, Value o, URI g, ExtractionContext context) throws TripleHandlerException {
    	super.receiveTriple(s, p, o, g, context);
       
        Triple triple = new Triple(s.stringValue(),p.stringValue(),o.stringValue());
        triples.add(triple);
    }

    public List<Triple> getOutput() {
        return triples;
    }
}
