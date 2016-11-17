package cz.cuni.mff.xrg.odalic.tasks.executions;

import uk.ac.shef.dcs.kbproxy.KBProxy;

import java.util.Map;

/**
 * Created by Jan
 */
public interface KnowledgeBaseProxyFactory {
  /**
   * Lazily initializes the KB searches.
   *
   * @return the KB search implementations
   */
  Map<String, KBProxy> getKBProxies();
}
