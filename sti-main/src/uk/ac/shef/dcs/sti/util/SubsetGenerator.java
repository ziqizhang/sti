package uk.ac.shef.dcs.sti.util;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by zqz on 29/04/2015.
 */
public class SubsetGenerator {

    public static List<String> generateSubsets(Set<Integer> words){
        Set<Set<Integer>> subsets = Sets.powerSet(words);
        List<String> result = new ArrayList<>();
        for(Set<Integer> sub: subsets){
            List<Integer> ordered = new ArrayList<>(sub);
            Collections.sort(ordered);
            String string = "";
            for(Integer a: ordered){
                string+=a+" ";
            }
            result.add(string.trim());
        }
        result.remove("");
        return result;
    }
}
