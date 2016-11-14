/**
 * 
 */
package cz.cuni.mff.xrg.odalic.feedbacks;

import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;
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
    Preconditions.checkNotNull(feedback);
    Preconditions.checkNotNull(base);

    return new Constraints(convertSubjectColumn(feedback, base),
        convertIgnores(feedback.getColumnIgnores()),
        convertColumnAmbiguities(feedback.getColumnAmbiguities()),
        convertClassifications(feedback.getClassifications(), base),
        convertRelations(feedback.getColumnRelations(), base),
        convertDisambiguations(feedback.getDisambiguations(), base),
        convertAmbiguitites(feedback.getAmbiguities()));
  }

  private uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition convertSubjectColumn(
      Feedback feedback, KnowledgeBase base) {
    final ColumnPosition subjectColumnPosition = feedback.getSubjectColumnPositions().get(base);

    if (subjectColumnPosition != null) {
      return convert(subjectColumnPosition);
    } else {
      return null;
    }
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
    return set.stream().map(e -> convert(e, base)).filter(e -> e != null).collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation> convertDisambiguations(
      Set<? extends Disambiguation> set, KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).filter(e -> e != null).collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation> convertRelations(
      Set<? extends ColumnRelation> set, KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).filter(e -> e != null).collect(Collectors.toSet());
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
    final NavigableSet<EntityCandidate> candidates = e.getCandidates().get(base);
    if (candidates == null) {
      return null;
    }
    
    final Set<EntityCandidate> chosen = e.getChosen().get(base);
    if (chosen == null) {
      return null;
    }
    
    return new uk.ac.shef.dcs.sti.core.extension.annotations.HeaderAnnotation(
        convertCandidates(candidates), convertCandidates(chosen));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation convert(
      CellAnnotation e, KnowledgeBase base) {
    final NavigableSet<EntityCandidate> candidates = e.getCandidates().get(base);
    if (candidates == null) {
      return null;
    }
    
    final Set<EntityCandidate> chosen = e.getChosen().get(base);
    if (chosen == null) {
      return null;
    }
    
    return new uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation(
        convertCandidates(candidates), convertCandidates(chosen));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.ColumnRelationAnnotation convert(
      ColumnRelationAnnotation e, KnowledgeBase base) {
    final NavigableSet<EntityCandidate> candidates = e.getCandidates().get(base);
    if (candidates == null) {
      return null;
    }
    
    final Set<EntityCandidate> chosen = e.getChosen().get(base);
    if (chosen == null) {
      return null;
    }
    
    return new uk.ac.shef.dcs.sti.core.extension.annotations.ColumnRelationAnnotation(
        convertCandidates(candidates), convertCandidates(chosen));
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.Classification convert(
      Classification e, KnowledgeBase base) {
    final uk.ac.shef.dcs.sti.core.extension.annotations.HeaderAnnotation convertedAnnotation = convert(e.getAnnotation(), base);
    if (convertedAnnotation == null) {
      return null;
    }
    
    return new uk.ac.shef.dcs.sti.core.extension.constraints.Classification(
        convert(e.getPosition()), convertedAnnotation);
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation convert(
      Disambiguation e, KnowledgeBase base) {
    final uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation convertedAnnotation = convert(e.getAnnotation(), base);
    if (convertedAnnotation == null) {
      return null;
    }
    
    return new uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation(
        convert(e.getPosition()), convertedAnnotation);
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation convert(
      ColumnRelation e, KnowledgeBase base) {
    final uk.ac.shef.dcs.sti.core.extension.annotations.ColumnRelationAnnotation convertedAnnotation = convert(e.getAnnotation(), base);
    if (convertedAnnotation == null) {
      return null;
    }
    
    return new uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation(
        convert(e.getPosition()), convertedAnnotation);
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
        convert(candidate.getEntity()), convert(candidate.getScore()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.Entity convert(final Entity entity) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.Entity(entity.getResource(),
        entity.getLabel());
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.Score convert(final Score score) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.Score(score.getValue());
  }
}
