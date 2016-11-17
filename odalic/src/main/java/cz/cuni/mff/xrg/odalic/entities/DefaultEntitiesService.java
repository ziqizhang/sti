/**
 * 
 */
package cz.cuni.mff.xrg.odalic.entities;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Default {@link EntitiesService} implementation.
 *
 */
public final class DefaultEntitiesService implements EntitiesService {

  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;

  public DefaultEntitiesService(KnowledgeBaseProxyFactory knowledgeBaseProxyFactory) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);

    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
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
      throws IllegalArgumentException, KBProxyException {
    KBProxy kbProxy = getKBProxy(base);

    List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findEntityByFulltext(query, limit);

    return searchResult.stream().map(entity -> new Entity(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.entities.EntitiesService#propose(cz.cuni.mff.xrg.odalic.entities.
   * ClassProposal)
   */
  @Override
  public Entity propose(KnowledgeBase base, ClassProposal proposal)
    throws KBProxyException {
    KBProxy kbProxy = getKBProxy(base);

    String superClassUri = null;
    Entity superClass = proposal.getSuperClass();
    if (superClass != null){
      superClassUri= superClass.getResource();
    }

    uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertClass(
            proposal.getSuffix(),
            proposal.getLabel(),
            proposal.getAlternativeLabels(),
            superClassUri);

    return new Entity(entity.getId(), entity.getLabel());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.entities.EntitiesService#propose(cz.cuni.mff.xrg.odalic.entities.
   * ResourceProposal)
   */
  @Override
  public Entity propose(KnowledgeBase base, ResourceProposal proposal)
          throws KBProxyException {
    KBProxy kbProxy = getKBProxy(base);

    Collection<String> classes = null;
    if (proposal.getClasses() != null) {
      classes = proposal.getClasses().stream().map(Entity::getResource).collect(Collectors.toList());
    }
    uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertConcept(
            proposal.getSuffix(),
            proposal.getLabel(),
            proposal.getAlternativeLabels(),
            classes);

    return new Entity(entity.getId(), entity.getLabel());
  }

  private KBProxy getKBProxy(KnowledgeBase base) throws KBProxyException {
    KBProxy kbProxy = knowledgeBaseProxyFactory.getKBProxies().get(base.getName());

    if (kbProxy == null) {
      throw new IllegalArgumentException(
              "Knowledge base named \"" + base.getName() + "\" was not found.");
    }

    return kbProxy;
  }
}
