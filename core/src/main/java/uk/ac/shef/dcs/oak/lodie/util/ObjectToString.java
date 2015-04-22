package uk.ac.shef.dcs.oak.lodie.util;

import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 10/03/14
 * Time: 21:19
 * To change this template use File | Settings | File Templates.
 */
public class ObjectToString {

    public static String string_array_list_toString(Object o) {
        try {
            List<String[]> list = (List<String[]>) o;
            String content = "";
            for (String[] a : list) {
                for (int i = 0; i < a.length; i++) {
                    content = content + a[i] + " ";
                }
            }
            return content.trim();
        } catch (Exception e) {
        }
        return "";
    }

    public static String entity_candidate_list_toString(Object o) {
        try {
            List<EntityCandidate> ec = (List<EntityCandidate>) o;
            String content = "";
            for (EntityCandidate e : ec) {
                content = content + e.getId() + " " + e.getName() + " "
                        + string_array_list_toString(e.getFacts()) + " " + string_array_list_toString(e.getTypes());

            }
            return content;
        } catch (Exception e) {
        }
        return "";
    }
}
