package cz.cuni.mff.xrg.odalic.tasks.results;

import uk.ac.shef.dcs.sti.core.model.TAnnotation;

public interface AnnotationToResultAdapter {
  Result toResult(TAnnotation annotation);
}
