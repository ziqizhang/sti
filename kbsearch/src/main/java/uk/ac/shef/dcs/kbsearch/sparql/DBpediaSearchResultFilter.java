package uk.ac.shef.dcs.kbsearch.sparql;

import uk.ac.shef.dcs.kbsearch.KBSearchResultFilter;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseEnum;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * TODO
 */
public class DBpediaSearchResultFilter extends KBSearchResultFilter {
    public DBpediaSearchResultFilter(String property) throws IOException {
        super(property);
    }

    @Override
    public List<Clazz> filterClazz(Collection<Clazz> types) {
        List<Clazz> r = new ArrayList<>();
        /*if(types==null)
            System.out.println();*/
        for (Clazz t : types) {
            if (!isValidClazz(t)) continue;
            r.add(t);
        }
        return r;
    }

    @Override
    public boolean isValidClazz(Clazz c) {
        Set<String> stop = stoplists.get(LABEL_INVALID_CLAZZ);
        if (stop == null)
            return true;

        for (String s : stop) {
            if (c.getId().contains(s) || c.getLabel().equalsIgnoreCase(s))
                return false;
        }
        return true;
    }

    @Override
    public List<Attribute> filterAttribute(Collection<Attribute> facts) {
        List<Attribute> r = new ArrayList<>();
        for (Attribute t : facts) {
            if(!isValidAttribute(t)) continue;
            r.add(t);
        }
        return r;
    }

    @Override
    public boolean isValidAttribute(Attribute attribute) {
        Set<String> stop = stoplists.get(LABEL_INVALID_ATTRIBUTE);
        String relation =attribute.getRelationURI();
        if (stop != null) {
            for (String s : stop) {
                if (relation.startsWith(s))
                    return false;
            }

        }
        return true;
    }
}
