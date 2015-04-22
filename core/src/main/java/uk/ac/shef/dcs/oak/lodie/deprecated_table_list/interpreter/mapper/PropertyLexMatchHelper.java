package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix1D;
import uk.ac.shef.dcs.oak.lodie.PlaceHolder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter.FilterPolicy;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label.LabelFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property.PropertyFinder;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/05/13
 * Time: 16:57
 */
class PropertyLexMatchHelper {
    public static void findCandidate_Properties_Plus_Labels_ForHeaders(LTable table, ObjectMatrix1D candidateMatrix,
                                                                       Map<String, Set<String>> labels,
                                                                       Map<String, String> mapOfEquivalence,
                                                                       PropertyFinder[] propertyFinders,
                                                                       LabelFinder[] labelFinders,
                                                                       List<Integer> dataColumns, FilterPolicy... policies) {
        //finding candidates for "header"
        Set<String> uniqueURIs = new HashSet<String>();

        for (int col=0; col<table.getNumHeaders(); col++) {
            if (!dataColumns.contains(col))
                continue;
            String headerText = table.getColumnHeader(col).getHeaderText();
            if (headerText.equals(PlaceHolder.TABLE_HEADER_UNKNOWN)) {
                continue;
            }
            if (TableContent2KnowledgeBaseMapper
                    .skip(table, 0, col, policies))
                continue;

            Set<String> concepts = new HashSet<String>();
            for (PropertyFinder finder : propertyFinders) {
                List<String[]> c = null;
                try {
                    c = (List<String[]>) finder.findCandidates(TableContent2KnowledgeBaseMapper.
                            cleanTableCellValue(headerText));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (c != null) {
                    for (String[] propAndEquiv : c) {
                        String prop = propAndEquiv[0];
                        String equiv = propAndEquiv[1];
                        concepts.add(prop);
                        if (equiv.length() > 0) {
                            concepts.add(equiv);
                            mapOfEquivalence.put(prop, equiv);
                            mapOfEquivalence.put(equiv, prop);
                        }

                    }
                }
            }
            candidateMatrix.set(col, concepts);
            uniqueURIs.addAll(concepts);
        }

        TableContent2KnowledgeBaseMapper.
                findLabelsForResources(labels, mapOfEquivalence, uniqueURIs, labelFinders);
    }
}
