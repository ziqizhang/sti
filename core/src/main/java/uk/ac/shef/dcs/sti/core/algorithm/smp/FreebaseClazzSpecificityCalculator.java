package uk.ac.shef.dcs.sti.core.algorithm.smp;

import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.STIConstantProperty;

/**
 *
 */
public class FreebaseClazzSpecificityCalculator implements ClazzSpecificityCalculator {

    private KBSearch kbSearch;

    public FreebaseClazzSpecificityCalculator(KBSearch kbSearch) {
        this.kbSearch = kbSearch;
    }

    @Override
    public double compute(String clazzURI) {
        double conceptGranularity = 0;
        try {
            conceptGranularity = kbSearch.findGranularityOfClazz(clazzURI);
        } catch (KBSearchException e) {
            return 0.0;
        }
        if (conceptGranularity < 0)
            return 0.0;
        return 1 - Math.sqrt(conceptGranularity / STIConstantProperty.FREEBASE_TOTAL_TOPICS);

    }
}
