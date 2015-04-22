package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.LTableColumn2ColumnRelation;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinder;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/05/13
 * Time: 18:26
 */
class RelationBetweenInstanceHelper {
    public static List<LTableColumn2ColumnRelation> findRelationsBetweenDataCells(
            Logger log, ObjectMatrix2D candidates,
            LTable table,
            List<Integer> dataColumns,
            int mainSubjectColumn,
            RelationFinder... relationFinder) {
        Map<String, Set<String>> inMemCache = new HashMap<String, Set<String>>();

        List<LTableColumn2ColumnRelation> result = new ArrayList<LTableColumn2ColumnRelation>();
        for (int c : dataColumns) {
            if (c == mainSubjectColumn)
                continue;

            LTableColumn2ColumnRelation tccr = new LTableColumn2ColumnRelation(mainSubjectColumn, c);

            for (int r = 1; r < table.getNumRows(); r++) {
                Object main = candidates.get(r, mainSubjectColumn);
                Object other = candidates.get(r, c);
                if (main == null || other == null)
                    continue;
                //each cell may have multiple candidates, retrieve them all
                Set<String> mainCandidates = (HashSet<String>) main;
                Set<String> otherCandidates = (HashSet<String>) other;
                RelationBetweenClassesHelper.
                        findAndCacheRelations(mainCandidates, otherCandidates, inMemCache, relationFinder); //cache relations. do the query

                log.info("\t\\r=" + r + ", mainC=" + mainSubjectColumn + ", c=" + c + "; size mainC=" + mainCandidates.size() + ", size c=" + otherCandidates.size());
                //retrieved the cached relations, create objects. (no query)
                RelationBetweenClassesHelper.
                        findRelationsFromCache(mainCandidates, otherCandidates, c, r, mainSubjectColumn, tccr, inMemCache);
            }//end for ecah row
            result.add(tccr);
        } //end for ecah datacolumn
        return result;
    }
}
