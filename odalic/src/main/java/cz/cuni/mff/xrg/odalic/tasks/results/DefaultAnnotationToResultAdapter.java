package cz.cuni.mff.xrg.odalic.tasks.results;

import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.*;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

public class DefaultAnnotationToResultAdapter implements AnnotationToResultAdapter {

    @Override
    public Result toResult(TAnnotation original) {
        // TODO: Pass this from the core
        KnowledgeBase knowledgeBase = new KnowledgeBase("DBpedia");

        List<HeaderAnnotation> headerAnnotations = ConvertColumnAnnotations(original, knowledgeBase);
        CellAnnotation[][] cellAnnotations = ConvertCellAnnotations(original, knowledgeBase);
        Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelations = ConvertColumnRelations(original, knowledgeBase);
        Map<CellRelationPosition, CellRelationAnnotation> cellCellRelations = ConvertCellRelations(original, knowledgeBase);
        ColumnPosition subjectColumn = new ColumnPosition(original.getSubjectColumn());

        Result result = new Result(subjectColumn, headerAnnotations, cellAnnotations, columnRelations, cellCellRelations);

        return result;
    }

    Map<CellRelationPosition, CellRelationAnnotation> ConvertCellRelations(TAnnotation original, KnowledgeBase knowledgeBase) {
        Map<CellRelationPosition, CellRelationAnnotation> cellCellRelations = new HashMap<>();
        for (Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> columnAnnotations : original.getCellcellRelations().entrySet() ){
            for(Map.Entry<Integer, List<TCellCellRelationAnotation>> annotations : columnAnnotations.getValue().entrySet()){
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

                    if (bestCandidate == null || bestCandidate.getLikelihood().getValue() < candidate.getLikelihood().getValue()) {
                        bestCandidate = candidate;
                    }
                }

                if (bestCandidate != null){
                    chosenSet.add(bestCandidate);
                }

                RelationColumns relationColumns = columnAnnotations.getKey();
                ColumnRelationPosition columnPosition = new ColumnRelationPosition(new ColumnPosition(relationColumns.getSubjectCol()), new ColumnPosition(relationColumns.getObjectCol()));
                RowPosition rowPosition = new RowPosition(annotations.getKey());
                CellRelationPosition position = new CellRelationPosition(columnPosition, rowPosition);
                CellRelationAnnotation relationAnnotation = new CellRelationAnnotation(candidates, chosen);
                cellCellRelations.put(position, relationAnnotation);
            }
        }
        return cellCellRelations;
    }

    Map<ColumnRelationPosition, ColumnRelationAnnotation> ConvertColumnRelations(TAnnotation original, KnowledgeBase knowledgeBase) {
        Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelations = new HashMap<>();
        for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> annotations : original.getColumncolumnRelations().entrySet() ){
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

                if (bestCandidate == null || bestCandidate.getLikelihood().getValue() < candidate.getLikelihood().getValue()) {
                    bestCandidate = candidate;
                }
            }

            if (bestCandidate != null){
                chosenSet.add(bestCandidate);
            }

            RelationColumns relationColumns = annotations.getKey();
            ColumnRelationPosition position = new ColumnRelationPosition(new ColumnPosition(relationColumns.getSubjectCol()), new ColumnPosition(relationColumns.getObjectCol()));
            ColumnRelationAnnotation relationAnnotation = new ColumnRelationAnnotation(candidates, chosen);
            columnRelations.put(position, relationAnnotation);
        }
        return columnRelations;
    }

    CellAnnotation[][] ConvertCellAnnotations(TAnnotation original, KnowledgeBase knowledgeBase) {
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

                        if (bestCandidate == null || bestCandidate.getLikelihood().getValue() < candidate.getLikelihood().getValue()) {
                            bestCandidate = candidate;
                        }
                    }

                    if (bestCandidate != null){
                        chosenSet.add(bestCandidate);
                    }
                }

                CellAnnotation cellAnnotation = new CellAnnotation(candidates, chosen);
                cellAnnotations[rowIndex][columnIndex] = cellAnnotation;
            }
        }

        return cellAnnotations;
    }

    List<HeaderAnnotation> ConvertColumnAnnotations(TAnnotation original, KnowledgeBase knowledgeBase) {
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

                    if (bestCandidate == null || bestCandidate.getLikelihood().getValue() < candidate.getLikelihood().getValue()) {
                        bestCandidate = candidate;
                    }
                }

                if (bestCandidate != null){
                    chosenSet.add(bestCandidate);
                }
            }

            HeaderAnnotation headerAnnotation = new HeaderAnnotation(candidates, chosen);
            headerAnnotations.add(headerAnnotation);
        }

        return  headerAnnotations;
    }
}
