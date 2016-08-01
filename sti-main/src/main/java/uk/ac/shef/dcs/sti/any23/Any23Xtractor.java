package uk.ac.shef.dcs.sti.any23;

import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.http.HTTPClient;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.FileDocumentSource;
import org.apache.any23.source.HTTPDocumentSource;
import org.apache.any23.writer.TripleHandlerException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.oak.any23.extension.extractor.LAny23;
import uk.ac.shef.oak.any23.extension.extractor.LNTripleWriter;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 02/11/12
 * Time: 09:33
 */
public class Any23Xtractor {
    private static Any23Xtractor ourInstance;
    private LAny23 runner;

    private static Any23Xtractor getInstance() throws STIException {
        if (ourInstance == null)
            ourInstance = new Any23Xtractor();
        return ourInstance;
    }

    private Any23Xtractor() throws STIException {
        runner = new LAny23("lodie-html-rdfa11", "lodie-html-microdata");
        runner.setHTTPUserAgent("test-user-agent");

    }

    public static List<LTriple> extract(String uri) throws STIException {
        LNTripleWriter handler = null;
        try {
            HTTPClient httpClient = getInstance().runner.getHTTPClient();

            DocumentSource source = new HTTPDocumentSource(
                    httpClient,
                    uri
            );
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            handler = new LNTripleWriter(out);
            getInstance().runner.extract(source, handler);
            return handler.getOutput();
        } catch (IOException ioe) {
            throw new STIException("Any23 cannot obtain " + HTTPClient.class.getName(), ioe);
        } catch (URISyntaxException | ExtractionException use) {
            throw new STIException("Document source error " + uri, use);
        } finally {
            if (handler != null)
                try {
                    handler.close();
                } catch (TripleHandlerException e) {
                }
        }
    }
    public static List<LTriple> extract(File file) throws STIException {
        LNTripleWriter handler = null;
        try {
            DocumentSource source = new FileDocumentSource(
                    file
            );
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            handler = new LNTripleWriter(out);
            getInstance().runner.extract(source, handler);
            return handler.getOutput();
        } catch (IOException ioe) {
            throw new STIException("Any23 cannot obtain " + HTTPClient.class.getName(), ioe);
        } catch (ExtractionException ee) {
            throw new STIException("Document source error " + file, ee);
        } finally {
            if (handler != null)
                try {
                    handler.close();
                } catch (TripleHandlerException e) {
                }
        }
    }

}
