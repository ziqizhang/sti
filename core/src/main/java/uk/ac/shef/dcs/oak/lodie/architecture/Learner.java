package uk.ac.shef.dcs.oak.lodie.architecture;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 22/10/12
 * Time: 16:44
 */
public abstract class Learner implements Runnable{

    protected Dataset<AnnodatedDocument> annotations;
    protected Dataset<Triple> triples;

    protected CandidateDocIdentifier candDocIdentifier;
    protected SeedIdentifier seedIdentifier;

    public Learner(CandidateDocIdentifier candDocIdentifier, SeedIdentifier seedIdentifier){
        this.candDocIdentifier=candDocIdentifier;
        this.seedIdentifier=seedIdentifier;
    }

    public abstract void learn();

    public Dataset<AnnodatedDocument> getNewAnnotations(){
        return annotations;
    }

    public Dataset<Triple> getNewTriples(){
        return triples;
    }


}
