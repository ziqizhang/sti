package uk.ac.shef.dcs.oak.sti.table.interpreter.io;

import uk.ac.shef.dcs.oak.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class LTableAnnotationKeyFileReader_old {
    public static Map<Integer, List<List<String>>> readHeaderAnnotation(String keyfile) throws IOException {
        Map<Integer, List<List<String>>> result = new HashMap<Integer, List<List<String>>>();

        List<String> lines = FileUtils.readList(keyfile, false);
        for (String l : lines) {
            if(l.endsWith("___NE")){
                l=l.substring(0, l.indexOf("___NE")).trim();
            }
            String[] col_annotations = l.split("=", 2);
            if (col_annotations.length < 2)
                continue;

            int col = Integer.valueOf(col_annotations[0]);
            String annotations = col_annotations[1];
            List<List<String>> anns = new ArrayList<List<String>>();
            for (String a : annotations.split("\t")) {
                a = a.trim();
                if (a.length() > 0) {
                    if (a.indexOf("=") == -1){
                        List<String> list = new ArrayList<String>();
                        list.add(a);
                        anns.add(list);
                    }
                    else {
                        List<String> equal_weights = new ArrayList<String>();
                        for (String e : a.split("=")) {
                            e = e.trim();
                            if (e.length() > 0)
                                equal_weights.add(e);
                        }
                        anns.add(equal_weights);
                    }
                }
            }
            result.put(col, anns);
        }
        return result;
    }


    public static Map<int[], List<List<String>>> readColumnBinaryRelationAnnotation(String keyfile) throws IOException {
        Map<int[], List<List<String>>> result = new HashMap<int[], List<List<String>>>();

        List<String> lines = FileUtils.readList(keyfile, false);
        for (String l : lines) {
            String[] pos_annotations = l.split("=", 2);
            if (pos_annotations.length < 2)
                continue;

            String pos = pos_annotations[0];
            String[] pos_ = pos.split(",");
            if (pos_.length < 2) continue;
            int mainCol = Integer.valueOf(pos_[0].trim());
            int otherCol = Integer.valueOf(pos_[1].trim());

            String annotations = pos_annotations[1];
            List<List<String>> anns = new ArrayList<List<String>>();
            for (String a : annotations.split("\\|")) {
                a = a.trim();
                if (a.length() > 0) {
                    if (a.indexOf("=") == -1){
                        List<String> list = new ArrayList<String>();
                        list.add(a);
                        anns.add(list);
                    }
                    else {
                        List<String> equal_weights = new ArrayList<String>();
                        for (String e : a.split("=")) {
                            e = e.trim();
                            if (e.length() > 0)
                                equal_weights.add(e);
                        }
                        anns.add(equal_weights);
                    }
                }
            }
            result.put(new int[]{mainCol, otherCol}, anns);
        }
        return result;
    }

    public static Map<int[], List<List<String>>> readCellAnnotation(String keyfile) throws IOException {
        Map<int[], List<List<String>>> result = new HashMap<int[], List<List<String>>>();

        List<String> lines = FileUtils.readList(keyfile, false);
        for (String l : lines) {
            String[] pos_annotations = l.split("=",2);
            if (pos_annotations.length < 2)
                continue;

            String pos = pos_annotations[0];
            String[] pos_ = pos.split(",");
            if (pos_.length < 2) continue;
            int row = Integer.valueOf(pos_[0].trim());
            int col = Integer.valueOf(pos_[1].trim());

            String annotations = pos_annotations[1];
            List<List<String>> anns = new ArrayList<List<String>>();
            for (String a : annotations.split("\\|")) {
                a = a.trim();
                if (a.indexOf("=") == -1){
                    List<String> list = new ArrayList<String>();
                    list.add(a);
                    anns.add(list);
                }
                else {
                    List<String> equal_weights = new ArrayList<String>();
                    for (String e : a.split("=")) {
                        e = e.trim();
                        if (e.length() > 0)
                            equal_weights.add(e);
                    }
                    anns.add(equal_weights);
                }
            }
            result.put(new int[]{row, col}, anns);
        }
        return result;
    }
}
