/**
 * 
 */
package cz.cuni.mff.xrg.odalic.feedbacks;

import java.util.Set;
import java.util.stream.Collectors;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Likelihood;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;

/**
 * Default {@link FeedbackToConstraintsAdapter} implementation.
 * 
 * @author Václav Brodec
 *
 */
public class DefaultFeedbackToConstraintsAdapter implements FeedbackToConstraintsAdapter {

  @Override
  public Constraints toConstraints(Feedback feedback, KnowledgeBase base) {
    uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition subjectColumnPosition = null;
    if (feedback.getSubjectColumnPosition() != null) {
      subjectColumnPosition = convert(feedback.getSubjectColumnPosition());
    }

    return new Constraints(subjectColumnPosition,
        convertIgnores(feedback.getColumnIgnores()), convertColumnAmbiguities(feedback.getColumnAmbiguities()),
        convertClassifications(feedback.getClassifications(), base), convertRelations(feedback.getColumnRelations(), base),
        convertDisambiguations(feedback.getDisambiguations(), base), convertAmbiguitites(feedback.getAmbiguities())
        );
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.Ambiguity> convertAmbiguitites(
      Set<? extends Ambiguity> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.ColumnIgnore> convertIgnores(
      Set<? extends ColumnIgnore> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.ColumnAmbiguity> convertColumnAmbiguities(
      Set<? extends ColumnAmbiguity> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate> convertCandidates(
      Set<? extends EntityCandidate> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.Classification> convertClassifications(
      Set<? extends Classification> set, KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation> convertDisambiguations(
      Set<? extends Disambiguation> set, KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation> convertRelations(
      Set<? extends ColumnRelation> set, KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).collect(Collectors.toSet());
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.Ambiguity convert(Ambiguity e) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.Ambiguity(convert(e.getPosition()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.ColumnIgnore convert(
      ColumnIgnore e) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.ColumnIgnore(convert(e.getPosition()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.ColumnAmbiguity convert(
      ColumnAmbiguity e) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.ColumnAmbiguity(
        convert(e.getPosition()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.HeaderAnnotation convert(
      HeaderAnnotation e, KnowledgeBase base) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.HeaderAnnotation(
        convertCandidates(e.getCandidates().get(base)), convertCandidates(e.getChosen().get(base)));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation convert(
      CellAnnotation e, KnowledgeBase base) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation(
        convertCandidates(e.getCandidates().get(base)), convertCandidates(e.getChosen().get(base)));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.ColumnRelationAnnotation convert(
      ColumnRelationAnnotation e, KnowledgeBase base) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.ColumnRelationAnnotation(
        convertCandidates(e.getCandidates().get(base)), convertCandidates(e.getChosen().get(base)));
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.Classification convert(
      Classification e, KnowledgeBase base) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.Classification(
        convert(e.getPosition()), convert(e.getAnnotation(), base));
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation convert(
      Disambiguation e, KnowledgeBase base) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation(
        convert(e.getPosition()), convert(e.getAnnotation(), base));
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation convert(
      ColumnRelation e, KnowledgeBase base) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation(
        convert(e.getPosition()), convert(e.getAnnotation(), base));
  }

  private static uk.ac.shef.dcs.sti.core.extension.positions.CellPosition convert(CellPosition e) {
    return new uk.ac.shef.dcs.sti.core.extension.positions.CellPosition(e.getRowIndex(),
        e.getColumnIndex());
  }

  private static uk.ac.shef.dcs.sti.core.extension.positions.ColumnRelationPosition convert(
      ColumnRelationPosition e) {
    return new uk.ac.shef.dcs.sti.core.extension.positions.ColumnRelationPosition(
        convert(e.getFirst()), convert(e.getSecond()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition convert(
      final ColumnPosition columnPosition) {
    return new uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition(
        columnPosition.getIndex());
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate convert(
      final EntityCandidate candidate) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate(
        convert(candidate.getEntity()), convert(candidate.getLikelihood()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.Entity convert(final Entity entity) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.Entity(entity.getLabel(),
        entity.getResource());
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.Score convert(
      final Likelihood score) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.Score(score.getValue());
  }
}
