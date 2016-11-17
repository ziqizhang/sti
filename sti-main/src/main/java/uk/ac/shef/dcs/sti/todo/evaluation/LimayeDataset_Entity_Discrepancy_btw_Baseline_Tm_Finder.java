package uk.ac.shef.dcs.sti.todo.evaluation;

import info.aduna.io.FileUtil;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbproxy.freebase.FreebaseSearch;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.todo.TAnnotationKeyFileReader;
import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * find discrepancy between annotations created by different systems
 */
public class LimayeDataset_Entity_Discrepancy_btw_Baseline_Tm_Finder {

    public static void main(String[] args) throws IOException {
        String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";

        //todo: this will not work
        /*File configFile = new File(cacheFolder + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolder,
                configFile);*/
        EmbeddedSolrServer server = null; //new EmbeddedSolrServer(container, "collection1");
        //object to fetch things from KB
        String freebaseProperties = "D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties";
        //todo: this will not work
        FreebaseSearch freebaseMatcher = null;//new FreebaseSearch(freebaseProperties, true, server, null,null);

        find_discrepancies_between(
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\tableminer_aclshort_no_ref_ent",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\baseline_aclshort",
                //"E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_tm-gs_discrepancy",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\tableminer_baseline(aclshort)_entity_discrepancy_(tm_no_ent_ref)",
                freebaseMatcher);
        server.close();
        System.exit(0);


       /* update_discrepancy_data_using_previous_discrepancy_analysis(
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\tableminer_baseline(aclshort)_entity_discrepancy",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\tableminer_baseline(aclshort)_entity_discrepancy_(tm_no_ent_ref)",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\_tmp_"
        );
        System.exit(0);*/


        calculate_different_accuracy_between(
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\tableminer_baseline(aclshort)_entity_discrepancy_(tm_no_ent_ref)",
                true);
        System.exit(0);


    }

    public static void update_discrepancy_data_using_previous_discrepancy_analysis(
            String prev_discrepancy_folder,
            String current_discrepancy_folder,
            String outfolder
    ) throws IOException {
        for (File f : new File(current_discrepancy_folder).listFiles()) {
            String name = f.getName();

            File prev_discrepancy_file = new File(prev_discrepancy_folder + "/" + name);
            if (!prev_discrepancy_file.exists()) {
                FileUtil.copyFile(f, new File(outfolder + "/" + name));
                System.out.println("new file:" + f);
                continue;
            }

            List<String> prev_discrepancies = FileUtils.readList(prev_discrepancy_file.toString(), false);
            List<String> current_discrepancies = FileUtils.readList(f.toString(), false);

            Map<String, String[]> prev_discrepancy_objects = new HashMap<String, String[]>();
            for (String l : prev_discrepancies) {
                String[] first_split = l.split("=", 2);
                String position = first_split[0].trim();
                String[] second_split = first_split[1].trim().split("\t\t");
                prev_discrepancy_objects.put(position,
                        new String[]{second_split[0].trim(), second_split[1].trim()});
            }
            Map<String, String[]> current_discrepancy_objects = new HashMap<String, String[]>();
            for (String l : current_discrepancies) {
                String[] first_split = l.split("=", 2);
                String position = first_split[0].trim();
                String[] second_split = first_split[1].trim().split("\t\t");
                current_discrepancy_objects.put(position,
                        new String[]{second_split[0].trim(), second_split[1].trim()});
            }


            List<String> outLines = new ArrayList<String>();
            for (Map.Entry<String, String[]> current_entry : current_discrepancy_objects.entrySet()) {
                String[] prev_resolved = prev_discrepancy_objects.get(current_entry.getKey());
                String line = "";
                if (prev_resolved == null) {
                    line = current_entry.getKey() + "=\t" + "-" + current_entry.getValue()[0] + "\t\t" + current_entry.getValue()[1];
                } else {
                    String[] current_to_resolve = current_entry.getValue();
                    if (prev_resolved[0].endsWith(current_to_resolve[0]) && prev_resolved[1].endsWith(current_to_resolve[1])) {
                        line = current_entry.getKey() + "=\t" + prev_resolved[0] + "\t\t" + prev_resolved[1];
                    } else {
                        if (prev_resolved[0].startsWith("x") || prev_resolved[1].startsWith("x"))
                            line = current_entry.getKey() + "=\t" + prev_resolved[0] + "\t\t" + prev_resolved[1];
                        else
                            line = current_entry.getKey() + "=\t-" + current_entry.getValue()[0] + "\t\t" + current_entry.getValue()[1];
                    }
                }

                if (line.length() > 0)
                    outLines.add(line);
                else
                    System.err.println("error");
            }

            Collections.sort(outLines);
            PrintWriter p = new PrintWriter(outfolder + "/" + name);
            for (String l : outLines)
                p.println(l);
            p.close();
        }
    }

    public static void calculate_different_accuracy_between(String analysis_file_folder, boolean either_or) throws IOException {
        int total = 0, total_tableminer = 0, total_baseline = 0, total_all_wrong = 0;
        for (File f : new File(analysis_file_folder).listFiles()) {
            System.out.println(f);
            List<String> all = FileUtils.readList(f.toString(), false);
            if(!either_or)
                total += all.size();
            for (String line : all) {
                if (line.startsWith("x")) {
                    System.out.println(line);
                    continue;
                }
                String[] parts = line.split("=", 2)[1].trim().split("\t+");
                if (parts.length != 2)
                    System.err.println("Strange line");
                if (parts[0].startsWith("xtm") || parts[1].startsWith("xbs")) {
                    if(either_or){
                        total++;
                    }
                    if (parts[0].startsWith("xtm")) {
                        total_tableminer++;
                        //System.out.println(line);
                    } else {
                        total_baseline++;
                        //System.out.println(line);
                    }
                } else {
                    System.err.println("\t\terror-" + line);
                    total_all_wrong++;
                }

            }
            System.out.println(total + "," + total_tableminer + "," + total_baseline + "," + total_all_wrong);
        }
        System.out.println(total + ", tm=" + total_tableminer + ", bs=" + total_baseline + ", all_wrong=" + total_all_wrong);
    }

    public static void find_discrepancies_between(
            String inFolder_tableminer,
            String inFolder_baseline,
            String outFolder,
            FreebaseSearch searcher) throws IOException {
        for (File tableminer_entity_file : new File(inFolder_tableminer).listFiles()) {
            Map<int[], List<List<String>>> tableminer_cells = null, baseline_cells = null;

            Set<String> processed = new HashSet<String>();
            String filename = tableminer_entity_file.getName();
            /* if(filename.toLowerCase().contains("9587"))
            System.out.println();*/
            if (filename.endsWith(".htm") || filename.endsWith(".html")) {
                if (!filename.contains(".attributes") && !filename.contains(".keys") && !processed.contains(filename)) {
                    String tableminer_entity_gs_file = tableminer_entity_file.toString() + ".cell.keys";
                    String baseline_entity_gs_file = inFolder_baseline + "/" + tableminer_entity_file.getName() + ".cell.keys";
                    System.out.println(tableminer_entity_file);

                    tableminer_cells = TAnnotationKeyFileReader.readCellAnnotation(tableminer_entity_gs_file);
                    baseline_cells = TAnnotationKeyFileReader.readCellAnnotation(baseline_entity_gs_file);
                    List<int[]> uniqueKeys = collect_unique_keys(tableminer_cells, baseline_cells);
                    Collections.sort(uniqueKeys, new Comparator<int[]>() {
                        @Override
                        public int compare(int[] o1, int[] o2) {
                            int compared = new Integer(o1[0]).compareTo(o2[0]);
                            if (compared == 0) {
                                return new Integer(o1[1]).compareTo(o2[1]);
                            }
                            return compared;  //To change body of implemented methods use File | Settings | File Templates.
                        }
                    });
                    PrintWriter p = new PrintWriter(outFolder + "/" + filename + ".cell.keys");

                    for (int[] key : uniqueKeys) {
                        try {
                            StringBuilder line = new StringBuilder();
                            List<List<String>> tableminer_answer = find_answer_at_position(key, tableminer_cells);
                            List<List<String>> baseline_answer = find_answer_at_position(key, baseline_cells);
                            if (!hasSameAnswer(tableminer_answer, baseline_answer)) {
                                line.append(key[0]).append(",").append(key[1]).append("=");
                                line.append("\ttm=");
                                String answer = appendBestAnswer(tableminer_answer);
                                if (answer.equals("null")) {
                                } else {
                                    answer = answer + "|" + extractName(searcher.findAttributesOfEntities(new Entity(answer, answer)));
                                }
                                line.append(answer).append("\t");
                                line.append("\tbs=");

                                answer = appendBestAnswer(baseline_answer);
                                if (answer.equals("null")) {
                                } else {
                                    answer = answer + "|" + extractName(searcher.findAttributesOfEntities(new Entity(answer, answer)));
                                }
                                line.append(answer);
                            }

                            if (line.length() > 0)
                                p.println(line);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }


                    p.close();

                    processed.add(filename);
                } else
                    continue;
            } else {

            }
        }
    }

    private static String extractName(List<Attribute> typesForEntityId) {
        for (Attribute a : typesForEntityId) {
            if (a.getRelationURI().equals("/type/object/name"))
                return a.getValue();
        }
        return "null";  //To change body of created methods use File | Settings | File Templates.
    }

    private static List<List<String>> find_answer_at_position(int[] position, Map<int[], List<List<String>>> answers) {
        for (Map.Entry<int[], List<List<String>>> e : answers.entrySet()) {
            int[] key = e.getKey();
            if (key[0] == position[0] && key[1] == position[1])
                return e.getValue();
        }
        return null;
    }

    private static List<int[]> collect_unique_keys(Map<int[], List<List<String>>> answers1, Map<int[], List<List<String>>> answers2) {
        List<int[]> keys = new ArrayList<int[]>();
        for (int[] key : answers1.keySet()) {
            if (!array_list_contains(key, keys)) {
                keys.add(key);
            }
        }
        for (int[] key : answers2.keySet()) {
            if (!array_list_contains(key, keys)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private static boolean array_list_contains(int[] find, List<int[]> container) {
        for (int[] i : container) {
            if (i[0] == find[0] && i[1] == find[1])
                return true;
        }
        return false;
    }

    private static boolean hasSameAnswer(List<List<String>> one, List<List<String>> two) {
        if (one != null && two != null && one.size() > 0 && two.size() > 0) {
            List<String> one_first = new ArrayList<String>(one.get(0));
            List<String> two_first = new ArrayList<String>(two.get(0));

            one_first.retainAll(two_first);

            if (one_first.size() > 0)
                return true;
        }
        return false;
    }

    private static String appendBestAnswer(List<List<String>> list) {
        if (list == null || list.size() == 0)
            return "null";
        return list.get(0).get(0);
    }
}
