/**
 * 
 */
package cz.cuni.mff.xrg.odalic.entities;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseSearchFactory;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Default {@link EntitiesService} implementation.
 *
 */
public final class DefaultEntitiesService implements EntitiesService {

  private final KnowledgeBaseSearchFactory knowledgeBaseSearchFactory;

  public DefaultEntitiesService(KnowledgeBaseSearchFactory knowledgeBaseSearchFactory) {
    Preconditions.checkNotNull(knowledgeBaseSearchFactory);

    this.knowledgeBaseSearchFactory = knowledgeBaseSearchFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.entities.EntitiesService#search(cz.cuni.mff.xrg.odalic.tasks.annotations
   * .KnowledgeBase, java.lang.String, int)
   */
  @Override
  public NavigableSet<Entity> search(KnowledgeBase base, String query, int limit)
      throws IllegalArgumentException, KBSearchException {
    KBSearch kbSearch = knowledgeBaseSearchFactory.getKBSearches().get(base.getName());

    if (kbSearch == null) {
      throw new IllegalArgumentException(
          "Knowledge base named \"" + base.getName() + "\" was not found.");
    }

    List<uk.ac.shef.dcs.kbsearch.model.Entity> searchResult =
        kbSearch.findEntityByFulltext(query, limit);
    NavigableSet<Entity> result =
        searchResult.stream().map(entity -> new Entity(entity.getId(), entity.getLabel()))
            .collect(Collectors.toCollection(TreeSet::new));

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.entities.EntitiesService#propose(cz.cuni.mff.xrg.odalic.entities.
   * ClassProposal)
   */
  @Override
  public Entity propose(ClassProposal proposal) {
    // TODO: Implement class proposal. Throw exceptions whenever something is wrong, otherwise
    // return the newly created class (in general entity form).
    // 
    // Please note that proposing already existing class should also throw exception.
    return new Entity("http://dummy.com/Class", "Dummy class label");
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.entities.EntitiesService#propose(cz.cuni.mff.xrg.odalic.entities.
   * ResourceProposal)
   */
  @Override
  public Entity propose(ResourceProposal proposal) {
    // TODO: Implement class proposal. Throw exceptions whenever something is wrong, otherwise
    // return the newly created class (in general entity form).
    // 
    // Please note that proposing already existing class should also throw exception.
    return new Entity("http://dummy.com/Entity", "Dummy entity label");
  }
}
