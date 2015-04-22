package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property;

import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/05/13
 * Time: 19:59
 */
public abstract class PropertyFinder extends CandidateFinder {
    public PropertyFinder(QueryCache cache) {
        super(cache);
    }

    @Override
    protected abstract List<String[]> findCandidatesInKB(String text);

    @Override
    protected abstract List<String[]> findCandidatesInCache(String text);
}
