package uk.ac.shef.dcs.sti.parser.list.validator;

import uk.ac.shef.dcs.sti.core.model.List;
import uk.ac.shef.dcs.sti.core.model.ListItem;
import uk.ac.shef.dcs.sti.parser.ContentValidator;

import java.util.Map;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 15:02
 * <p/>
 * Implements the following rules:
 * <p/>
 * - must contain at least 5 PROPER items
 * \t       an item is proper if: 1)it has some texts 2)it is not lengthy
 * \t
 * \t       an item is lengthy if: 1)over x% of values (multi-valued item) in the list item has over Y tokens
 * \t                              2)there are over X values (multi-valued item) in the item
 * - must not contain more than X% lengthy items
 * - must not be a list of numeric values (beyond X% of items)
 * - must not contain too many empty items (beyond x% of items)
 * - must not contain an item that begins with ["list of"]
 */
public class ListVaildatorLanient extends ContentValidator implements ListValidator {

    protected final static int THRESHOLD_MIN_PROPERITEMS=5;
    protected final static int THRESHOLD_MAX_TOKENS_PER_VALUEINITEM = 5; //above this value, this value in the item is "lengthy", as a result, the item is lengthy
    protected final static int THRESHOLD_MAX_VALUES_PER_ITEM = 3; //above this value, this list item is "lengthy"

    protected final static double THRESHOLD_TOOMANY_LENGTHYITEM = 0.2; //% of lengthy items allowed in a list
    protected final static double THRESHOLD_TOOMANY_NUMERICITEMS = 0.2;
    protected final static double THRESHOLD_TOOMANY_EMPTYTEMS = 0.2;

    @Override
    public boolean isValid(List list) {
        int countEmpty = 0, countLengthy = 0, numericItems = 0;

        for (ListItem li : list.getItems()) {
            String fulltext = li.getText();

            if(fulltext.toLowerCase().startsWith("list of"))
                return false;
            if (isEmptyMediaWikiString(fulltext))
                countEmpty++;
            if (isNumericContent(fulltext))
                numericItems++;
            if(li.getValuesAndURIs().size()>THRESHOLD_MAX_VALUES_PER_ITEM)
                countLengthy++;
            for(Map.Entry<String, String> e: li.getValuesAndURIs().entrySet()){
                String text = e.getKey();
                int length = text.split("\\s+").length;
                if(length>THRESHOLD_MAX_TOKENS_PER_VALUEINITEM){
                    countLengthy++;
                    break;
                }
            }

            int fulltextLength = fulltext.split("\\s+").length;
            int maxLengthAllowed = li.getValuesAndURIs().size()*THRESHOLD_MAX_TOKENS_PER_VALUEINITEM;
            if(fulltextLength>maxLengthAllowed)
                countLengthy++;
        }

        if(countEmpty>list.getItems().size()*THRESHOLD_TOOMANY_EMPTYTEMS)
            return false;
        if(numericItems>list.getItems().size()*THRESHOLD_TOOMANY_NUMERICITEMS)
            return false;
        if(countLengthy>list.getItems().size()*THRESHOLD_TOOMANY_LENGTHYITEM)
            return false;
        if(list.getItems().size()-countEmpty-countLengthy-numericItems<THRESHOLD_MIN_PROPERITEMS)
            return false;

        return true;
    }


}
