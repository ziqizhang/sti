package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter.FilterPolicy;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label.LabelFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass.SuperConceptFinder;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 29/04/13
 * Time: 21:24
 */
class SuperclassOfInstanceInCellHelper {

    /*
   returns an OjbjectMatrix2D, where each column is a column in the table. each cell contains a Map<String, List>, where key=candidate_uri_for_this_cell,
   and List = superclasses for this uri

   if we do not want to get labels, simply provide a "null" value for the param "labels"
   //todo: some uris have "superclass" that is exactly the same as itself
    */
    public static ObjectMatrix2D
    findCandidate_Concept_and_Labels_ForHeadersBySuperclassesOfDataValues(Logger log,
                                                                          LTable table,
                                                                          ObjectMatrix2D cellWithCandidates,
                                                                          List<Integer> dataColumns,
                                                                          Map<String, Set<String>> labels,
                                                                          Map<String, String> mapOfEquivalence,
                                                                          SuperConceptFinder[] superclassFinders,
                                                                          LabelFinder[] labelFinder_forHeaderConcepts,
                                                                          FilterPolicy... policies) {
        ObjectMatrix2D headerCandidates = new SparseObjectMatrix2D(cellWithCandidates.rows(), cellWithCandidates.columns());
        Map<String, Set<String>> inMemCache = new HashMap<String, Set<String>>();
        Map<String, Integer> superclasses2CountsForThisTable = new HashMap<String, Integer>();  //lets count frequency of superclasses
        //there will be too many to process. lets delete those have freq=1


        log.info("\ttable of size:r=" + cellWithCandidates.rows() + ",c=" + cellWithCandidates.columns());
        for (int c : dataColumns) {
            if (TableContent2KnowledgeBaseMapper.skip(table, -1, c, policies))
                continue;

            for (int r = 1; r < cellWithCandidates.rows(); r++) {
                log.info("\t\\r=" + r + " c=" + c);
                Object cell = cellWithCandidates.get(r, c);
                if (cell == null)
                    continue;
                Set<String> urisInThisCell = (Set<String>) cell;
                Map<String, Set<String>> superclassesMapOfThisCell = new HashMap<String, Set<String>>();

                for (String uri : urisInThisCell) {
                    Set<String> superclassesOfUri = inMemCache.get(uri);
                    if (superclassesOfUri == null) {
                        superclassesOfUri = new HashSet<String>();
                        for (SuperConceptFinder finder : superclassFinders) {
                            try {
                                List<String[]> rs = (List<String[]>) finder.findCandidates(uri);
                                if (rs == null || rs.size() == 0)
                                    continue;

                                //process each result, split by | to get class and equiv
                                for (String[] conceptAndEquiv : rs) {
                                    String concept = conceptAndEquiv[0];
                                    String equiv = conceptAndEquiv[1];
                                    superclassesOfUri.add(concept);
                                    if (equiv.length() > 0) {
                                        superclassesOfUri.add(equiv);
                                        mapOfEquivalence.put(concept, equiv);
                                        mapOfEquivalence.put(equiv, concept);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    superclassesMapOfThisCell.put(uri, superclassesOfUri);
                    inMemCache.put(uri, superclassesOfUri);
                } //end for each uri in this cell
                headerCandidates.set(r, c, superclassesMapOfThisCell);

                //update frequency
                Set<String> uniqueSuperclassesOfThisCell = new HashSet<String>();
                for (Set<String> sups : superclassesMapOfThisCell.values())
                    uniqueSuperclassesOfThisCell.addAll(sups);
                for (String a : uniqueSuperclassesOfThisCell) {
                    Integer freq = superclasses2CountsForThisTable.get(a);
                    freq = freq == null ? 0 : freq;
                    freq = freq + 1;
                    superclasses2CountsForThisTable.put(a, freq);
                }
            }
        }

        //completed searching and counting. Now discard infrequent superclasses
        Set<String> superclassesOfThisTalbe = new HashSet<String>();
        for (int c = 0; c < headerCandidates.columns(); c++) {
            for (int r = 1; r < headerCandidates.rows(); r++) {
                Object cell = headerCandidates.get(r, c);
                if (cell == null)
                    continue;
                Map<String, Set<String>> superclassesOfThisCell = (Map<String, Set<String>>) cell;

                for (Set<String> supers : superclassesOfThisCell.values()) {
                    Iterator<String> it = supers.iterator();
                    while (it.hasNext()) {
                        String sup = it.next();
                        Integer freq = superclasses2CountsForThisTable.get(sup);
                        if (freq == null || freq < 2)
                            it.remove();
                        else
                            superclassesOfThisTalbe.add(sup);
                    }
                }
            }
        }
        log.info("\t\\ before deleting infrequent: " + superclasses2CountsForThisTable.size() + ", after:" + superclassesOfThisTalbe.size());

        TableContent2KnowledgeBaseMapper.
                findLabelsForResources(labels, mapOfEquivalence, superclassesOfThisTalbe, labelFinder_forHeaderConcepts);
        return headerCandidates;
    }
}
