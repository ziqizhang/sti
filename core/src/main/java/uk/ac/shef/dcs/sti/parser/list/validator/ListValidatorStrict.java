package uk.ac.shef.dcs.sti.parser.list.validator;

import uk.ac.shef.dcs.sti.core.model.List;
import uk.ac.shef.dcs.sti.core.model.ListItem;
import uk.ac.shef.dcs.sti.parser.ContentValidator;

import java.util.Map;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 11/10/12
 * Time: 12:53
 * <p/>
 * <p/>
 * Implements the following rules:
 * <p/>
 * - must contain at least 5 PROPER items
 * - must not contain lengthy items (multi-valued item not allowed)
 * - must not contain numeric items
 * - must not contain  empty items
 * - must not contain an item that begins with ["list of"]
 * - must contain at least 60% and a minimum of 5 items-with-uris (gold standard)
 */
public class ListValidatorStrict extends ContentValidator implements ListValidator {

    protected final double THRESHOLD_FRACTION_ITEMSWITHURIS = 0.6;


    public ListValidatorStrict() {
    }


    @Override
    public boolean isValid(List list) {
        if (list.getItems().size() < ListVaildatorLanient.THRESHOLD_MIN_PROPERITEMS)
            return false;

        int countGSWithURIs = 0;
        for (ListItem li : list.getItems()) {
            if (li.getValuesAndURIs().size() > 1)
                return false;
            if (li.getValuesAndURIs().size() > 0) {
                String uri = li.getValuesAndURIs().values().iterator().next();
                if (isWikiInternalLink(uri)) {
                    countGSWithURIs++;
                }
            }

            String fulltext = li.getText();
            int fulltextLength = fulltext.split("\\s+").length;
            if (fulltextLength > ListVaildatorLanient.THRESHOLD_MAX_TOKENS_PER_VALUEINITEM)
                return false;

            if (fulltext.toLowerCase().startsWith("list of"))
                return false;
            if (isEmptyMediaWikiString(fulltext))
                return false;
            if (isNumericContent(fulltext))
                return false;
            for (Map.Entry<String, String> e : li.getValuesAndURIs().entrySet()) {
                String text = e.getKey();
                int length = text.split("\\s+").length;
                if (length > ListVaildatorLanient.THRESHOLD_MAX_TOKENS_PER_VALUEINITEM) {
                    return false;
                }
            }
        }

        if (countGSWithURIs < 5 || countGSWithURIs < list.getItems().size() * THRESHOLD_FRACTION_ITEMSWITHURIS)
            return false;

        return true;
    }
}
