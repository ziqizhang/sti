/**
 * 
 */
package cz.cuni.mff.xrg.odalic.util;

/**
 * Reference wrapper. It may find use in cases when pass by reference semantics are needed.
 * 
 * @author Václav Brodec
 */
public final class ReferenceWrapper<T> {

  private T reference;

  public static <U> ReferenceWrapper<U> empty() {
    return new ReferenceWrapper<U>();
  }
  
  public static <U> ReferenceWrapper<U> wrap(U reference) {
    return new ReferenceWrapper<U>(reference);
  }
  
  /**
   * Creates an empty wrapper.
   */
  public ReferenceWrapper() {}

  /**
   * Wraps the reference.
   * 
   * @param reference wrapped reference
   */
  public ReferenceWrapper(final T reference) {
    this.reference = reference;
  }

  /**
   * @return the reference
   */
  public T getReference() {
    return reference;
  }

  /**
   * @param reference the reference to set
   */
  public void setReference(T reference) {
    this.reference = reference;
  }
  
  /**
   * Indicates non-{@code null} reference presence.
   * 
   * @return true when the contained reference is {@code null}, false otherwise
   */
  public boolean isEmpty() {
    return reference == null;
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
    result = prime * result + System.identityHashCode(reference);
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
    @SuppressWarnings("rawtypes")
    ReferenceWrapper other = (ReferenceWrapper) obj;
    return reference == other.reference;
  }
}
