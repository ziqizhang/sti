package cz.cuni.mff.xrg.odalic.util;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Utility class for -- you guessed it -- working with maps.
 * 
 * @author Václav Brodec
 *
 */
public final class Maps {

  /**
   * We want to keep this class uninstantiable, so no visible constructor is available.
   */
  private Maps() {}

  /**
   * Merges the added map into the modified one. If the key of the added entry is not found in the map, the entry is simply added.
   * 
   * @param modified the map whose elements serve as the first argument of the merge function and
   *        then are replaced by its result
   * @param added collection whose elements serve as the second argument of the merge function
   * @param mergeFunction merge function
   * 
   * @param <K> type of keys
   * @param <V> type of values
   */
  public static <K, V> void mergeWith(Map<K, V> modified, Map<K, V> added,
      BiFunction<V, V, V> mergeFunction) throws IllegalArgumentException {
    
    for (Map.Entry<K, V> addedEntry : added.entrySet()) {
      final K key = addedEntry.getKey();
      
      final V addedValue = addedEntry.getValue();
      final V modifiedValue = modified.get(key);
      
      if (modifiedValue == null) {
        modified.put(key, addedValue);
      } else {
        modified.put(key, mergeFunction.apply(modifiedValue, addedValue));
      }
    }
  }


}
