package uk.ac.shef.oak.any23.extension.extractor;

import org.apache.any23.extractor.ExtractionContext;
import org.apache.any23.rdf.RDFUtils;
import org.apache.any23.writer.NTriplesWriter;
import org.apache.any23.writer.TripleHandlerException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 29/10/12
 * Time: 20:39
 */
public class LNTripleWriter extends NTriplesWriter {

    private List<LTriple> triples;

    public LNTripleWriter(OutputStream out) {
        super(out);
        triples = new ArrayList<LTriple>();
    }

    public void receiveTriple(Resource s, URI p, Value o, URI g, ExtractionContext context, String sCtx, String pCtx, String oCtx) throws TripleHandlerException {
        receiveTriple(s, p, o, g, context);
        final URI graph = g == null ? context.getDocumentURI() : g;
        Statement stmt = RDFUtils.quad(s, p, o, graph);
        LTriple triple = new LTriple(stmt);
        triple.setsXPath(sCtx);
        triple.setpXPath(pCtx);
        triple.setoXPath(oCtx);

        triples.add(triple);
    }

    public List<LTriple> getOutput() {
        return triples;
    }
}
