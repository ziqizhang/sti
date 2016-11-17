package uk.ac.shef.dcs.sti.core.model;

import uk.ac.shef.dcs.kbproxy.model.Attribute;

import java.io.Serializable;
import java.util.List;

/**
 * annotates relation between two cells on the same row. Annotations on multi rows must be aggregated to derive an annotation for
 * two columns
 */
public class TCellCellRelationAnotation implements Serializable, Comparable<TCellCellRelationAnotation> {

    private static final long serialVersionUID = -1208912234750474692L;
    private RelationColumns relationColumns;
    private int row;

    private String relationURI;
    private String relationLabel;

    private List<Attribute> winningAttributes; //multiple winner possible
    private double winningAttributeMatchScore;

    //matched_value[]: (0)=property name (1)=the attribute value matched with the objecCol field on this row; (2) id/uri, if any (used for later knowledge base retrieval)
    public TCellCellRelationAnotation(RelationColumns key,
                                      int row,
                                      String relation_annotation,
                                      String relation_label,
                                      List<Attribute> winningAttributes, double winningAttributeMatchScore){
        this.relationColumns =key;
        this.row=row;
        this.relationURI = relation_annotation;
        this.relationLabel =relation_label;
        this.winningAttributeMatchScore = winningAttributeMatchScore;
        this.winningAttributes = winningAttributes;
    }

    public double getWinningAttributeMatchScore(){
        return winningAttributeMatchScore;
    }
    public void setWinningAttributeMatchScore(double winningAttributeMatchScore){
        this.winningAttributeMatchScore = winningAttributeMatchScore;
    }



    public int getRow(){
        return row;
    }

    public String getRelationURI(){
        return relationURI;
    }

    public boolean equals(Object o){
        if(o instanceof TCellCellRelationAnotation){
            TCellCellRelationAnotation that = (TCellCellRelationAnotation) o;
            return that.getRelationColumns().equals(getRelationColumns())
                    &&that.getRow()==getRow()
                    &&that.getRelationURI().equals(getRelationURI());
        }
        return false;
    }

    public int hashCode(){
        return getRelationColumns().hashCode()+19*getRow()+29* getRelationURI().hashCode();
    }

    @Override
    public int compareTo(TCellCellRelationAnotation o) {
        int compared = new Integer(o.getRow()).compareTo(getRow());

        if(compared==0)
            return new Double(o.getWinningAttributeMatchScore()).compareTo(getWinningAttributeMatchScore());

        return compared;
    }

    public RelationColumns getRelationColumns() {
        return relationColumns;
    }

    public void setRelationColumns(RelationColumns relationColumns) {
        this.relationColumns = relationColumns;
    }

    public List<Attribute> getWinningAttributes() {
        return winningAttributes;
    }

    public void setWinningAttributes(List<Attribute> winningAttributes) {
        this.winningAttributes = winningAttributes;
    }

    public void addWinningAttributes(List<Attribute> toAdd){
        for(Attribute vta: toAdd){
            if(!winningAttributes.contains(vta))
                winningAttributes.add(vta);
        }
    }

    public String toString(){
        return relationURI;
    }

    public String getRelationLabel() {
        return relationLabel;
    }

    public void setRelationLabel(String relationLabel) {
        this.relationLabel = relationLabel;
    }
}
