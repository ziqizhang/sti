package uk.ac.shef.dcs.sti.core.extension.annotations;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Knowledge base identifier. May turn into full-fledged domain entity class in future version.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class KnowledgeBase {

  private final String name;

  public KnowledgeBase(String name) {
    Preconditions.checkNotNull(name);

    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Computes hash code (for now) based on the name.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /**
   * Compares for equality (only other knowledge base instance with the same name passes, for now).
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
    KnowledgeBase other = (KnowledgeBase) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
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
    return "KnowledgeBase [name=" + name + "]";
  }
}
