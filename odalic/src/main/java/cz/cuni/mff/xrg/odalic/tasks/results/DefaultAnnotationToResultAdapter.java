package cz.cuni.mff.xrg.odalic.tasks.results;

import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Likelihood;
import cz.cuni.mff.xrg.odalic.util.Arrays;
import cz.cuni.mff.xrg.odalic.util.Lists;
import cz.cuni.mff.xrg.odalic.util.Maps;
import cz.cuni.mff.xrg.odalic.util.ReferenceWrapper;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellCellRelationAnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * This implementation of {@link AnnotationToResultAdapter} simply merges the annotations done with
 * different knowledge bases.
 * 
 * @author Jan Váňa
 * @author Václav Brodec
 *
 */
@Immutable
public class DefaultAnnotationToResultAdapter implements AnnotationToResultAdapter {

  /**
   * This implementation demands that the subject columns recognized in the annotations are the
   * same.
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.results.AnnotationToResultAdapter#toResult(Map,
   *      KnowledgeBase)
   */
  @Override
  public Result toResult(Map<KnowledgeBase, TAnnotation> basesToTableAnnotations,
      KnowledgeBase primaryBase) {
    Preconditions.checkArgument(!basesToTableAnnotations.isEmpty());

    final Iterator<Map.Entry<KnowledgeBase, TAnnotation>> entrySetIterator =
        initializeEntrySetIterator(basesToTableAnnotations);

    // Process the first entry to initialize working structures for the merges.
    final Map.Entry<KnowledgeBase, TAnnotation> firstEntry = entrySetIterator.next();
    final KnowledgeBase firstKnowledgeBase = firstEntry.getKey();
    final TAnnotation firstTableAnnotation = firstEntry.getValue();

    final ReferenceWrapper<ColumnPosition> mergedSubjectColumn = ReferenceWrapper
        .wrap(convertSubjectColumn(firstTableAnnotation, firstKnowledgeBase, primaryBase));
    final List<HeaderAnnotation> mergedHeaderAnnotations =
        convertColumnAnnotations(firstKnowledgeBase, firstTableAnnotation);
    final CellAnnotation[][] mergedCellAnnotations =
        convertCellAnnotations(firstKnowledgeBase, firstTableAnnotation);
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations =
        convertColumnRelations(firstKnowledgeBase, firstTableAnnotation);
    final Map<CellRelationPosition, CellRelationAnnotation> mergedCellCellRelations =
        convertCellRelations(firstKnowledgeBase, firstTableAnnotation);

    // Process the rest.
    processTheRest(entrySetIterator, primaryBase, mergedSubjectColumn, mergedHeaderAnnotations,
        mergedCellAnnotations, mergedColumnRelations, mergedCellCellRelations);

    Preconditions.checkArgument(!mergedSubjectColumn.isEmpty());
    
    return new Result(mergedSubjectColumn.getReference(), mergedHeaderAnnotations, mergedCellAnnotations,
        mergedColumnRelations, mergedCellCellRelations);
  }

  private static Iterator<Map.Entry<KnowledgeBase, TAnnotation>> initializeEntrySetIterator(
      Map<KnowledgeBase, TAnnotation> basesToTableAnnotations) {
    final Set<Map.Entry<KnowledgeBase, TAnnotation>> entrySet = basesToTableAnnotations.entrySet();
    final Iterator<Map.Entry<KnowledgeBase, TAnnotation>> entrySetIterator = entrySet.iterator();
    return entrySetIterator;
  }

  private static void processTheRest(
      final Iterator<Map.Entry<KnowledgeBase, TAnnotation>> entrySetIterator,
      final KnowledgeBase primaryBase, final ReferenceWrapper<ColumnPosition> mergedSubjectColumn,
      final List<HeaderAnnotation> mergedHeaderAnnotations,
      final CellAnnotation[][] mergedCellAnnotations,
      final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations,
      final Map<CellRelationPosition, CellRelationAnnotation> mergedCellCellRelations) {
    while (entrySetIterator.hasNext()) {
      final Map.Entry<KnowledgeBase, TAnnotation> entry = entrySetIterator.next();

      final KnowledgeBase knowledgeBase = entry.getKey();
      final TAnnotation tableAnnotation = entry.getValue();

      mergeSubjectColumn(mergedSubjectColumn, knowledgeBase, tableAnnotation, primaryBase);

      mergeHeaders(mergedHeaderAnnotations, knowledgeBase, tableAnnotation);
      mergeCells(mergedCellAnnotations, knowledgeBase, tableAnnotation);
      mergeColumnRelations(mergedColumnRelations, knowledgeBase, tableAnnotation);
      mergeCellRelations(mergedCellCellRelations, knowledgeBase, tableAnnotation);
    }
  }

  private static ColumnPosition convertSubjectColumn(final TAnnotation tableAnnotation,
      final KnowledgeBase knowledgeBase, final KnowledgeBase primaryBase) {
    if (!knowledgeBase.equals(primaryBase)) {
      return null; // Only primary base determines the subject column!
    }

    return new ColumnPosition(tableAnnotation.getSubjectColumn());
  }

  private static void mergeSubjectColumn(final ReferenceWrapper<ColumnPosition> mergedSubjectColumn,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation, final KnowledgeBase primaryBase) {
    final ColumnPosition subjectColumn = convertSubjectColumn(tableAnnotation, knowledgeBase, primaryBase);
    
    if (subjectColumn != null) {
      mergedSubjectColumn.setReference(subjectColumn);
    }
  }

  private static void mergeCellRelations(
      final Map<CellRelationPosition, CellRelationAnnotation> mergedCellCellRelations,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation) {
    final Map<CellRelationPosition, CellRelationAnnotation> cellCellRelations =
        convertCellRelations(knowledgeBase, tableAnnotation);
    Maps.mergeWith(mergedCellCellRelations, cellCellRelations,
        (first, second) -> first.merge(second));
  }

  private static void mergeColumnRelations(
      final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation) {
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelations =
        convertColumnRelations(knowledgeBase, tableAnnotation);
    Maps.mergeWith(mergedColumnRelations, columnRelations, (first, second) -> first.merge(second));
  }

  private static void mergeCells(final CellAnnotation[][] mergedCellAnnotations,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation) {
    final CellAnnotation[][] cellAnnotations =
        convertCellAnnotations(knowledgeBase, tableAnnotation);
    Arrays.zipMatrixWith(mergedCellAnnotations, cellAnnotations,
        (first, second) -> first.merge(second));
  }

  private static void mergeHeaders(final List<HeaderAnnotation> mergedHeaderAnnotations,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation) {
    final List<HeaderAnnotation> headerAnnotations =
        convertColumnAnnotations(knowledgeBase, tableAnnotation);
    Lists.zipWith(mergedHeaderAnnotations, headerAnnotations,
        (first, second) -> first.merge(second));
  }

  private static Map<CellRelationPosition, CellRelationAnnotation> convertCellRelations(
      KnowledgeBase knowledgeBase, TAnnotation original) {
    Map<CellRelationPosition, CellRelationAnnotation> cellCellRelations = new HashMap<>();
    for (Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> columnAnnotations : original
        .getCellcellRelations().entrySet()) {
      for (Map.Entry<Integer, List<TCellCellRelationAnotation>> annotations : columnAnnotations
          .getValue().entrySet()) {
        HashMap<KnowledgeBase, Set<EntityCandidate>> candidates = new HashMap<>();
        HashMap<KnowledgeBase, Set<EntityCandidate>> chosen = new HashMap<>();

        Set<EntityCandidate> candidatesSet = new HashSet<>();
        Set<EntityCandidate> chosenSet = new HashSet<>();

        candidates.put(knowledgeBase, candidatesSet);
        chosen.put(knowledgeBase, chosenSet);

        EntityCandidate bestCandidate = null;
        for (TCellCellRelationAnotation annotation : annotations.getValue()) {
          Entity entity = new Entity(annotation.getRelationURI(), annotation.getRelationLabel());
          Likelihood likelihood = new Likelihood(annotation.getWinningAttributeMatchScore());

          EntityCandidate candidate = new EntityCandidate(entity, likelihood);

          candidatesSet.add(candidate);

          if (bestCandidate == null
              || bestCandidate.getLikelihood().getValue() < candidate.getLikelihood().getValue()) {
            bestCandidate = candidate;
          }
        }

        if (bestCandidate != null) {
          chosenSet.add(bestCandidate);
        }

        RelationColumns relationColumns = columnAnnotations.getKey();
        ColumnRelationPosition columnPosition =
            new ColumnRelationPosition(new ColumnPosition(relationColumns.getSubjectCol()),
                new ColumnPosition(relationColumns.getObjectCol()));
        RowPosition rowPosition = new RowPosition(annotations.getKey());
        CellRelationPosition position = new CellRelationPosition(columnPosition, rowPosition);
        CellRelationAnnotation relationAnnotation = new CellRelationAnnotation(candidates, chosen);
        cellCellRelations.put(position, relationAnnotation);
      }
    }
    return cellCellRelations;
  }

  private static Map<ColumnRelationPosition, ColumnRelationAnnotation> convertColumnRelations(
      KnowledgeBase knowledgeBase, TAnnotation original) {
    Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelations = new HashMap<>();
    for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> annotations : original
        .getColumncolumnRelations().entrySet()) {
      HashMap<KnowledgeBase, Set<EntityCandidate>> candidates = new HashMap<>();
      HashMap<KnowledgeBase, Set<EntityCandidate>> chosen = new HashMap<>();

      Set<EntityCandidate> candidatesSet = new HashSet<>();
      Set<EntityCandidate> chosenSet = new HashSet<>();

      candidates.put(knowledgeBase, candidatesSet);
      chosen.put(knowledgeBase, chosenSet);

      EntityCandidate bestCandidate = null;
      for (TColumnColumnRelationAnnotation annotation : annotations.getValue()) {
        Entity entity = new Entity(annotation.getRelationURI(), annotation.getRelationLabel());
        Likelihood likelihood = new Likelihood(annotation.getFinalScore());

        EntityCandidate candidate = new EntityCandidate(entity, likelihood);

        candidatesSet.add(candidate);

        if (bestCandidate == null
            || bestCandidate.getLikelihood().getValue() < candidate.getLikelihood().getValue()) {
          bestCandidate = candidate;
        }
      }

      if (bestCandidate != null) {
        chosenSet.add(bestCandidate);
      }

      RelationColumns relationColumns = annotations.getKey();
      ColumnRelationPosition position =
          new ColumnRelationPosition(new ColumnPosition(relationColumns.getSubjectCol()),
              new ColumnPosition(relationColumns.getObjectCol()));
      ColumnRelationAnnotation relationAnnotation =
          new ColumnRelationAnnotation(candidates, chosen);
      columnRelations.put(position, relationAnnotation);
    }
    return columnRelations;
  }

  private static CellAnnotation[][] convertCellAnnotations(KnowledgeBase knowledgeBase,
      TAnnotation original) {
    int columnCount = original.getCols();
    int rowCount = original.getRows();
    CellAnnotation[][] cellAnnotations = new CellAnnotation[rowCount][columnCount];

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
        TCellAnnotation[] annotations = original.getContentCellAnnotations(rowIndex, columnIndex);

        HashMap<KnowledgeBase, Set<EntityCandidate>> candidates = new HashMap<>();
        HashMap<KnowledgeBase, Set<EntityCandidate>> chosen = new HashMap<>();

        if (annotations != null) {
          Set<EntityCandidate> candidatesSet = new HashSet<>();
          Set<EntityCandidate> chosenSet = new HashSet<>();

          candidates.put(knowledgeBase, candidatesSet);
          chosen.put(knowledgeBase, chosenSet);

          EntityCandidate bestCandidate = null;
          for (TCellAnnotation annotation : annotations) {
            uk.ac.shef.dcs.kbsearch.model.Entity clazz = annotation.getAnnotation();

            Entity entity = new Entity(clazz.getId(), clazz.getLabel());
            Likelihood likelihood = new Likelihood(annotation.getFinalScore());

            EntityCandidate candidate = new EntityCandidate(entity, likelihood);

            candidatesSet.add(candidate);

            if (bestCandidate == null || bestCandidate.getLikelihood().getValue() < candidate
                .getLikelihood().getValue()) {
              bestCandidate = candidate;
            }
          }

          if (bestCandidate != null) {
            chosenSet.add(bestCandidate);
          }
        }

        CellAnnotation cellAnnotation = new CellAnnotation(candidates, chosen);
        cellAnnotations[rowIndex][columnIndex] = cellAnnotation;
      }
    }

    return cellAnnotations;
  }

  private static List<HeaderAnnotation> convertColumnAnnotations(KnowledgeBase knowledgeBase,
      TAnnotation original) {
    int columnCount = original.getCols();
    List<HeaderAnnotation> headerAnnotations = new ArrayList<>(columnCount);

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      TColumnHeaderAnnotation[] annotations = original.getHeaderAnnotation(columnIndex);

      HashMap<KnowledgeBase, Set<EntityCandidate>> candidates = new HashMap<>();
      HashMap<KnowledgeBase, Set<EntityCandidate>> chosen = new HashMap<>();

      if (annotations != null) {
        Set<EntityCandidate> candidatesSet = new HashSet<>();
        Set<EntityCandidate> chosenSet = new HashSet<>();

        candidates.put(knowledgeBase, candidatesSet);
        chosen.put(knowledgeBase, chosenSet);

        EntityCandidate bestCandidate = null;
        for (TColumnHeaderAnnotation annotation : annotations) {
          Clazz clazz = annotation.getAnnotation();

          Entity entity = new Entity(clazz.getId(), clazz.getLabel());
          Likelihood likelihood = new Likelihood(annotation.getFinalScore());

          EntityCandidate candidate = new EntityCandidate(entity, likelihood);

          candidatesSet.add(candidate);

          if (bestCandidate == null
              || bestCandidate.getLikelihood().getValue() < candidate.getLikelihood().getValue()) {
            bestCandidate = candidate;
          }
        }

        if (bestCandidate != null) {
          chosenSet.add(bestCandidate);
        }
      }

      HeaderAnnotation headerAnnotation = new HeaderAnnotation(candidates, chosen);
      headerAnnotations.add(headerAnnotation);
    }

    return headerAnnotations;
  }
}
