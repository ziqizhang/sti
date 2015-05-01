package uk.ac.shef.dcs.oak.sti.util;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by zqz on 29/04/2015.
 */
public class SubsetGenerator {
    public static List<String> generateSubsets(Set<String> words){
        Set<Set<String>> subsets = Sets.powerSet(words);
        List<String> result = new ArrayList<String>();
        for(Set<String> sub: subsets){
            List<String> ordered = new ArrayList<String>(sub);
            Collections.sort(ordered);
            String string = "";
            for(String a: ordered){
                string+=a+" ";
            }
            result.add(string.trim());
        }
        result.remove("");
        return result;
    }
}
