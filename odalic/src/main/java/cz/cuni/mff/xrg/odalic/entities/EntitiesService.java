package cz.cuni.mff.xrg.odalic.entities;

import uk.ac.shef.dcs.kbproxy.KBProxyException;

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
  NavigableSet<Entity> search(KnowledgeBase base, String query, int limit) throws KBProxyException;

  /**
   * Propose a new class to the primary base.
   *
   * @param base used knowledge base
   * @param proposal class proposal
   * @return created class
   * @throws IllegalArgumentException when the class is already defined or some part of the proposal is invalid
   */
  Entity propose(KnowledgeBase base, ClassProposal proposal) throws KBProxyException;

  /**
   * Propose a new entity to the primary base.
   *
   * @param base used knowledge base
   * @param proposal entity proposal
   * @return created entity
   * @throws IllegalArgumentException when the entity is already defined or some part of the proposal is invalid
   */
  Entity propose(KnowledgeBase base, ResourceProposal proposal) throws KBProxyException;
}
