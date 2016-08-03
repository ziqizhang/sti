package uk.ac.shef.dcs.kbsearch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Information retrieved from a KB may contain those that are too generic or high level to be useful. E.g., in DBpedia, class 'Thing'
 * may never be a sensible class to annotate a table column.
 *
 * KBSearchResultFilter is responsible for filtering such information. This can be class, relation, or entity, depending on the actual
 * implementing classes.
 */
public abstract class KBSearchResultFilter {
    protected Map<String, Set<String>> stoplists = new HashMap<>();
    protected static final String LABEL_INVALID_CLAZZ="!invalid_clazz";
    protected static final String LABEL_INVALID_ATTRIBUTE="!invalid_attribute";

    public KBSearchResultFilter(String stoplistsFile) throws IOException {
        loadStoplists(stoplistsFile);
    }

    /**
     * An external file defining class/relation/entities to be filtered. the file must correspond to certain format.
     * See 'resources/kbstoplist.txt' for explanation
     *
     * It loads all given classes/relations/entities to a Map, which contains as the key for the set of stop wrods the label obtained from the file (from the line starting with !).
     * So there are different stop words for attributes and classes, e.g.
     * @param stoplistsFile
     * @throws IOException
     */
    protected void loadStoplists(String stoplistsFile) throws IOException {
        LineIterator it = FileUtils.lineIterator(new File(stoplistsFile));
        String label="";
        Set<String> elements = new HashSet<>();
        while(it.hasNext()){
            String line=it.nextLine().trim();

            if(line.length()<1 || line.startsWith("#"))
                continue;

            if(line.startsWith("!")){
                if(elements.size()>0)
                    stoplists.put(label, elements);

                elements= new HashSet<>();
                label=line;
            }else{
                elements.add(line);
            }
        }
        if(elements.size()!=0)
            stoplists.put(label, elements);
    }

    public List<Clazz> filterClazz(Collection<Clazz> types) {
        List<Clazz> r = new ArrayList<>();
        for (Clazz t : types) {
            if (isValidClazz(t)) {
                r.add(t);
            }
        }
        return r;
    }

    /**
     * Checks whether the class is valid class. Class is valid if it is not blacklisted.
     * @param c
     * @return true if the class is valid
     */
    protected boolean isValidClazz(Clazz c) {

        Set<String> stop = stoplists.get(LABEL_INVALID_CLAZZ);
        if (stop == null)
            return true;

        for (String s : stop) {
            if (c.getId().contains(s) || c.getLabel().equalsIgnoreCase(s))
                return false;
        }
        return true;

    }

    /**
     * Creates new list of attributes, which contains only attributes which are valid.
     * @param facts
     * @return
     */
    public List<Attribute> filterAttribute(Collection<Attribute> facts) {
        List<Attribute> filteredList = new ArrayList<>();
        for (Attribute t : facts) {
            if(isValidAttribute(t)) {
                filteredList.add(t);
            }
        }
        return filteredList;
    }


    /**
     * Checks whether the attribute is valid attribute. Attribute is valid if it is not blacklisted.
     * @param attribute
     * @return true if the attribute is valid
     */
    protected boolean isValidAttribute(Attribute attribute) {

        Set<String> stop = stoplists.get(LABEL_INVALID_ATTRIBUTE);
        String relation = attribute.getRelationURI();
        if (stop != null) {
            for (String s : stop) {
                if (relation.startsWith(s))
                    return false;
            }
        }
        return true;
    }

}
