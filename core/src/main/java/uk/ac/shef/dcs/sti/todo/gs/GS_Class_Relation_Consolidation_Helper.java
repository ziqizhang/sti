package uk.ac.shef.dcs.sti.todo.gs;

import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 */
public class GS_Class_Relation_Consolidation_Helper {
    public static void main(String[] args) throws IOException {
        List<String> sourcefolders = new ArrayList<String>();
        /*sourcefolders.add("E:\\Data\\table_annotation\\limaye_sample\\88_tables_regen\\baseline\\baseline_nm+first(no_RI)");
        sourcefolders.add("E:\\Data\\table_annotation\\limaye_sample\\88_tables_regen\\tableminer\\tableminer_journal(no_RI,nm_changed)");*/

        sourcefolders.add("E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\baseline\\baseline_sl(RI)");
        sourcefolders.add("E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\baseline\\baseline_nm+first(RI)");
        sourcefolders.add("E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\tableminer\\tm_dc_ri");


        Set<String> files = new HashSet<String>();
        for(String source: sourcefolders){
            for(File f: new File(source).listFiles()){
                if(f.toString().contains("header.keys")||f.toString().contains("relation.keys"))
                    files.add(f.getName());
            }
        }



        for(String f: files){
            Map<String, List<String>> candidates = new HashMap<String, List<String>>();
            PrintWriter p = new PrintWriter("E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ri/"+f);
         /*   if(!f.contains("Aeropuertos"))
                continue;
*/
            for(String source: sourcefolders){
                File found=null;
                for(File sf: new File(source).listFiles()){
                    if(sf.getName().equals(f)){
                        found=sf;
                        break;
                    }
                }
                if(found!=null){
                    update(found, candidates);
                }
            }

            List<String> keys = new ArrayList<String>(candidates.keySet());
            Collections.sort(keys);
            for(String k: keys){
                List<String> cands = candidates.get(k);
                Collections.sort(cands);
                String line = "";
                for(String c: cands){
                    line=line+"|"+c;
                }
                line=line.trim();
                if(line.startsWith("|"))
                    line=line.substring(1);
                p.println(k+"="+line);
            }
            p.close();
        }
    }

    private static void update(File found, Map<String, List<String>> candidates) throws IOException {
        List<String> lines = FileUtils.readList(found.toString(), false);
        for(String l: lines){
            String[] parts = l.split("=",2);
            String key = parts[0].trim();
            List<String> cands = candidates.get(key);
            cands=cands==null?new ArrayList<String>():cands;

            String values = parts[1].trim();
            String[] value_entries = values.split("[\\|=\t+]");
            for(String v: value_entries){
                v=v.trim();
                if(v.length()>0&&!cands.contains(v))
                    cands.add(v);
            }

            candidates.put(key, cands);
        }
    }
}
