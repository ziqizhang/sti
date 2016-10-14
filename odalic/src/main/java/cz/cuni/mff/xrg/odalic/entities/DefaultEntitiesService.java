/**
 * 
 */
package cz.cuni.mff.xrg.odalic.entities;

import java.util.NavigableSet;

import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Default {@link EntitiesService} implementation.
 *
 */
public final class DefaultEntitiesService implements EntitiesService {

  public DefaultEntitiesService() {}

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.entities.EntitiesService#search(cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase, java.lang.String, int)
   */
  @Override
  public NavigableSet<Entity> search(KnowledgeBase base, String query, int limit) {
    // TODO: Implement search. You can hard-wire the dependencies for now.
    
    return ImmutableSortedSet.of();
  }

}
