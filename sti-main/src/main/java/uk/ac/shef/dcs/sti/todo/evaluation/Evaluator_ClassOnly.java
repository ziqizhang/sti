package uk.ac.shef.dcs.sti.todo.evaluation;

import uk.ac.shef.dcs.sti.todo.TAnnotationKeyFileReader;
import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 26/02/14
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator_ClassOnly {

    protected static boolean PREDICTION_ONE_CORRECT_COUNTS_ALL = false; //if prediction has multiple labels while gs has 1, if prediction covers gs, should it be 1 correct (true) or not (false)


    public static void main(String[] args) throws IOException {

        /*Evaluator_Generic evaluator = new Evaluator_Generic();
                evaluator.evaluate(
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_annotated(disamb=0)",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_gs",
                "eval.csv",
                "missed.csv"
        );*/
        Evaluator_ClassOnly evaluator = new Evaluator_ClassOnly();
        evaluator.evaluate(
                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_cos(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_header_bs_cos-all.csv",
                "tmp_result/limaye_header_bs_cos-all_missed.csv", false*/
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\tableminer\\tableminer_swj",
                "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\gs\\112_tables_gs(aclshort-changed-iswc)",
                "tmp_result/limaye_100_bs.csv",
                "tmp_result/limaye_100_bs_missed.csv", true

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_nm+first(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_header_bs_nm-all.csv",
                "tmp_result/limaye_header_bs_nm-all_missed.csv", false*/
                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_nm+first(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_header_bs_nm-ne.csv",
                "tmp_result/limaye_header_bs_nm-ne_missed.csv", true*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_dice(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_header_bs_sl_dice-all.csv",
                "tmp_result/limaye_header_bs_sl_dice-all_missed.csv", false*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_dice(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_header_bs_sl_dice-ne.csv",
                "tmp_result/limaye_header_bs_sl_dice-ne_missed.csv", true*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_lev(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_header_bs_sl_lev-all.csv",
                "tmp_result/limaye_header_bs_sl_lev-all_missed.csv", false*/

                /* "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_lev(RI)",
                                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                                "tmp_result/limaye_header_bs_sl_lev-ne.csv",
                                "tmp_result/limaye_header_bs_sl_lev-ne_missed.csv", true
                */
                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd",
               "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
               "tmp_result/limaye_header_tm_ospd-all.csv",
               "tmp_result/limaye_header_tm_ospd-all_missed.csv", false*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_header_tm_ospd-ne.csv",
                "tmp_result/limaye_header_tm_ospd-ne_missed.csv", true*/

                /* "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd_nsc",
               "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
               "tmp_result/limaye_header_tm_ospd_nsc-all.csv",
               "tmp_result/limaye_header_tm_ospd_nsc-all_missed.csv", false*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd_nsc",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_header_tm_ospd_nsc-ne.csv",
                "tmp_result/limaye_header_tm_ospd_nsc-ne_missed.csv", true*/
        );

    }

    public void evaluate(String in_computed_folder,
                         String in_gs_folder,
                         String out_result_file,
                         String out_missed_file, boolean gs_NE_only) throws IOException {
        PrintWriter p = new PrintWriter(out_result_file);
        p.println("File,HEADER(0)_cp_y, gs_y, cp_n, p,r,f," +
                "HEADER(1)_cp_y, gs_y, cp_n, p,r,f");


        PrintWriter out_missed_writer = new PrintWriter(out_missed_file);
        Set<String> processed = new HashSet<String>();
        for (File gsFile : new File(in_gs_folder).listFiles()) {

            Map<Integer, List<List<String>>> gs_headers = null;

            String filename = gsFile.getName();
            if (processed.contains(filename))
                continue;

            if (filename.endsWith(".keys")) {
                int dothtml = filename.lastIndexOf(".html");
                if (dothtml == -1)
                    dothtml = filename.lastIndexOf(".htm");
                if (dothtml == -1) {
                    System.err.println(filename);
                    continue;
                }
                int end = dothtml + 5;

                filename = filename.substring(0, end).trim();
                System.out.println(filename);
                /* if(filename.contains("Wisden"))
                System.out.println();*/
                //if (!filename.contains(".attributes") && !filename.contains(".keys") && !processed.contains(filename)) {
                String entity_gs = gsFile.getParent() + "/" + filename + ".cell.keys";
                processed.add(entity_gs);
                String header_gs = gsFile.getParent() + "/" + filename + ".header.keys";
                processed.add(header_gs);
                String binary = gsFile.getParent() + "/" + filename + ".relation.keys";
                processed.add(binary);

                gs_headers = TAnnotationKeyFileReader.readHeaderAnnotation(header_gs, true, gs_NE_only);
                processed.add(filename);

            } else {
                continue;
            }
            if (gs_headers == null) {
                System.err.println(filename);
                continue;
            }

            boolean found = false;

            for (File cpFile : new File(in_computed_folder).listFiles()) {
                if (cpFile.getName().startsWith(filename)) {
                    found = true;

                    String cpFile_name = cpFile.toString();
                    if (processed.contains(cpFile_name) || !cpFile_name.endsWith("keys"))
                        continue;

                    String entity_cp = cpFile.getParent() + "/" + filename + ".cell.keys";
                    processed.add(entity_cp);
                    String header_cp = cpFile.getParent() + "/" + filename + ".header.keys";
                    processed.add(header_cp);
                    String binary_cp = cpFile.getParent() + "/" + filename + ".relation.keys";
                    processed.add(binary_cp);

                    StringBuilder line = new StringBuilder("\"" + cpFile.getPath() + "\",");

                    Map<Integer, List<List<String>>> cp_headers =
                            TAnnotationKeyFileReader.readHeaderAnnotation(header_cp, false, gs_NE_only);


                    double[] header_data_mode_0 = Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 0);
                    line.append(appendResult(header_data_mode_0));

                    double[] header_data_mode_1 = Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 1);
                    line.append(appendResult(header_data_mode_1));

                    p.println(line.toString());
                    break;
                }
            }
            if (!found) {
                out_missed_writer.println(gsFile);
            }
        }
        out_missed_writer.close();
        p.close();
    }

    public void evaluate_with_filter(
            String log_file_as_filter,
            String in_computed_folder,
            String in_gs_folder,
            String out_result_file,
            String out_missed_file, boolean gs_NE_only) throws IOException {

        Set<String> keepFiles = create_filter_with_log_file(log_file_as_filter);
        PrintWriter p = new PrintWriter(out_result_file);
        p.println("File,HEADER(0)_cp_y, gs_y, cp_n, p,r,f," +
                "HEADER(1)_cp_y, gs_y, cp_n, p,r,f");


        PrintWriter out_missed_writer = new PrintWriter(out_missed_file);
        Set<String> processed = new HashSet<String>();
        for (File gsFile : new File(in_gs_folder).listFiles()) {
            if (!keepFiles.contains(gsFile.getName()))
                continue;

            Map<Integer, List<List<String>>> gs_headers = null;

            String filename = gsFile.getName();
            if (processed.contains(filename))
                continue;

            if (filename.endsWith(".keys")) {
                int dothtml = filename.lastIndexOf(".html");
                if (dothtml == -1)
                    dothtml = filename.lastIndexOf(".htm");
                if (dothtml == -1) {
                    System.err.println(filename);
                    continue;
                }
                int end = dothtml + 5;

                filename = filename.substring(0, end).trim();
                System.out.println(filename);
                /* if(filename.contains("Wisden"))
                System.out.println();*/
                //if (!filename.contains(".attributes") && !filename.contains(".keys") && !processed.contains(filename)) {
                String entity_gs = gsFile.getParent() + "/" + filename + ".cell.keys";
                processed.add(entity_gs);
                String header_gs = gsFile.getParent() + "/" + filename + ".header.keys";
                processed.add(header_gs);
                String binary = gsFile.getParent() + "/" + filename + ".relation.keys";
                processed.add(binary);

                gs_headers = TAnnotationKeyFileReader.readHeaderAnnotation(header_gs, true, gs_NE_only);
                processed.add(filename);

            } else {
                continue;
            }
            if (gs_headers == null) {
                System.err.println(filename);
                continue;
            }

            boolean found = false;

            for (File cpFile : new File(in_computed_folder).listFiles()) {
                if (cpFile.getName().startsWith(filename)) {
                    found = true;

                    String cpFile_name = cpFile.toString();
                    if (processed.contains(cpFile_name) || !cpFile_name.endsWith("keys"))
                        continue;

                    String entity_cp = cpFile.getParent() + "/" + filename + ".cell.keys";
                    processed.add(entity_cp);
                    String header_cp = cpFile.getParent() + "/" + filename + ".header.keys";
                    processed.add(header_cp);
                    String binary_cp = cpFile.getParent() + "/" + filename + ".relation.keys";
                    processed.add(binary_cp);

                    StringBuilder line = new StringBuilder("\"" + cpFile.getPath() + "\",");

                    Map<Integer, List<List<String>>> cp_headers =
                            TAnnotationKeyFileReader.readHeaderAnnotation(header_cp, false, gs_NE_only);


                    double[] header_data_mode_0 = Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 0);
                    line.append(appendResult(header_data_mode_0));

                    double[] header_data_mode_1 = Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 1);
                    line.append(appendResult(header_data_mode_1));

                    p.println(line.toString());
                    break;
                }
            }
            if (!found) {
                out_missed_writer.println(gsFile);
            }
        }
        out_missed_writer.close();
        p.close();
    }

    private Set<String> create_filter_with_log_file(String log_file_as_filter) throws IOException {
        List<String> lines = FileUtils.readList(log_file_as_filter, false);
        Set<String> rs = new HashSet<String>();
        boolean next_new_file = false;
        String next_new_file_name = "";
        boolean converged = false;
        boolean learning_block = false;

        for (String l : lines) {
            next_new_file = false;
            String[] parts = l.split("_", 2);
            if (parts.length > 1) {
                try {
                    Integer.valueOf(parts[0].trim());
                    //if(parts[1].trim().startsWith("E:")){
                    next_new_file = true;
                    learning_block = false;
                    String filePath = parts[1].trim();
                    int end = filePath.indexOf(".xml");
                    filePath = filePath.substring(0, end + 4).trim();
                    next_new_file_name = new File(filePath).getName();

                    //}
                } catch (Exception e) {
                }
            }


            if (l.contains("FORWARD LEARNING"))
                learning_block = true;
            else {
                if (learning_block == true && l.contains("Convergence iteration=")) {
                    //converged=true;
                    rs.add(next_new_file_name);
                }
            }

        }


        return rs;  //To change body of created methods use File | Settings | File Templates.
    }

    public static double[] compute_prf_header(Map<Integer, List<List<String>>> gs,
                                              Map<Integer, List<List<String>>> cp, int mode) {
        double total_correct_by_cp = 0.0, total_wrong_by_cp = 0.0, total_correct_by_gs = 0.0;

        for (Map.Entry<Integer, List<List<String>>> e : gs.entrySet()) {
            int col = e.getKey();
            List<List<String>> gs_annotations = e.getValue();
            List<String> gs_vital = gs_annotations.size() > 0 ? gs_annotations.get(0) : new ArrayList<String>();
            List<List<String>> cp_annotations = cp.get(col);
            if (cp_annotations == null) {
                cp_annotations = new ArrayList<List<String>>();
            }
            List<String> cp_first = cp_annotations.size() > 0 ? cp_annotations.get(0) : new ArrayList<String>();
            if (cp_first.size() == 0) {
                total_correct_by_gs++;
                continue;
            }

            if (mode == 0) {
                double correct_by_gs = 1.0;
                double correct_by_cp = 0, wrong_by_cp = 0;

                List<String> intersection = new ArrayList<String>(cp_first);
                intersection.retainAll(gs_vital);

                if (intersection.size() > 0) {
                    if (PREDICTION_ONE_CORRECT_COUNTS_ALL)
                        correct_by_cp++; //lanient mode. if cp predicts multi labels, as long as one correct, its ok
                    else {
                        correct_by_cp += (double) intersection.size() / cp_first.size();    //strict mode. if cp predicts multi labels, must all be correct, otherwise suffers penalty
                        wrong_by_cp = 1.0 - correct_by_cp;
                    }
                } else {
                    wrong_by_cp++;
                }
                //   }
                total_correct_by_cp += correct_by_cp;
                total_correct_by_gs += correct_by_gs;
                total_wrong_by_cp += wrong_by_cp;
            } else {
                int correct_by_gs = 1;

                double correct_by_cp = 0.0, wrong_by_cp = 0.0;
                List<String> intersection_vital = new ArrayList<String>(cp_first);
                List<String> intersection_ok = new ArrayList<String>(cp_first);
                List<String> gs_ok = gs_annotations.size() == 2 ? gs_annotations.get(1) : new ArrayList<String>();

                intersection_vital.retainAll(gs_vital);
                intersection_ok.retainAll(gs_ok);

                double total_correct = intersection_vital.size() + 0.5 * intersection_ok.size();
                correct_by_cp = total_correct / (double) cp_first.size();
                wrong_by_cp = 1.0 - correct_by_cp;

                total_correct_by_cp += correct_by_cp;
                total_correct_by_gs += correct_by_gs;
                total_wrong_by_cp += wrong_by_cp;
            }
        }
        return new double[]{total_correct_by_cp, total_correct_by_gs, total_wrong_by_cp};
    }

    @Deprecated
    public static double[] compute_prf_relation_or_entity(Map<int[], List<List<String>>> gs,
                                                          Map<int[], List<List<String>>> cp, int mode) {
        double total_correct_by_cp = 0.0, total_wrong_by_cp = 0.0, total_correct_by_gs = 0.0;

        for (Map.Entry<int[], List<List<String>>> e : gs.entrySet()) {
            int[] col = e.getKey();
            List<List<String>> gs_annotations = e.getValue();
            List<String> gs_vital = gs_annotations.size() > 0 ? gs_annotations.get(0) : new ArrayList<String>();
            List<List<String>> cp_annotations = new ArrayList<List<String>>();

            for (Map.Entry<int[], List<List<String>>> f : cp.entrySet()) {
                int[] col_cp = f.getKey();
                if (col[0] == col_cp[0] && col[1] == col_cp[1]) {
                    cp_annotations = f.getValue();
                    break;
                }
            }
            List<String> cp_first = cp_annotations.size() > 0 ? cp_annotations.get(0) : new ArrayList<String>();
            if (cp_first.size() == 0) {
                total_correct_by_gs++;
                continue;
            }


            if (mode == 0) {
                double correct_by_gs = 1.0;
                double correct_by_cp = 0, wrong_by_cp = 0;

                List<String> intersection = new ArrayList<String>(cp_first);
                intersection.retainAll(gs_vital);

                if (intersection.size() > 0) {
                    if (PREDICTION_ONE_CORRECT_COUNTS_ALL)
                        correct_by_cp++; //lanient mode. if cp predicts multi labels, as long as one correct, its ok
                    else {
                        correct_by_cp += (double) intersection.size() / cp_first.size();    //strict mode. if cp predicts multi labels, must all be correct, otherwise suffers penalty
                        wrong_by_cp = 1.0 - correct_by_cp;
                    }
                } else {
                    wrong_by_cp++;
                }
                //   }
                total_correct_by_cp += correct_by_cp;
                total_correct_by_gs += correct_by_gs;
                total_wrong_by_cp += wrong_by_cp;
            } else {
                int correct_by_gs = 1;

                double correct_by_cp = 0.0, wrong_by_cp = 0.0;
                List<String> intersection_vital = new ArrayList<String>(cp_first);
                List<String> intersection_ok = new ArrayList<String>(cp_first);
                List<String> gs_ok = gs_annotations.size() == 2 ? gs_annotations.get(1) : new ArrayList<String>();

                intersection_vital.retainAll(gs_vital);
                intersection_ok.retainAll(gs_ok);

                double total_correct = intersection_vital.size() + 0.5 * intersection_ok.size();
                correct_by_cp = total_correct / (double) cp_first.size();
                wrong_by_cp = cp_first.size() - correct_by_cp;

                total_correct_by_cp += correct_by_cp;
                total_correct_by_gs += correct_by_gs;
                total_wrong_by_cp += wrong_by_cp;
            }
        }
        return new double[]{total_correct_by_cp, total_correct_by_gs, total_wrong_by_cp};
    }


    private String appendResult(double[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append(values[0]).append(",")
                .append(values[1]).append(",")
                .append(values[2]).append(",");
        double p = values[0] / (values[0] + values[2]);
        p = values[0] == 0 ? 0.0 : p;

        double r = values[0] / (values[1]);
        r = values[0] == 0 ? 0 : r;
        double f = 2 * p * r / (p + r);
        f = r == 0.0 || p == 0.0 ? 0.0 : f;

        sb.append(p).append(",")
                .append(r).append(",")
                .append(f).append(",");
        return sb.toString();

    }

}
