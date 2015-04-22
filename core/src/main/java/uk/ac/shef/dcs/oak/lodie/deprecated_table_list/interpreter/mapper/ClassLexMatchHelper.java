package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix1D;
import uk.ac.shef.dcs.oak.lodie.PlaceHolder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter.FilterPolicy;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.concept.ConceptFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label.LabelFinder;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/05/13
 * Time: 16:55
 * <p/>
 * Given a lexical term, we get
 * - the uri of concepts whose labels match the term
 * - the equivalent concepts of these concepts
 * - their labels
 */
class ClassLexMatchHelper {
    public static void findCandidate_Concepts_Plus_Labels_ForHeaders(LTable table, ObjectMatrix1D candidateMatrix,
                                                                     Map<String, Set<String>> labels,
                                                                     Map<String, String> mapOfEquivalence,
                                                                     ConceptFinder[] conceptFinders,
                                                                     LabelFinder[] labelFinders,
                                                                     List<Integer> dataColumns, FilterPolicy... policies) {
        //finding candidates for "header"
        Set<String> uniqueURIs = new HashSet<String>();

        for (int col=0; col<table.getNumHeaders(); col++) {
            if (!dataColumns.contains(col))
                continue;
            String headerText = table.getColumnHeader(col).toString();
            if (headerText.equals(PlaceHolder.TABLE_HEADER_UNKNOWN)) {
                continue;
            }
            if (TableContent2KnowledgeBaseMapper.
                    skip(table, 0, col, policies))
                continue;

            Set<String> concepts = new HashSet<String>();
            for (ConceptFinder finder : conceptFinders) {
                List<String[]> c = null;
                try {
                    c = (List<String[]>) finder.findCandidates(TableContent2KnowledgeBaseMapper
                            .cleanTableCellValue(headerText));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (c != null) {
                    for (String[] conceptAndEquiv : c) {
                        concepts.add(conceptAndEquiv[0]);
                        if (!conceptAndEquiv[1].equals("")) {
                            concepts.add(conceptAndEquiv[1]);
                            mapOfEquivalence.put(conceptAndEquiv[0], conceptAndEquiv[1]);
                            mapOfEquivalence.put(conceptAndEquiv[1], conceptAndEquiv[0]);
                        }

                    }
                    //concepts.addAll(c);
                }
            }
            candidateMatrix.set(col, concepts);
            uniqueURIs.addAll(concepts);
        }

        TableContent2KnowledgeBaseMapper.
                findLabelsForResources(labels, mapOfEquivalence, uniqueURIs, labelFinders);
    }
}
