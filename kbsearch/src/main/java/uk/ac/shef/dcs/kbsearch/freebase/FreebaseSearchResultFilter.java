package uk.ac.shef.dcs.kbsearch.freebase;

import uk.ac.shef.dcs.kbsearch.KBSearchResultFilter;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;

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

    public boolean isValidClazz(Clazz c) {
        /*if (type.startsWith("/user/") ||
                type.startsWith("/common/")||
                //type.equals("/common/image") ||
                *//*type.equals("/type/content") ||
                type.startsWith("/type/type/domain")||*//*
                type.startsWith("/type/")||
                type.endsWith("topic") || type.startsWith("/pipeline/") ||
                type.endsWith("skos_concept") ||
                type.endsWith("_instance") ||
                type.startsWith("/base/type_ontology")
                ||label.equalsIgnoreCase("topic")||label.equalsIgnoreCase("thing")||label.equalsIgnoreCase("concept")
                ||label.equalsIgnoreCase("things")||label.equalsIgnoreCase("entity"))
            return true;
        return false;*/
        Set<String> stop = stoplists.get(LABEL_INVALID_CLAZZ);
        if (stop == null)
            return true;

        for (String s : stop) {
            if (c.getId().contains(s) || c.getLabel().equalsIgnoreCase(s))
                return false;
        }
        return true;

    }

    public List<Clazz> filterClazz(Collection<Clazz> types) {
        List<Clazz> r = new ArrayList<>();
        for (Clazz t : types) {
            if (!isValidClazz(t)) continue;
            r.add(t);
        }
        return r;
    }

    public List<Attribute> filterAttribute(Collection<Attribute> facts) {
        List<Attribute> r = new ArrayList<>();
        for (Attribute t : facts) {
            if(!isValidAttribute(t)) continue;
            r.add(t);
        }
        return r;
    }

    public boolean isValidAttribute(Attribute attribute) {
        Set<String> stop = stoplists.get(LABEL_INVALID_ATTRIBUTE);
        String relation =attribute.getRelation();
        if (stop != null) {
            relation = attribute.getRelation();
            for (String s : stop) {
                if (relation.startsWith(s))
                    return false;
            }

        }
        return !relation.equalsIgnoreCase("id");
    }
}
