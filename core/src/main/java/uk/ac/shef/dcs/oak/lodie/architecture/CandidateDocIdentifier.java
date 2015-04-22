package uk.ac.shef.dcs.oak.lodie.architecture;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 22/10/12
 * Time: 13:21
 */
public interface CandidateDocIdentifier {

    Dataset<Document> findDocs(LearningJob job);
}
