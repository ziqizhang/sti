package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.LikelihoodAdapter;

/**
 * Probability based score value for annotation.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(LikelihoodAdapter.class)
public final class Likelihood implements Comparable<Likelihood>, Serializable {

  private static final long serialVersionUID = -901650058091668104L;

  private final double value;

  public Likelihood(double value) {
    Preconditions.checkArgument(value >= 0);

    this.value = value;
  }

  /**
   * @return the value
   */
  public double getValue() {
    return value;
  }

  /**
   * Computes hash code based on the value.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(value);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /**
   * Compares for equality (only other Likelihood with the same values passes).
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Likelihood other = (Likelihood) obj;
    if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Likelihood o) {
    return Double.compare(value, o.value);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Likelihood [value=" + value + "]";
  }

}
