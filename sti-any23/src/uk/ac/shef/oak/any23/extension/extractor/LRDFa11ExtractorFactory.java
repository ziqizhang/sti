package uk.ac.shef.oak.any23.extension.extractor;

import org.apache.any23.extractor.ExtractorDescription;
import org.apache.any23.extractor.ExtractorFactory;
import org.apache.any23.extractor.SimpleExtractorFactory;
import org.apache.any23.rdf.PopularPrefixes;
import org.apache.any23.rdf.Prefixes;

import java.util.Arrays;

/**
 * Created by Jan on 10.05.2016.
 */
public class LRDFa11ExtractorFactory
        extends SimpleExtractorFactory<LRDFa11Extractor>
        implements ExtractorFactory<LRDFa11Extractor>{

        public static final String NAME = "lodie-html-rdfa11";

        public static final Prefixes PREFIXES = null;

        private static final ExtractorDescription descriptionInstance = new LRDFa11ExtractorFactory();

        public LRDFa11ExtractorFactory() {
        super(
                LMicrodataExtractorFactory.NAME,
                LMicrodataExtractorFactory.PREFIXES,
                Arrays.asList("text/html;q=0.3", "application/xhtml+xml;q=0.3"),
                "example-rdfa11.html");
    }

        @Override
        public LRDFa11Extractor createExtractor() {
        return new LRDFa11Extractor();
    }

    public static ExtractorDescription getDescriptionInstance() {
        return descriptionInstance;
    }
}
