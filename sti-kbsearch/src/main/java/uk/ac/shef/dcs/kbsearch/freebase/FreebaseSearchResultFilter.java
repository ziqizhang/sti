package uk.ac.shef.dcs.kbsearch.freebase;

import uk.ac.shef.dcs.kbsearch.KBSearchResultFilter;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 16/02/14
 * Time: 14:25
 * To change this template use File | Settings | File Templates.
 */
public class FreebaseSearchResultFilter extends KBSearchResultFilter {

    public FreebaseSearchResultFilter(String stoplistsFile) throws IOException {
        super(stoplistsFile);
    }


    /**
     * Provides
     * @param attribute
     * @return
     */
    @Override
    public boolean isValidAttribute(Attribute attribute) {
        //here is a list of 'pass' relations that should always be kept as the stoplist can be over-generalising
        String rel = attribute.getRelationURI();
        if(rel.startsWith(FreebaseEnum.TYPE_COMMON_TOPIC.getString())||rel.equals(FreebaseEnum.RELATION_HASTYPE.getString())
                ||rel.equals(FreebaseEnum.RELATION_HASNAME.getString()))
            return true;

        Set<String> stop = stoplists.get(LABEL_INVALID_ATTRIBUTE);
        String relation =attribute.getRelationURI();
        if (stop != null) {
            relation = attribute.getRelationURI();
            for (String s : stop) {
                if (relation.startsWith(s))
                    return false;
            }

        }
        return !relation.equalsIgnoreCase("id");
    }
}
