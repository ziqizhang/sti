package cz.cuni.mff.xrg.odalic.entities;

import uk.ac.shef.dcs.kbsearch.KBSearchException;

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
  NavigableSet<Entity> search(KnowledgeBase base, String query, int limit) throws KBSearchException;

  /**
   * Propose a new class to the primary base.
   * 
   * @param proposal class proposal
   * @return created class
   * @throws IllegalArgumentException when the class is already defined or some part of the proposal is invalid
   */
  Entity propose(ClassProposal proposal);

  /**
   * Propose a new entity to the primary base.
   * 
   * @param proposal entity proposal
   * @return created entity
   * @throws IllegalArgumentException when the entity is already defined or some part of the proposal is invalid
   */
  Entity propose(ResourceProposal proposal);
}
