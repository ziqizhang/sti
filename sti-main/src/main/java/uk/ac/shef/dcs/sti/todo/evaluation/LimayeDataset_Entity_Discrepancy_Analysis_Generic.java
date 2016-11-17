package uk.ac.shef.dcs.sti.todo.evaluation;

import info.aduna.io.FileUtil;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.freebase.FreebaseSearch;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.todo.TAnnotationKeyFileReader;
import uk.ac.shef.dcs.sti.util.FileUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 24/04/14
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */
public class LimayeDataset_Entity_Discrepancy_Analysis_Generic {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        /*String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";
        File configFile = new File(cacheFolder + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolder,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");
//object to fetch things from KB
        String freebaseProperties = "D:/Work/lodiecrawler/src/main/java/freebase.properties";
        KBSearcher_Freebase freebaseMatcher = new KBSearcher_Freebase(freebaseProperties, server, true);

        Map<String, String> entity_annotation_output_each_method = new HashMap<String, String>();
        entity_annotation_output_each_method.put("tm",
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\tableminer\\tableminer_swj");
        entity_annotation_output_each_method.put("nm",
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\baseline\\baseline_swj_nm+first");
        entity_annotation_output_each_method.put("lev",
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\baseline\\baseline_iswc_swj_lev");
        find_discrepancies_between(
                "E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(limaye_original)",
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\discrepancy_analysis_jws",
                freebaseMatcher,
                entity_annotation_output_each_method);
        server.closeConnection();
        System.exit(0);*/


        /*update_discrepancy_data_using_previous_discrepancy_analysis(
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\discrepancy_analysis_iswc",
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\discrepancy_analysis_jws",
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\_tmp_"
        );
        System.exit(0);*/


        calculate_different_accuracy_between(
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\discrepancy_analysis_swj",
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
                FileUtil.copyFile(f, new File(outfolder + "/_" + name));
                System.out.println("new file:" + f);
                continue;
            }

            List<String> prev_discrepancies = FileUtils.readList(prev_discrepancy_file.toString(), false);
            List<String> current_discrepancies = FileUtils.readList(f.toString(), false);

            Map<String, String> prev_discrepancy_correct_answer = new HashMap<String, String>();
            Map<String, Set<String>> prev_discrepancy_candidates = new HashMap<String, Set<String>>();

            for (String l : prev_discrepancies) {
                String[] first_split = l.split("=", 2);
                String position = first_split[0].trim();
                String[] second_split = first_split[1].trim().split("\t\t");

                Set<String> candidates = prev_discrepancy_candidates.get(position);
                candidates = candidates == null ? new HashSet<String>() : candidates;

                boolean hasCorrect = false;
                for (String each : second_split) {
                    String id = each.split("=")[1].trim();
                    if (id.contains("|")) {
                        id = id.split("\\|")[0].trim();
                    }
                    if (!id.equals("null"))
                        candidates.add(id);

                    if (each.startsWith("x")) {
                        prev_discrepancy_correct_answer.put(position, id);
                        hasCorrect = true;
                    }
                }
                prev_discrepancy_candidates.put(position, candidates);

                if (!hasCorrect) {
                    prev_discrepancy_correct_answer.put(position, "none");
                }
            }

            List<String> outLines = new ArrayList<String>();
            for (String l : current_discrepancies) {
                String[] first_split = l.split("=", 2);
                String position = first_split[0].trim();
                String correct = prev_discrepancy_correct_answer.get(position);
                Set<String> previous_candidates = prev_discrepancy_candidates.get(position);
                String[] second_split = first_split[1].trim().split("\t");
                if (correct == null) {
                    outLines.add("?" + l);
                    continue;
                } else if (correct.equals("none")) {
                    Set<String> candidates_current = new HashSet<String>();
                    for (String each : second_split) {
                        String[] components = each.split("=");
                        String id = components[1];
                        if (id.contains("|"))
                            id = id.split("\\|")[0].trim();
                        candidates_current.add(id);
                    }
                    candidates_current.removeAll(previous_candidates);
                    if (candidates_current.size() == 0) {
                        outLines.add(l);
                        continue;
                    } else {
                        outLines.add("?" + l);
                        continue;
                    }
                }

                boolean found = false;
                Set<Integer> correct_indexes = new HashSet<Integer>();
                int index = -1;
                for (String each : second_split) {
                    index++;
                    String[] components = each.split("=");

                    String id = components[1];
                    if (id.contains("|"))
                        id = id.split("\\|")[0].trim();

                    if (correct.equals(id)) {
                        found = true;
                        correct_indexes.add(index);
                    }
                }
                if (found) {
                    String line = position + "=\t";
                    int ind = -1;
                    for (String each : second_split) {
                        ind++;
                        if (correct_indexes.contains(ind)) {
                            each = "x" + each.trim();
                        }
                        line = line + each + "\t";
                    }
                    outLines.add(line.trim());
                }

            }

            Collections.sort(outLines);
            PrintWriter p = new PrintWriter(outfolder + "/" + name);
            for (String l : outLines)
                p.println(l);
            p.close();
        }
    }

    public static void calculate_different_accuracy_between(String analysis_file_folder, boolean either_or) throws IOException {
        int total = 0, total_either_or=0;
        Map<String, Integer> count_correct =new HashMap<String, Integer>();
        for (File f : new File(analysis_file_folder).listFiles()) {
            System.out.println(f);
            List<String> all = FileUtils.readList(f.toString(), false);
            total += all.size();

            for (String line : all) {
                if (line.startsWith("x")) {
                    System.out.println(line);
                    continue;
                }
                String[] parts = line.split("=", 2)[1].trim().split("\t+");

                boolean foundCorrect=false;
                for(String m: parts){
                    m = m.split("=")[0].trim();

                    if(m.startsWith("x")){
                        m=m.substring(1).trim();
                        Integer c = count_correct.get(m);
                        c=c==null?0:c;
                        c++;
                        count_correct.put(m,c);
                        foundCorrect=true;
                    }
                }
                if(foundCorrect)
                    total_either_or++;
            }

        }
        System.out.println(total +","+total_either_or+","+ count_correct);
    }

    public static void find_discrepancies_between(
            String gs_entity_annotation_folder,
            String out_folder,
            FreebaseSearch searcher,
            Map<String, String> result_annotation_folders_to_consider) throws IOException, KBProxyException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        String reference_entity_annotation_folder_identifier = "tm";
        String reference_entity_annotation_folder =
                result_annotation_folders_to_consider.get(reference_entity_annotation_folder_identifier);

        for (File each_table_file_from_reference_entity_annotation_folder :
                new File(reference_entity_annotation_folder).listFiles()) {
            if (!each_table_file_from_reference_entity_annotation_folder.getName().endsWith("cell.keys"))
                continue;

            String gs_entity_annotation_file = gs_entity_annotation_folder + "/" + each_table_file_from_reference_entity_annotation_folder.getName();
            gs_entity_annotation_file = gs_entity_annotation_file.substring(0, gs_entity_annotation_file.indexOf(
                    "html.cell.keys"
            ));
            gs_entity_annotation_file = gs_entity_annotation_file + "cell.keys";
            PrintWriter p = new PrintWriter(out_folder + "/" + each_table_file_from_reference_entity_annotation_folder.getName());
            Map<int[], List<List<String>>> gs_entity_annotations =
                    TAnnotationKeyFileReader.readCellAnnotation(gs_entity_annotation_file);
            Set<String> exclude_cells = new HashSet<String>();
            for (int[] key : gs_entity_annotations.keySet()) {
                exclude_cells.add(key[0] + "," + key[1]);
            }

            Map<String, Map<String, String>> map_method_id_to_entity_annotations = new HashMap<String, Map<String, String>>();

//read annotations by the "reference" method
            Map<int[], List<List<String>>> reference_entity_annotations_O =
                    TAnnotationKeyFileReader.readCellAnnotation(each_table_file_from_reference_entity_annotation_folder.toString());
            Map<String, String> reference_entity_annotations = new HashMap<String, String>();
            for (int[] key : reference_entity_annotations_O.keySet()) {
                if (exclude_cells.contains(key[0] + "," + key[1]))
                    continue;
                try {
                    reference_entity_annotations.put(key[0] + "," + key[1], reference_entity_annotations_O.get(key).get(0).get(0));

                } catch (Exception npe) {
                }
                ;
            }
            map_method_id_to_entity_annotations.put(reference_entity_annotation_folder_identifier, reference_entity_annotations);

//then for each folder of annotated entities corresponding to a TI method do the following
            for (Map.Entry<String, String> e : result_annotation_folders_to_consider.entrySet()) {
                if (e.getValue().equals(reference_entity_annotation_folder))
                    continue;

                String identifier = e.getKey();
                String file_target = e.getValue() + "/" + each_table_file_from_reference_entity_annotation_folder.getName();

                Map<int[], List<List<String>>> target_entity_annotations_O =
                        TAnnotationKeyFileReader.readCellAnnotation(file_target);
                Map<String, String> target_entity_annotations = new HashMap<String, String>();
                for (int[] key : target_entity_annotations_O.keySet()) {
                    if (exclude_cells.contains(key[0] + "," + key[1]))
                        continue;
                    try {
                        target_entity_annotations.put(key[0] + "," + key[1], target_entity_annotations_O.get(key).get(0).get(0));

                    } catch (Exception npe) {
                    }
                    ;
                }
                map_method_id_to_entity_annotations.put(identifier, target_entity_annotations);
            }


//then consolidate results and output
            List<String> reference_entity_cells = new ArrayList<String>(reference_entity_annotations.keySet());
            Collections.sort(reference_entity_cells);

            for (String key : reference_entity_cells) {
//for each cell, gather annotations by different methods
                Set<String> different_annotations_by_different_methods = new HashSet<String>();
                for (Map.Entry<String, Map<String, String>> e : map_method_id_to_entity_annotations.entrySet()) {
                    String methodKey = e.getKey();
                    Map<String, String> annotations = e.getValue();
                    String ann = annotations.get(key);
                    different_annotations_by_different_methods.add(ann);
                }

//if disagrees, append results by each method
                if (different_annotations_by_different_methods.size() > 1) {

                    StringBuilder line = new StringBuilder();

                    line.append(key).append("=");
                    String reference_entity_annotation = reference_entity_annotations.get(key);
                    /*if(reference_entity_annotation.equals("/m/09v1w7p/m/0_rw_rh/m/0_rljbx/m/09q7yw3"))
                    System.out.println();*/
                    line.append("\t" + reference_entity_annotation_folder_identifier + "=").
                            append(reference_entity_annotation + "|").append(
                            extractName(
                                    searcher.findAttributesOfEntities(new Entity(reference_entity_annotation, reference_entity_annotation)))
                    );

                    for (Map.Entry<String, Map<String, String>> e : map_method_id_to_entity_annotations.entrySet()) {
                        String methodKey = e.getKey();
                        if (methodKey.equals(reference_entity_annotation_folder_identifier))
                            continue;

                        Map<String, String> annotations = e.getValue();
                        String ann = annotations.get(key);

                        if (ann == null) ann = "null";
                        if (ann.equals("null")) {
                            line.append("\t").append(methodKey).append("=").append(ann).append("|null");
                        } else {
                            line.append("\t").append(methodKey).append("=").append(ann).append("|").append(
                                    extractName(searcher.findAttributesOfEntities(new Entity(ann, ann)))

                            );
                        }
                    }
                    p.println(line);
                }

            }

            p.close();
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
