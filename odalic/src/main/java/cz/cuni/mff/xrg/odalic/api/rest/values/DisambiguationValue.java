package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;

/**
 * Domain class {@link Disambiguation} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "disambiguation")
public final class DisambiguationValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private CellPosition position;
  
  private CellAnnotation annotation;
  
  public DisambiguationValue() {}
  
  public DisambiguationValue(Disambiguation adaptee) {
    this.position = adaptee.getPosition();
    this.annotation = adaptee.getAnnotation();
  }

  /**
   * @return the position
   */
  @XmlElement
  @Nullable
  public CellPosition getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(CellPosition position) {
    Preconditions.checkNotNull(position);
    
    this.position = position;
  }

  /**
   * @return the annotation
   */
  @XmlElement
  @Nullable
  public CellAnnotation getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(CellAnnotation annotation) {
    Preconditions.checkNotNull(annotation);
    
    this.annotation = annotation;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DisambiguationValue [position=" + position + ", annotation=" + annotation + "]";
  }
}
