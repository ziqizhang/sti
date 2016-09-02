package cz.cuni.mff.xrg.odalic.tasks.results;

import java.util.Map;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;

/**
 * Converts the complete annotation result provided by the Semantic Table Interpreter to the
 * representation used by Odalic server to facilitate RDF output and user feedback.
 * 
 * @author Václav Brodec
 *
 */
public interface AnnotationToResultAdapter {
  /**
   * Converts the annotation formats. Uses the primary base to determine the subject column.
   * 
   * @param basesToTableAnnotations map to table annotations from the knowledge bases that were used
   *        to make them
   * @param primaryBase primary knowledge base
   * @return Odalic result format
   */
  Result toResult(Map<KnowledgeBase, TAnnotation> basesToTableAnnotations,
      KnowledgeBase primaryBase);
}
