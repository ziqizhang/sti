package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

@XmlRootElement(name = "likelihood")
public final class Likelihood implements Comparable<Likelihood>, Serializable {

  private static final long serialVersionUID = -901650058091668104L;

  @XmlElement
  private final double value;

  @SuppressWarnings("unused")
  private Likelihood() {
    value = Double.NEGATIVE_INFINITY;
  }

  /**
   * @param value
   */
  public Likelihood(double value) {
    Preconditions.checkArgument(value >= 0);

    // TODO: Check what this means. Core algorithm returns unbound score.
    // Preconditions.checkArgument(value <= 1);

    this.value = value;
  }

  /**
   * @return the value
   */
  public double getValue() {
    return value;
  }

  /*
   * (non-Javadoc)
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

  /*
   * (non-Javadoc)
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Likelihood [value=" + value + "]";
  }

  @Override
  public int compareTo(Likelihood o) {
    return Double.compare(value, o.value);
  }
}
