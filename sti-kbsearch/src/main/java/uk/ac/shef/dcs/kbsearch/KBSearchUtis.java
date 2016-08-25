package uk.ac.shef.dcs.kbsearch;

import java.util.Set;

/**
 * Created by JanVa_000 on 25.08.2016.
 */
public class KBSearchUtis {
  /**
   * This is a little hack for inconsistent http and https in predicate links.
   * @param set The set to search.
   * @param value The value to find.
   * @return True if the set contains the specified value.
   */
  public static boolean contains(Set<String> set, String value) {
    value = value.toLowerCase();
    return set.contains(value) || set.contains(value.replace("http://", "https://")) || set.contains(value.replace("https://", "http://"));
  }
}
