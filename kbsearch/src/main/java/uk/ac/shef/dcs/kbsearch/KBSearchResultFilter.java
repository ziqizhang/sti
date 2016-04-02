package uk.ac.shef.dcs.kbsearch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by - on 17/03/2016.
 */
public abstract class KBSearchResultFilter {
    protected Map<String, Set<String>> stoplists = new HashMap<>();
    protected static final String LABEL_INVALID_CLAZZ="!invalid_clazz";
    protected static final String LABEL_INVALID_ATTRIBUTE="!invalid_attribute";

    public KBSearchResultFilter(String stoplistsFile) throws IOException {
        loadStoplists(stoplistsFile);
    }

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

    public abstract List<Clazz> filterClazz(Collection<Clazz> types);

    public abstract boolean isValidClazz(Clazz c);
    /**
     * remove any attributes that contain an invalid relation
     * @param facts
     * @return
     */
    public abstract List<Attribute> filterAttribute(Collection<Attribute> facts);


    public abstract boolean isValidAttribute(Attribute attribute);
}
