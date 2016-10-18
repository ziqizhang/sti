package cz.cuni.mff.xrg.odalic.entities;

import java.util.NavigableSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Provides basic capabilities of entities management.
 * 
 * @author Václav Brodec
 *
 */
public interface EntitiesService {
  /**
   * Searches for entities conforming to the query.
   * 
   * @param base used knowledge base
   * @param query search query
   * @param limit maximum results count
   * @return found entities
   */
  NavigableSet<Entity> search(KnowledgeBase base, String query, int limit);
}
