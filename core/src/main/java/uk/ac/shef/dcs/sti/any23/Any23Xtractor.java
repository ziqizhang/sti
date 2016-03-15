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

    public static List<LTriple> extract_from_url(String link) throws STIException {
        LNTripleWriter handler = null;
        try {
            HTTPClient httpClient = getInstance().runner.getHTTPClient();

            DocumentSource source = new HTTPDocumentSource(
                    httpClient,
                    link
            );
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            handler = new LNTripleWriter(out);
            getInstance().runner.extract(source, handler);
            return handler.getOutput();
        } catch (IOException ioe) {
            throw new STIException("Any23 cannot obtain " + HTTPClient.class.getName(), ioe);
        } catch (URISyntaxException use) {
            throw new STIException("Document source error " + link, use);
        } catch (ExtractionException ee) {
            throw new STIException("Document source error " + link, ee);
        } finally {
            if (handler != null)
                try {
                    handler.close();
                } catch (TripleHandlerException e) {
                }
        }
    }
    public static List<LTriple> extract_from_file(String file) throws STIException {
        LNTripleWriter handler = null;
        try {
            HTTPClient httpClient = getInstance().runner.getHTTPClient();

            DocumentSource source = new FileDocumentSource(
                    new File(file)
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

    public static void main(String[] args) throws STIException {
        List<LTriple> triples = Any23Xtractor.extract_from_url("http://www.bbc.co.uk/music/artists/650e7db6-b795-4eb5-a702-5ea2fc46c848");
        for(LTriple t: triples){
            //if(t.getsXPath().contains("TABLE"))
                System.out.println(t+"\t\t\t"+t.getoXPath());
        }
    }
}
