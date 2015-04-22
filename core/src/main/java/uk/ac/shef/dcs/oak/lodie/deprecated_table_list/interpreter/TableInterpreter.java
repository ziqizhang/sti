package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter.FilterPolicy;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.concept.ConceptFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity.EntityFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label.LabelFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property.PredicateOfObjectAndPredDomainFinderSparql;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property.PropertyFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass.SuperConceptFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper.TableContent2KnowledgeBaseMapper;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 11/03/13
 * Time: 13:20
 */
public class TableInterpreter {

    private static Logger log = Logger.getLogger(TableInterpreter.class.getName());
    private ConceptFinder[] candidate_ConceptsFinder_forHeader; //finds concepts in KB that match a header text in a table
    private PropertyFinder[] candidate_PropertiesFinder_forHeader;//finds properties in KB that match a header text in a table
    private EntityFinder[] cellCandidateFinder;//finds instances in KB that match a text description in table cell (not header)
    private LabelFinder[] labelFinder_forHeaderConcepts; //finds labels for those *concepts* that match a header text
    private LabelFinder[] labelFinder_forHeaderProperties; //finds labels for those *properties* that match a header text
    private LabelFinder[] labelFinderForCell; //finds labels for those instances that match a table cell value
    private RelationFinder[] relationFinderBetweenURIs; //finds relations between two uris
    private SuperConceptFinder[] superclassFinder;  //find superclasses of instances (uris)
    private PredicateOfObjectAndPredDomainFinderSparql[] predicateOfObjectFinder;  //given a value that is at object position, find all its predicates
    private CandidateFinder[] domainOfPropFinder;

    public TableInterpreter(
            ConceptFinder[] headerCandidateConceptFinder,
            PropertyFinder[] headerCandidatePropertiesFinder,
            EntityFinder[] cellCandidateFinder,
            LabelFinder[] labelFinderForHeader_concepts,
            LabelFinder[] labelFinderForHeader_properties,
            LabelFinder[] labelFinderForCell,
            SuperConceptFinder[] superclassFinder,
            RelationFinder[] relationFinderBetweenURIs,
            PredicateOfObjectAndPredDomainFinderSparql[] predicateOfObjectFinder,
            CandidateFinder[] domainOfPropFinder
    ) {
        this.candidate_ConceptsFinder_forHeader = headerCandidateConceptFinder;
        this.candidate_PropertiesFinder_forHeader = headerCandidatePropertiesFinder;
        this.cellCandidateFinder = cellCandidateFinder;
        this.labelFinder_forHeaderConcepts = labelFinderForHeader_concepts;
        this.labelFinder_forHeaderProperties = labelFinderForHeader_properties;
        this.labelFinderForCell = labelFinderForCell;
        this.relationFinderBetweenURIs = relationFinderBetweenURIs;
        this.superclassFinder = superclassFinder;
        this.predicateOfObjectFinder = predicateOfObjectFinder;
        this.domainOfPropFinder = domainOfPropFinder;
    }

    //todo: find main subject column
    public void interpret(LTable table, TableContentIndexer indexer, FilterPolicy... policies) {
        //////////////// Initialisation of objects ///////////////
        List<Integer> dataColumns =
                findDataColumns(table, policies);//which columns contains real data? let's only interpret these columns
        int mainSubjectColumn = findMainSubjectColumn(table);  //which is the main subject column? relations will only
        //be interpreted for columns between main and others
        Map<String, Set<String>> all_labels = new HashMap<String, Set<String>>(); //track labels for each candidate URI
        Map<String, String> props2Domains = new HashMap<String, String>(); //track properties and their domains
        Map<String, String> mapOfEquivalence = new HashMap<String, String>(); //track equivalence of resources

        /////////////// Candidates for headers (querying header texts directly) ///////////
        //a. find concepts AND equivalent concepts matching a header's text
        //a1. also finds the labels for each candidates
        log.info("1 Find *concepts* matching a header's text, also labels of concepts");
        ObjectMatrix1D header_candConcept_lexMatched = new SparseObjectMatrix1D(table.getNumCols());
        TableContent2KnowledgeBaseMapper.findCandidate_Concepts_Plus_Labels_ForHeaders(
                table, header_candConcept_lexMatched,
                all_labels, mapOfEquivalence,candidate_ConceptsFinder_forHeader, labelFinder_forHeaderConcepts, dataColumns, policies);
        //b. find properties AND equivalent properties matching a header's text
        //b1. also finds the labels for each property
        log.info("2 Find *properties* matching a header's text, also labels of properties, domains of properties");
        ObjectMatrix1D header_candProp_lexMatched = new SparseObjectMatrix1D(table.getNumCols());
        TableContent2KnowledgeBaseMapper.findCandidate_Properties_Plus_Labels_ForHeaders(
                table, header_candProp_lexMatched,
                all_labels, mapOfEquivalence,
                candidate_PropertiesFinder_forHeader,
                labelFinder_forHeaderProperties, dataColumns, policies);


        //(we also need to find concepts/properties for headers based on their cell values under the header.
        // but for that we need to firstly find candidates for cell values

        //////////////// Candidates for data cell values ///////////////////////
        //a. find candidate URIs for data cells
        //a0. Also as a shortcut, finds superclasses NOT labels
        log.info("3 Find instances matching a data cell's text, also labels of instances");
        ObjectMatrix2D cell_instance = new SparseObjectMatrix2D(table.getNumRows(), table.getNumCols()); //keeps track of all candidates for each cell
        TableContent2KnowledgeBaseMapper.findCandidate_Instances_and_Labels_ForDataCells(
                table, dataColumns, cell_instance,
                all_labels, mapOfEquivalence,
                cellCandidateFinder, labelFinderForCell,
                policies);
        //a1. next find labels for data cells
        Set<String> uniqueURIsForInstances = new HashSet<String>();
        for (int i = 1; i < cell_instance.rows(); i++) {
            for (int j = 0; j < cell_instance.columns(); j++) {
                Object o = cell_instance.get(i, j);
                if (o == null)
                    continue;
                uniqueURIsForInstances.addAll((HashSet<String>) o);
            }
        }

        /////////////////// Candidates for headers (based on cell values underneath each header) ///////
        //a. header candidate CONCEPTS based on data - these are superclasses AND equivalent superclasses
        // of instances in data cells (IF STEP 3 run before this, superclasses have been cached already)
        //a1. also find labels of these superclasses
        //a2. ALSO DISCARD infrequent superclasses
        log.info("4 Find superclasses of instances in data cells, also labels of superclasses");
        ObjectMatrix2D header_candConcept_superClassOfCellInst = TableContent2KnowledgeBaseMapper.
                findCandidate_Concepts_and_Labels_ForHeadersBySuperclassesOfDataValues(
                        table, cell_instance, dataColumns, all_labels, mapOfEquivalence,
                        superclassFinder,
                        labelFinderForCell,
                        policies);
        //b. header candidate PROPERTIES based on data - these are predicates whose objects are instances found for data cell values
        //b1. also find labels of these properties
        //b2. ALSO DISCARD infrequent props
        //THIS METHOD FILLS UP props2domain map
        log.info("5 Find properties of instances in data cells, also labels of properties");
        ObjectMatrix2D header_candProp_propOfCellInst = TableContent2KnowledgeBaseMapper.
                findCandidate_Properties_plus_Domains_plus_Labels_ForHeadersByDataValues(
                        table, cell_instance, dataColumns, all_labels,
                        mapOfEquivalence,
                        props2Domains,
                        predicateOfObjectFinder,
                        labelFinder_forHeaderProperties,
                        policies);


        ///////////////////////////// Relations //////////////////////////////
        //next find relations  //todo: be careful about "equivalentclass", "subclassof" kind of relations in the candidates
        //a. relations between columns using DATA CELLs
        log.info("6 Find relations between data cell instances");
        List<LTableColumn2ColumnRelation> relations_cellByCell_basedOnInstances =
                TableContent2KnowledgeBaseMapper.
                        findRelationsBetweenDataCells(cell_instance,
                                table, dataColumns, mainSubjectColumn, relationFinderBetweenURIs);


        log.info("7 Find relations between headers' candidate properties (lexically matched)");
        //I. relations between columns using HEADERS' CANDIDATE PROPERTIES(MUST CONSIDER LEVEL of connecting concepts for two props that share domains
        List<LTableColumn2ColumnRelation> relations_headerByHeader_basedOnProperties =
                TableContent2KnowledgeBaseMapper.
                        findRelationsBetweenHeaders_By_Properties(
                                header_candProp_lexMatched,
                                props2Domains,
                                dataColumns, mainSubjectColumn, domainOfPropFinder
                        );

        log.info("8 Find relations between headers' candidate properties (properties of data cell instances)");
        //II. relation between columns using DATA CELL VALUES' CANDIDATE PROPERTIES (MUST CONSIDER LEVELS OF connecting...)
        List<LTableColumn2ColumnRelation> relations_cellByCell_basedOnProperties =
                TableContent2KnowledgeBaseMapper.findRelationsBetweenHeaders_ByDataCellProperties(
                        header_candProp_propOfCellInst, props2Domains,
                        dataColumns, mainSubjectColumn, domainOfPropFinder);

        /*
              //input comes from:
              //domains of all properties (populated by: a)findRelationsBetweenHeaders_By_Properties
                                                          b)findCandidate_Properties_plus_Domains_plus_Labels_ForHeadersByDataValues)
                all classes  (populated by: a) header_candidate_asConcept   b) header_candidate_asConceptByData
        */
        log.info("9-- Resolving concept hierarchies for this table");
        //Firstly gather all concepts
        Set<String> distinctConcepts = new HashSet<String>();
        for(String v: props2Domains.values())
                distinctConcepts.add(v);
        for(int c = 0; c<header_candConcept_lexMatched.size(); c++){
            Object o = header_candConcept_lexMatched.get(c);
            if(o==null)
                continue;
            distinctConcepts.addAll((Set<String>)header_candConcept_lexMatched.get(c));
        }
        for(int c=0; c<header_candConcept_superClassOfCellInst.columns(); c++){
            for(int r=0; r<header_candConcept_superClassOfCellInst.rows(); r++){
                Object o = header_candConcept_superClassOfCellInst.get(r, c);
                if(o==null)
                    continue;
                Map<String, Set<String>> content = ( Map<String, Set<String>>)o;
                for(Set<String> values :content.values())
                    distinctConcepts.addAll(values);
            }
        }

        //todo: update equivalence

        //todo: resolve concept hierarchies

        /*todo: this must be done after resolving hierarchies
        */
        log.info("9 Find relations between headers' candidate concepts (lexically matched)");
        //b. relations between columns using HEADERS' CANDIDATE CONCEPTS
        List<LTableColumn2ColumnRelation> relations_headerByHeader_basedOnConcepts =
                TableContent2KnowledgeBaseMapper.
                        findRelationsBetweenHeaders_By_Concepts(header_candConcept_lexMatched,
                                dataColumns, mainSubjectColumn,
                                relationFinderBetweenURIs);
        log.info("10 Find relations between headers' candidate concepts (superclasses of data cell instances");
        //c. relation between columns using SUPERCLASSES OF DATA CELLS (MUST CONSIDER LEVELS OF SUPERCLASSES)
        List<LTableColumn2ColumnRelation> relations_cellByCell_basedOnSuperclasses =
                TableContent2KnowledgeBaseMapper.findRelationsBetweenHeaders_ByDataCellSuperclasses(
                        header_candConcept_superClassOfCellInst,
                        dataColumns, mainSubjectColumn, relationFinderBetweenURIs);


        log.info("CANDIDATE KNOWLEDGE RESOURCE RETRIEVAL COMPLETE. TIDY RESOURCES...");

        //todo: update all labels
        ///////////////////// Before the next step, re-update labels (by also processing URIs) //////////////////


        //todo: add context features

        //todo: build models and indexes

        //todo: apply inference

        //
    }

    /**
     * find which columns of the table contain actual resolvable data
     *
     * @param table
     * @return
     */
    private List<Integer> findDataColumns(LTable table, FilterPolicy... policies) {
        List<Integer> dataColumns = new ArrayList<Integer>();
        for (int c = 0; c < table.getNumCols(); c++) {
            for (FilterPolicy policy : policies) {
                if (policy.discard(table, -1, c))
                    continue;
            }
            dataColumns.add(c);
        }
        return dataColumns;
    }

    //todo  NOTE: when there are only 2 columns, do not need to find main. simply assign X
    private int findMainSubjectColumn(LTable table) {
        return 0;
    }


    /**
     * find candidate relations between columns of the header row
     *
     * @param -           the LTable object to be interpreted
     * @param dataColumns - integer ids indicating which columns of the table contain data to be interpreted
     */
    private List<LTableColumn2ColumnRelation> findRelationsBetweenHeaderColumns(ObjectMatrix1D headerCandidateConcepts,
                                                                                List<Integer> dataColumns, int mainSubjectColumn) {

        List<LTableColumn2ColumnRelation> result = new ArrayList<LTableColumn2ColumnRelation>();
        for (int c : dataColumns) {
            if (c == mainSubjectColumn)
                continue;

            LTableColumn2ColumnRelation tccr = new LTableColumn2ColumnRelation(mainSubjectColumn, c);

            Object main = headerCandidateConcepts.get(mainSubjectColumn);
            Object other = headerCandidateConcepts.get(c);
            if (main == null || other == null)
                continue;
            //each cell may have multiple candidates, retrieve them all
            List<String> mainCandidates = (List<String>) main;
            List<String> otherCandidates = (List<String>) other;

            //get all pairs b/w the two lists and their relations, create triples accordingly
            for (String m : mainCandidates) {
                for (String o : otherCandidates) {
                    Set<String> relations = new HashSet<String>();
                    boolean relationReverse = false;
                    //find relations that link m as subject and o as object
                    for (RelationFinder finder : relationFinderBetweenURIs) {
                        List<String> rels = null;
                        try {
                            rels = finder.findRelationBetween(m, o);
                        } catch (Exception e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        if (rels != null)
                            relations.addAll(rels);
                    }

                    if (relations.size() == 0) {
                        //find relations that link m as Object and o as Subject
                        for (RelationFinder finder : relationFinderBetweenURIs) {
                            List<String> rels = null;
                            try {
                                rels = finder.findRelationBetween(o, m);
                            } catch (Exception e) {

                                if (rels != null)
                                    relations.addAll(rels);
                            }
                            relationReverse = true;
                        }
                    }

                    for (String rel : relations) {
                        TripleInTableRow triple = new TripleInTableRow(m, rel, o, mainSubjectColumn, c, 0);
                        triple.setPredicateReverse(relationReverse);
                        tccr.addTriple(triple);
                    }
                }//end for each candidate uri for the *object* cell content
            }//end for each candidate uri for the *subject* cell content

            result.add(tccr);
        } //end for ecah datacolumn
        return result;
    }


}
