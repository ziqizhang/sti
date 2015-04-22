package uk.ac.shef.dcs.oak.triplesearch;


/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 21/09/12
 * Time: 14:23
 *
 * A simple representation of a triple as three string objects
 */
public class Triple {
    private String subject;
    private String predicate;
    private String object;

    public Triple(String s, String p, String o){
        subject=s;
        predicate=p;
        object=o;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public int hashCode(){
        return subject.hashCode()+predicate.hashCode()*19+object.hashCode()*29;
    }

    public boolean equals(Object o){
        if(o instanceof Triple){
            Triple t = (Triple) o;
            return t.getSubject().equals(getSubject())&&t.getPredicate().equals(getPredicate())
                    &&t.getObject().equals(getObject());
        }
        return false;
    }

    public String toString(){
        return subject+" "+predicate+" "+object;
    }
}
