package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter.FilterPolicy;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity.EntityFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label.LabelFinder;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/05/13
 * Time: 18:23
 */
//todo: matching currently only handles single values in a cell. What if the cell has multiple string values to be matched?
class InstanceLexMatchHelper {

    public static void findCandidatesForDataCells(
            Logger log,
            LTable table,
            List<Integer> dataColumns,
            ObjectMatrix2D candidates,
            Map<String, Set<String>> all_labels,
            Map<String, String> mapOfEquivalence,
            EntityFinder[] cellCandidateFinder,
            LabelFinder[] labelFinder,
            FilterPolicy... policies) {
        log.info("\ttable of size:r=" + table.getNumRows() + ",c=" + table.getNumCols());
        Map<String, List<ObjObj<String, Map<String, String>>>> inMemCache =
                new HashMap<String, List<ObjObj<String, Map<String, String>>>>();
        //find candidates for data cells
        Set<String> allInstances = new HashSet<String>();
        for (int c : dataColumns) {
            if (TableContent2KnowledgeBaseMapper.skip(table, -1, c, policies))
                continue;
            //dataColumns.add(c);

            for (int r = 1; r < table.getNumRows(); r++) {
                log.info("\t\\r=" + r + " c=" + c);
                //LTableContentCell tc = table.getTableCell(r, c);
                LTableContentCell contentCellText = table.getContentCell(r,c);

                Set<String> concepts = new HashSet<String>();
                for (EntityFinder finder : cellCandidateFinder) {
                    String text = TableContent2KnowledgeBaseMapper.cleanTableCellValue(contentCellText.getText());
                    List<ObjObj<String, Map<String, String>>> cands =
                            inMemCache.get(text);
                    if (cands == null) {
                        try {
                            cands = (List<ObjObj<String, Map<String, String>>>)
                                    finder.findCandidates(TableContent2KnowledgeBaseMapper.cleanTableCellValue(contentCellText.getText()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (cands != null) {
                        for (ObjObj<String, Map<String, String>> owo : cands) {
                            concepts.add(owo.getMainObject());
                            Map<String, String> superConceptAndEquiv = owo.getOtherObject();
                            for(Map.Entry<String, String> e: superConceptAndEquiv.entrySet()){
                                if(!e.getValue().equals(""))
                                    mapOfEquivalence.put(e.getKey(), e.getValue());
                            }
                        }
                        inMemCache.put(text, cands);
                    } else {
                        inMemCache.put(text, new ArrayList<ObjObj<String, Map<String, String>>>());
                    }
                }
                candidates.set(r, c, concepts);
                allInstances.addAll(concepts);
            }
        }

        TableContent2KnowledgeBaseMapper.findLabelsForResources(all_labels, mapOfEquivalence, allInstances, labelFinder);

    }

}
