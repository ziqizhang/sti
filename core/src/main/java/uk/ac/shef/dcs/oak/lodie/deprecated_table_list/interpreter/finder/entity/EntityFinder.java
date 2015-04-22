package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity;

import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.List;
import java.util.Map;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/05/13
 * Time: 17:25
 */
public abstract class EntityFinder extends CandidateFinder {
    public EntityFinder(QueryCache cache) {
        super(cache);
    }

    @Override
    protected abstract List<ObjObj<String, Map<String, String>>> findCandidatesInKB(String text);

    @Override
    protected abstract List<ObjObj<String, Map<String, String>>> findCandidatesInCache(String text);
}
