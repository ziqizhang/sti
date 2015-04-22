package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass;

import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/05/13
 * Time: 19:44
 */
public abstract class SuperConceptFinder extends CandidateFinder {

    public SuperConceptFinder(QueryCache cache) {
        super(cache);
    }

    @Override
    protected abstract List<String[]> findCandidatesInKB(String text);

    @Override
    protected abstract List<String[]> findCandidatesInCache(String text);
}
