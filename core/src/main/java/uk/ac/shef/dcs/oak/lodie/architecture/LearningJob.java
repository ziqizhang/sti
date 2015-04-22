package uk.ac.shef.dcs.oak.lodie.architecture;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 22/10/12
 * Time: 11:17
 *
 * A LearningJob is a single binary relation to be learned by IE. The relation can be constrained on either
 * subject or object.
 * - When subjects and objects are both empty, it means learn the relation regardless of the subject/object
 * - When either subjects or objects are provided, it means learn the relation with constraints on subjects/objects
 *      such that the subjects/objects can only take on those values
 * - Subjects and objects cannot be constrained at the same time
 */
public class LearningJob {

    private String description;   //NL description of what to learn
    private List<String> constraints;
    private String predicate;
    private boolean subjectOpen;
    private boolean objectOpen;

    public LearningJob(String predicate, boolean subjectOpen, boolean objectOpen) throws LodieException{
        if(!subjectOpen&&!objectOpen)
            throw new LodieException("Invalid LearningJob definition. Either subject or object must be open.");

        this.predicate=predicate;
        this.subjectOpen=subjectOpen;
        this.objectOpen=objectOpen;
        constraints=new ArrayList<String>();
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void addConstraints(List<String> constraints) {
        this.constraints.addAll(constraints);
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public boolean isSubjectOpen() {
        return subjectOpen;
    }

    public void setSubjectOpen(boolean subjectOpen) {
        this.subjectOpen = subjectOpen;
    }

    public boolean isObjectOpen() {
        return objectOpen;
    }

    public void setObjectOpen(boolean objectOpen) {
        this.objectOpen = objectOpen;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
