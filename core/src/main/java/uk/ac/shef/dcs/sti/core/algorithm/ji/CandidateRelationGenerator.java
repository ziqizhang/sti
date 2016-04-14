package uk.ac.shef.dcs.sti.core.algorithm.ji;

import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.kbsearch.model.Resource;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by zqz on 01/05/2015.
 */
public class CandidateRelationGenerator {
    private boolean includeCellCellRelations = true;
    private JIAdaptedAttributeMatcher matcher;
    private KBSearch kbSearch;

    private static final Logger LOG = Logger.getLogger(CandidateRelationGenerator.class.getName());

    public CandidateRelationGenerator(JIAdaptedAttributeMatcher matcher,
                                      KBSearch kbSearch,
                                      boolean includeCellCellRelations) {
        this.matcher = matcher;
        this.kbSearch = kbSearch;
        this.includeCellCellRelations = includeCellCellRelations;
    }

    public void generateInitialColumnColumnRelations(TAnnotationJI tableAnnotations,
                                                     Table table, boolean useSubjectColumn,
                                                     Collection<Integer> ignoreColumns) throws IOException, KBSearchException {
        //RelationDataStructure result = new RelationDataStructure();

        //mainColumnIndexes contains indexes of columns that are possible NEs
        Map<Integer, DataTypeClassifier.DataType> colTypes
                = new HashMap<>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getType();
            colTypes.put(c, type);
        }

        //candidate relations between any pairs of columns
        List<Integer> subjectColumnsToConsider = new ArrayList<>();
        if (useSubjectColumn)
            subjectColumnsToConsider.add(tableAnnotations.getSubjectColumn());
        else {
            for (int c = 0; c < table.getNumCols(); c++) {
                if (!ignoreColumns.contains(c))
                    subjectColumnsToConsider.add(c);
            }
        }

        if (includeCellCellRelations) {
            LOG.info("\t\t>> relations derived from rows");
            addRelationsFromRowEvidence(table, tableAnnotations, subjectColumnsToConsider, ignoreColumns, colTypes);
        }

        //using pairs of column headers to compute candidate relations between columns, update tableannotation object
        LOG.info("\t\t>> relations derived from column clazz annotations");
        addRelationsFromColumnClazzAnnotations(table,
                tableAnnotations,
                colTypes, kbSearch, subjectColumnsToConsider, ignoreColumns);


        LOG.info("\t\t>> update entity-relation affinity scores");
        //further, update entity-relation scores, to account for 1:n relations
        updateEntityRelationAffinityScores(table,tableAnnotations,subjectColumnsToConsider,ignoreColumns);
    }


    //relation candidates can come from rows
    private void addRelationsFromRowEvidence(
            Table table,
            TAnnotationJI tableAnnotations,
            List<Integer> subjectColumnsToConsider,
            Collection<Integer> ignoreColumns,
            Map<Integer, DataTypeClassifier.DataType> colTypes
    ) throws IOException {
        for (int subjectColumn : subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;

            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn || ignoreColumns.contains(objectColumn))
                    continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(objectColumn).getFeature().getMostFrequentDataType().getType();
                if (!columnDataType.equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                for (int r = 0; r < table.getNumRows(); r++) {
                    //in JI, all candidate NEs (the disambiguated NE) is needed from each cell to aggregate candidate relation
                    TCellAnnotation[] subjectCells = tableAnnotations.getContentCellAnnotations(r, subjectColumn);
                    TCellAnnotation[] objectCells = tableAnnotations.getContentCellAnnotations(r, objectColumn);
                    //matches obj of facts of subject entities against object cell text and candidate entity labels.
                    //also create evidence for entity-relation, concept-relation
                    List<JIAdaptedAttributeMatcher.MatchResult> matchResults = matcher.matchCellAnnotations(
                            Arrays.asList(subjectCells),
                            Arrays.asList(objectCells),
                            colTypes.get(objectColumn));
                    for (JIAdaptedAttributeMatcher.MatchResult mr : matchResults) {
                        createCandidateAnnotation(tableAnnotations,
                                r, subjectColumn, objectColumn,
                                mr.attribute, mr.score, mr.subjectAnnotation, mr.objectAnnotations);
                    }
                }
            }
        }

        //create columncolumnhrelation objects
        for (Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> e :
                tableAnnotations.getCellcellRelations().entrySet()) {

            //simply create dummy header relation annotations
            RelationColumns relationColumns = e.getKey(); //key indicating the directional relationship (subject col, object col)

            Set<String> candidateRelationURLs = new HashSet<>();
            Map<Integer, List<TCellCellRelationAnotation>> relation_on_rows = e.getValue();
            for (Map.Entry<Integer, List<TCellCellRelationAnotation>> entry :
                    relation_on_rows.entrySet()) {
                for (TCellCellRelationAnotation cbr : entry.getValue()) {
                    candidateRelationURLs.add(cbr.getRelationURI());
                }
            }

            for (String url : candidateRelationURLs) {
                tableAnnotations.addColumnColumnRelation(
                        new TColumnColumnRelationAnnotation(relationColumns, url,
                                url, 0.0)
                );
            }
        }
    }


    private void createCandidateAnnotation(TAnnotationJI tableAnnotation,
                                           int row, int subjectColumn, int objectColumn,
                                           Attribute attribute,
                                           double score,
                                           Resource sbjEntity,
                                           List<Resource> matchedObjCellCandidates) {
        tableAnnotation.addCellCellRelation(new TCellCellRelationAnotation(
                new RelationColumns(subjectColumn, objectColumn), row, attribute.getRelationURI(), attribute.getRelationURI(),
                new ArrayList<>(), score
        ));
        //subject entity, its concepts and relation
        populateEntityPairAndRelationScore(tableAnnotation, sbjEntity.getId(),
                attribute.getRelationURI(), matchedObjCellCandidates, subjectColumn, objectColumn);
        populateClazzPairAndRelationScoreFromRows(tableAnnotation, sbjEntity,
                row,
                attribute.getRelationURI(), matchedObjCellCandidates, subjectColumn, objectColumn, score);
    }

    private void populateEntityPairAndRelationScore(TAnnotationJI tableAnnotation,
                                                    String entityId, String relationURL, List<Resource> objEntities,
                                                    int relationFrom, int relationTo
    ) {
        for (Resource obj : objEntities) {
            tableAnnotation.setScoreEntityPairAndRelation(entityId,
                    obj.getId(),
                    TColumnColumnRelationAnnotation.toStringExpanded(
                            relationFrom, relationTo, relationURL), 1.0);
        }
    }

    private void populateClazzPairAndRelationScoreFromRows(TAnnotationJI tableAnnotation,
                                                           Resource sbjEntity,
                                                           int entityRow,
                                                           String relationURL,
                                                           List<Resource> matchedObjCellCandidates,
                                                           int relationFrom, int relationTo,
                                                           double maxScore) {
        //todo: false relation added to highly general types (person, religious_leader_title), maybe use only most specific type of sbj, obj
        for (Clazz sbjType : ((Entity) sbjEntity).getTypes()) {
            for (Resource obj : matchedObjCellCandidates) {
                Entity objEntity = (Entity) obj;
                for (Clazz objType : objEntity.getTypes()) {
                    if (sbjType.getId().equals(objType.getId())) continue;
                    tableAnnotation.setScoreClazzPairAndRelationFromRows(entityRow,
                            sbjType.getId(),
                            TColumnColumnRelationAnnotation.toStringExpanded(relationFrom, relationTo, relationURL),
                            objType.getId(),
                            maxScore);
                }
            }
        }
    }


    //relation candidates can come from column clazz annotations
    private void addRelationsFromColumnClazzAnnotations(Table table,
                                                        TAnnotationJI annotation,
                                                        Map<Integer, DataTypeClassifier.DataType> colTypes,
                                                        KBSearch kbSearch,
                                                        Collection<Integer> subjectColumnsToConsider,
                                                        Collection<Integer> ignoreColumns) throws KBSearchException {
        for (int subjectColumn : subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;

            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn || ignoreColumns.contains(objectColumn)) continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(objectColumn).getFeature().getMostFrequentDataType().getType();
                if (!columnDataType.equals(DataTypeClassifier.DataType.NAMED_ENTITY)) continue;
                TColumnHeaderAnnotation[] candidates_col1 = annotation.getHeaderAnnotation(subjectColumn);
                TColumnHeaderAnnotation[] candidates_col2 = annotation.getHeaderAnnotation(objectColumn);
                List<JIAdaptedAttributeMatcher.MatchResult> matchResults = matcher.matchColumnAnnotations(
                        Arrays.asList(candidates_col1),
                        Arrays.asList(candidates_col2),
                        colTypes.get(objectColumn),
                        kbSearch);

                for (JIAdaptedAttributeMatcher.MatchResult mr : matchResults) {
                    createCandidateAnnotation(annotation,
                            subjectColumn, objectColumn,
                            mr.attribute, mr.subjectAnnotation, mr.objectAnnotations);
                }
            }
        }
    }

    private void createCandidateAnnotation(TAnnotationJI annotation,
                                           int subCol,
                                           int objCol,
                                           Attribute attribute,
                                           Resource subjectClazz,
                                           List<Resource> objectClazz
                                           ) {
        String relation_key = TColumnColumnRelationAnnotation.toStringExpanded(subCol, objCol, attribute.getRelationURI());
        String subClazz = subjectClazz.getId();
        if (objectClazz != null) {
            for (Resource oc : objectClazz) {
                annotation.setScoreClazzPairAndRelationFromHeaderEvidence(subClazz, relation_key, oc.getId(), 1.0);
            }
        }

        List<TColumnColumnRelationAnnotation> candidateRelations =
                annotation.getColumncolumnRelations().get(
                        new RelationColumns(subCol, objCol)
                );
        if (candidateRelations == null) candidateRelations = new ArrayList<>();
        boolean contains = false;
        for (TColumnColumnRelationAnnotation hbr : candidateRelations) {
            if (hbr.getRelationURI().equals(attribute.getRelationURI())) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            annotation.addColumnColumnRelation(new TColumnColumnRelationAnnotation(
                    new RelationColumns(subCol, objCol), attribute.getRelationURI(), attribute.getRelationURI(), 0.0
            ));
        }
    }

    private void updateEntityRelationAffinityScores(Table table, TAnnotationJI tableAnnotations,
                                                    Collection<Integer> subjectColumnsToConsider,
                                                    Collection<Integer> ignoreColumns){
        for (int subjectColumn : subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;
            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn || ignoreColumns.contains(objectColumn)) continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(objectColumn).getFeature().getMostFrequentDataType().getType();
                if (!columnDataType.equals(DataTypeClassifier.DataType.NAMED_ENTITY)) continue;
                for (int r = 0; r < table.getNumRows(); r++) {
                    //in JI, all candidate NEs (the disambiguated NE) is needed from each cell to aggregate candidate relation
                    TCellAnnotation[] subjectCells = tableAnnotations.getContentCellAnnotations(r, subjectColumn);
                    TCellAnnotation[] objectCells = tableAnnotations.getContentCellAnnotations(r, objectColumn);
                    if (objectCells.length == 0 || subjectCells.length == 0) continue;
                    //matches obj of facts of subject entities against object cell text and candidate entity labels.
                    //also create evidence for entity-relation, concept-relation
                    matcher.matchCellAnnotationAndRelation(
                            Arrays.asList(subjectCells),
                            subjectColumn,
                            objectColumn,
                            tableAnnotations);
                }
            }
        }
    }
}
