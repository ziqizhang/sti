package cz.cuni.mff.xrg.odalic.bases;

import java.util.NavigableSet;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Provides basic capabilities of bases management.
 * 
 * @author Václav Brodec
 *
 */
public interface BasesService {

  /**
   * @return bases naturally ordered
   */
  NavigableSet<KnowledgeBase> getBases();

  /**
   * @return insert supporting bases, naturally ordered
   */
  NavigableSet<KnowledgeBase> getInsertSupportingBases();
}
