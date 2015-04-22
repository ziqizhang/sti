package uk.ac.shef.dcs.oak.lodie.architecture;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 22/10/12
 * Time: 13:22
 */
public interface SeedIdentifier {

    Dataset<Triple> findTSeed(LearningJob job);

    Dataset<Document> findDSeed(LearningJob job);

}
