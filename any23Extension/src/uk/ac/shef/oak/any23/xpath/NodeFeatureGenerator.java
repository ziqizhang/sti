package uk.ac.shef.oak.any23.xpath;


import java.io.IOException;

import uk.ac.shef.oak.any23.extension.extractor.LTriple;


public interface NodeFeatureGenerator {

    void extratFeatures(LTriple lt);

    void output(String destination) throws IOException;
}
