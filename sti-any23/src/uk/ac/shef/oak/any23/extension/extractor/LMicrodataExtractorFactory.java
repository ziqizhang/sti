package uk.ac.shef.oak.any23.extension.extractor;

import java.util.Arrays;

import org.apache.any23.extractor.ExtractorDescription;
import org.apache.any23.extractor.ExtractorFactory;
import org.apache.any23.extractor.SimpleExtractorFactory;
import org.apache.any23.rdf.PopularPrefixes;
import org.apache.any23.rdf.Prefixes;

/**
 * Created by Jan on 10.05.2016.
 */
public class LMicrodataExtractorFactory extends SimpleExtractorFactory<LMicrodataExtractor> implements
        ExtractorFactory<LMicrodataExtractor> {

    public static final String NAME = "lodie-html-microdata";

    public static final Prefixes PREFIXES = PopularPrefixes.createSubset("rdf", "doac", "foaf");

    private static final ExtractorDescription descriptionInstance = new LMicrodataExtractorFactory();

    public LMicrodataExtractorFactory() {
        super(
                LMicrodataExtractorFactory.NAME,
                LMicrodataExtractorFactory.PREFIXES,
                Arrays.asList("text/html;q=0.1", "application/xhtml+xml;q=0.1"),
                "example-microdata.html");
    }

    @Override
    public LMicrodataExtractor createExtractor() {
        return new LMicrodataExtractor();
    }

    public static ExtractorDescription getDescriptionInstance() {
        return descriptionInstance;
    }
}
