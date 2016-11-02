package cz.cuni.mff.xrg.odalic.tasks.executions;

import uk.ac.shef.dcs.kbsearch.KBSearch;

import java.util.Map;

/**
 * Created by Jan
 */
public interface KnowledgeBaseSearchFactory {
  /**
   * Lazily initializes the KB searches.
   *
   * @return the KB search implementations
   */
  Map<String, KBSearch> getKBSearches();
}
