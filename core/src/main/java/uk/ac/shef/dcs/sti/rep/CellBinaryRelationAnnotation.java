package uk.ac.shef.dcs.sti.rep;

import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * annotates relation between two cells on the same row. Annotations on multi rows must be aggregated to derive an annotation for
 * two columns
 */
public class CellBinaryRelationAnnotation implements Serializable, Comparable<CellBinaryRelationAnnotation> {

    private Key_SubjectCol_ObjectCol subject_object_key;
    private int row;

    private String annotation_url;
    private String annotation_label;

    private List<Attribute> matched_values;
    private double score;

    private boolean dummy; //true if this relation is created as a dummy relation, i.e., not supported by evidence in the KB


    //matched_value[]: (0)=property name (1)=the attribute value matched with the objecCol field on this row; (2) id/uri, if any (used for later knowledge base retrieval)
    public CellBinaryRelationAnnotation(Key_SubjectCol_ObjectCol key,
                                        int row,
                                        String relation_annotation,
                                        String relation_label,
                                        List<Attribute> matched_values, double score){
        this.subject_object_key=key;
        this.row=row;
        this.annotation_url = relation_annotation;
        this.annotation_label=relation_label;
        this.score=score;
        this.matched_values=matched_values;
    }

    public double getScore(){
        return score;
    }
    public void setScore(double score){
        this.score=score;
    }



    public int getRow(){
        return row;
    }

    public String getAnnotation_url(){
        return annotation_url;
    }

    public boolean equals(Object o){
        if(o instanceof CellBinaryRelationAnnotation){
            CellBinaryRelationAnnotation that = (CellBinaryRelationAnnotation) o;
            return that.getSubject_object_key().equals(getSubject_object_key())
                    &&that.getRow()==getRow()
                    &&that.getAnnotation_url().equals(getAnnotation_url());
        }
        return false;
    }

    public int hashCode(){
        return getSubject_object_key().hashCode()+19*getRow()+29* getAnnotation_url().hashCode();
    }

    @Override
    public int compareTo(CellBinaryRelationAnnotation o) {
        int compared = new Integer(o.getRow()).compareTo(getRow());

        if(compared==0)
            return new Double(o.getScore()).compareTo(getScore());

        return compared;
    }

    public Key_SubjectCol_ObjectCol getSubject_object_key() {
        return subject_object_key;
    }

    public void setSubject_object_key(Key_SubjectCol_ObjectCol subject_object_key) {
        this.subject_object_key = subject_object_key;
    }

    public List<Attribute> getMatched_values() {
        return matched_values;
    }

    public void setMatched_values(List<Attribute> matched_values) {
        this.matched_values = matched_values;
    }

    public void addMatched_values(List<Attribute> values_to_add){
        for(Attribute vta: values_to_add){
            if(!matched_values.contains(vta))
                matched_values.add(vta);
        }

         //System.out.println(matched_values.size());
    }

    public boolean isDummy() {
        return dummy;
    }

    public void setDummy(boolean dummy) {
        this.dummy = dummy;
    }

    public String toString(){
        return annotation_url;
    }

    public String getAnnotation_label() {
        return annotation_label;
    }

    public void setAnnotation_label(String annotation_label) {
        this.annotation_label = annotation_label;
    }
}
