package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.CellRelation;
import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;

/**
 * Domain class {@link CellRelation} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "cellRelation")
public final class CellRelationValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private CellRelationPosition position;
  
  private CellRelationAnnotation annotation;
  
  public CellRelationValue() {}
  
  public CellRelationValue(CellRelation adaptee) {
    this.position = adaptee.getPosition();
    this.annotation = adaptee.getAnnotation();
  }

  /**
   * @return the position
   */
  @XmlElement
  @Nullable
  public CellRelationPosition getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(CellRelationPosition position) {
    Preconditions.checkNotNull(position);
    
    this.position = position;
  }

  /**
   * @return the annotation
   */
  @XmlElement
  @Nullable
  public CellRelationAnnotation getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(CellRelationAnnotation annotation) {
    Preconditions.checkNotNull(annotation);
    
    this.annotation = annotation;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellRelationValue [position=" + position + ", annotation=" + annotation + "]";
  }
}
