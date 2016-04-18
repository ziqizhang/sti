package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 */
public class TableTriple implements Serializable {

    private static final long serialVersionUID = -813672581122221313L;
    private String subject_annotation;
    private String subject;
    private int[] subject_position; //row, column of table
    private String object_annotation;
    private String object;
    private int[] object_position;
    private String relation_annotation;

    public String getSubject_annotation() {
        return subject_annotation;
    }

    public void setSubject_annotation(String subject_annotation) {
        this.subject_annotation = subject_annotation;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int[] getSubject_position() {
        return subject_position;
    }

    public void setSubject_position(int[] subject_position) {
        this.subject_position = subject_position;
    }

    public String getObject_annotation() {
        return object_annotation;
    }

    public void setObject_annotation(String object_annotation) {
        this.object_annotation = object_annotation;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public int[] getObject_position() {
        return object_position;
    }

    public void setObject_position(int[] object_position) {
        this.object_position = object_position;
    }

    public String getRelation_annotation() {
        return relation_annotation;
    }

    public void setRelation_annotation(String relation_annotation) {
        this.relation_annotation = relation_annotation;
    }
}
