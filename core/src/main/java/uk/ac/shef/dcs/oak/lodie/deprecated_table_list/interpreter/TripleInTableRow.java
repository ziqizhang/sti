package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 12/03/13
 * Time: 11:26
 */
public class TripleInTableRow implements Comparable<TripleInTableRow> {
    private String subject;
    private String predicate;
    private String object;
    private int subjectColumn;
    private int objectColumn;
    private int row;
    private boolean predicateReverse;

    public TripleInTableRow(String subject, String predicate, String object,
                            int subjectColumn, int objectColumn, int row) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.subjectColumn = subjectColumn;
        this.objectColumn = objectColumn;
        this.row = row;
    }


    public int getSubjectColumn() {
        return subjectColumn;
    }

    public void setSubjectColumn(int subjectColumn) {
        this.subjectColumn = subjectColumn;
    }

    public int getObjectColumn() {
        return objectColumn;
    }

    public void setObjectColumn(int objectColumn) {
        this.objectColumn = objectColumn;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
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

    @Override
    public int compareTo(TripleInTableRow o) {
        return Integer.valueOf(o.getRow()).compareTo(this.getRow());
    }

    public boolean isPredicateReverse() {
        return predicateReverse;
    }

    public void setPredicateReverse(boolean predicateReverse) {
        this.predicateReverse = predicateReverse;
    }
}
