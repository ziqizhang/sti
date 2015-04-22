package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.LTableColumn2ColumnRelation;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter.FilterPolicy;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.concept.ConceptFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity.EntityFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label.LabelFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property.PredicateOfObjectAndPredDomainFinderSparql;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property.PropertyFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass.SuperConceptFinder;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 15/03/13
 * Time: 10:16
 */
public class TableContent2KnowledgeBaseMapper {

    private static Logger log = Logger.getLogger(TableContent2KnowledgeBaseMapper.class.getName());


    /**
     * for a text value in a Header cell, finds: candidate concepts/properties and their labels
     *
     * @param table
     * @param policies
     */
    public static void findCandidate_Concepts_Plus_Labels_ForHeaders(LTable table, ObjectMatrix1D candidateMatrix,
                                                                     Map<String, Set<String>> labels,
                                                                     Map<String, String> mapOfEquivalence,
                                                                     ConceptFinder[] conceptFinders,
                                                                     LabelFinder[] labelFinders,
                                                                     List<Integer> dataColumns, FilterPolicy... policies) {
        ClassLexMatchHelper.findCandidate_Concepts_Plus_Labels_ForHeaders(
                table, candidateMatrix,
                labels,
                mapOfEquivalence,
                conceptFinders,
                labelFinders,
                dataColumns, policies
        );
    }


    public static void findCandidate_Properties_Plus_Labels_ForHeaders(LTable table, ObjectMatrix1D candidateMatrix,
                                                                       Map<String, Set<String>> labels,
                                                                       Map<String, String> mapOfEquivalence,
                                                                       PropertyFinder[] propertyFinders,
                                                                       LabelFinder[] labelFinders,
                                                                       List<Integer> dataColumns, FilterPolicy... policies) {
        PropertyLexMatchHelper.findCandidate_Properties_Plus_Labels_ForHeaders(
                table, candidateMatrix,
                labels,
                mapOfEquivalence,
                propertyFinders,
                labelFinders,
                dataColumns, policies
        );
    }

    /*
    NOTE: predicateOfObjectFinders will not append "<" or ">" to sparql queries. This method will have to take
    care of that

    THIS METHOD FILLS UP props2domain map
     */
    public static ObjectMatrix2D findCandidate_Properties_plus_Domains_plus_Labels_ForHeadersByDataValues(
            LTable table,
            ObjectMatrix2D cell_candidate_asInstance,
            List<Integer> dataColumns,
            Map<String, Set<String>> all_labels,
            Map<String, String> mapOfEquivalence,
            Map<String, String> props2Domains,
            PredicateOfObjectAndPredDomainFinderSparql[] predicateOfObjectFinder,
            LabelFinder[] labelFinder_forHeaderProperties,
            FilterPolicy[] policies) {
        return PropertyOfInstanceInCellHelper.
                findCandidateProperties_and_Domains_and_Labels_ForHeadersByDataValues(log,
                        table,
                        cell_candidate_asInstance,
                        dataColumns,
                        all_labels,
                        mapOfEquivalence,
                        props2Domains,
                        predicateOfObjectFinder,
                        labelFinder_forHeaderProperties,
                        policies);
    }

    /*
   returns an OjbjectMatrix2D, where each column is a column in the table. each cell contains a Map<String, List>, where key=candidate_uri_for_this_cell,
   and List = superclasses for this uri

   if we do not want to get labels, simply provide a "null" value for the param "labels"
   //todo: some uris have "superclass" that is exactly the same as itself
    */
    public static ObjectMatrix2D
    findCandidate_Concepts_and_Labels_ForHeadersBySuperclassesOfDataValues(LTable table,
                                                                           ObjectMatrix2D cellWithCandidates,
                                                                           List<Integer> dataColumns,
                                                                           Map<String, Set<String>> labels,
                                                                           Map<String, String> mapOfEquivalence,
                                                                           SuperConceptFinder[] superclassFinders,
                                                                           LabelFinder[] labelFinder_forHeaderConcepts,
                                                                           FilterPolicy... policies) {
        return SuperclassOfInstanceInCellHelper.
                findCandidate_Concept_and_Labels_ForHeadersBySuperclassesOfDataValues(log,
                        table,
                        cellWithCandidates,
                        dataColumns,
                        labels,
                        mapOfEquivalence,
                        superclassFinders,
                        labelFinder_forHeaderConcepts,
                        policies);
    }


    public static void findCandidate_Instances_and_Labels_ForDataCells(LTable table,
                                                                       List<Integer> dataColumns,
                                                                       ObjectMatrix2D candidates,
                                                                       Map<String, Set<String>> all_labels,
                                                                       Map<String, String> mapOfEquivalence,
                                                                       EntityFinder[] cellCandidateFinder,
                                                                       LabelFinder[] labelFinder,
                                                                       FilterPolicy... policies) {
        InstanceLexMatchHelper.
                findCandidatesForDataCells(log, table,
                        dataColumns, candidates,
                        all_labels, mapOfEquivalence,
                        cellCandidateFinder, labelFinder,
                        policies);

    }


    /**
     * //todo: currently we only find relations with *main*subject columns. not other column pairs
     * find candidate relations between columns
     *
     * @param candidates  - candidate uris for each value in each cell of a table
     * @param -           the LTable object to be interpreted
     * @param dataColumns - integer ids indicating which columns of the table contain data to be interpreted
     */
    public static List<LTableColumn2ColumnRelation> findRelationsBetweenDataCells(ObjectMatrix2D candidates,
                                                                                  LTable table,
                                                                                  List<Integer> dataColumns,
                                                                                  int mainSubjectColumn,
                                                                                  RelationFinder... relationFinder) {
        return RelationBetweenInstanceHelper.findRelationsBetweenDataCells(
                log,
                candidates,
                table,
                dataColumns,
                mainSubjectColumn,
                relationFinder
        );

    }

    /**
     * A BRUTE-FORCE finding approach
     *
     * @param header_candidate_asConcept
     * @param dataColumns
     * @param mainSubjectColumn
     * @param relationFinderBetweenURIs
     * @return
     */
    public static List<LTableColumn2ColumnRelation> findRelationsBetweenHeaders_By_Concepts(ObjectMatrix1D header_candidate_asConcept,
                                                                                            List<Integer> dataColumns, int mainSubjectColumn, RelationFinder[] relationFinderBetweenURIs) {
        return RelationBetweenClassesHelper.findRelationsBetweenHeaders_By_Concepts(
                log, header_candidate_asConcept, dataColumns, mainSubjectColumn, relationFinderBetweenURIs
        );
    }


    /**
     *
     */
    public static List<LTableColumn2ColumnRelation> findRelationsBetweenHeaders_ByDataCellSuperclasses(
            ObjectMatrix2D candidates,
            List<Integer> dataColumns,
            int mainSubjectColumn,
            RelationFinder... relationFinder) {
        return RelationBetweenClassesHelper.
                findRelationsBetweenHeaders_ByDataCellSuperclasses(
                        log,
                        candidates,
                        dataColumns,
                        mainSubjectColumn,
                        relationFinder
                );
    }


    public static List<LTableColumn2ColumnRelation> findRelationsBetweenHeaders_ByDataCellProperties(
            ObjectMatrix2D candidates,
            Map<String, String> prop2Domain,
            List<Integer> dataColumns,
            int mainSubjectColumn,
            CandidateFinder... domainOfPropFinders) {

        return RelationBetweenPropertyHelper.findRelationsBetweenHeaders_ByDataCellProperties(
                log,
                candidates,
                prop2Domain,
                dataColumns,
                mainSubjectColumn,
                domainOfPropFinders
        );


    }

    public static List<LTableColumn2ColumnRelation> findRelationsBetweenHeaders_By_Properties(
                ObjectMatrix1D candidates,
                Map<String, String> prop2Domain,
                List<Integer> dataColumns,
                int mainSubjectColumn,
                CandidateFinder... domainOfPropFinder) {
        return RelationBetweenPropertyHelper.findRelationsBetweenHeaders_By_Properties(
                candidates, prop2Domain, dataColumns, mainSubjectColumn,domainOfPropFinder
        );
    }






    public static void findLabelsForResources(Map<String, Set<String>> all_labels,
                                              Map<String, String> mapOfEquivalence,
                                              Set<String> resources,
                                              LabelFinder... labelFinders) {
        log.info("\tfinding labels, resources = " + resources.size() + "; finders = " + labelFinders.length);
        if (all_labels != null) {
            for (String sc : resources) {
                String equivalence = mapOfEquivalence.get(sc);
                Set<String> l = all_labels.get(sc);
                if (l != null) //if labels are found in the map, it means we have searched for it before, no need to search again
                    continue;
                l = new HashSet<String>();

                for (LabelFinder f : labelFinders) {
                    List<String> found = null;
                    try {
                        found = f.findCandidates(sc);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (found == null)
                        continue;
                    l.addAll(found);
                }
                all_labels.put(sc, l);
                if (equivalence != null)
                    all_labels.put(equivalence, l);
            }
        }
    }

    /**
     * The filter policies decides if the corresponding row/col in the table should be interpreted or not.
     * e.g., we may not want to interpret a digit, a date ...
     *
     * @param table
     * @param row      when row=-1, the filter policy checkes if the entire column is to be kept
     * @param col      when col=-1, the filter policy checks if the entire row is to be kept
     * @param policies
     * @return
     */
    static boolean skip(LTable table, int row, int col, FilterPolicy... policies) {
        for (FilterPolicy p : policies) {
            if (p.discard(table, row, col))
                return true;
        }
        return false;
    }

    static String cleanTableCellValue(String content) {
        return content;
    }


}
