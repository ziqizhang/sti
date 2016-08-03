package cz.cuni.mff.xrg.odalic.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;

/**
 * Utility class for -- you guessed it -- working with collections.
 * 
 * @author Václav Brodec
 *
 */
public final class Lists {

  /**
   * We want to keep this class uninstantiable, so no visible constructor is available.
   */
  private Lists() {}

  /**
   * Executes functional zip over a list and a collection, but modifies the list in the process.
   * 
   * @param modified the list whose elements serve as the first argument of the zip function and
   *        then are replaced by its result
   * @param added collection whose elements serve as the second argument of the zip function
   * @param zipFunction zip function
   * @throws IllegalArgumentException If the modified and added have different number of elements
   * @throws UnsupportedOperationException  if the set operation is not supported by the list iterator
   * 
   * @param <T> type of elements in modified
   * @param <U> type of elements in added
   */
  public static <T, U> void zipWith(List<T> modified, Collection<U> added,
      BiFunction<T, U, T> zipFunction) throws IllegalArgumentException {
    if (modified.size() != added.size()) {
      throw new IllegalArgumentException();
    }

    final ListIterator<T> listIterator = modified.listIterator();
    final Iterator<U> iterator = added.iterator();
    while (listIterator.hasNext() && iterator.hasNext()) {
      listIterator.set(zipFunction.apply(listIterator.next(), iterator.next()));
    }
  }


}
