package uk.ac.shef.dcs.sti.experiment.evaluation;

import uk.ac.shef.dcs.sti.io.LTableAnnotationKeyFileReader;

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
public class Evaluator_Generic_old {

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
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\tableminer_aclshort_no_ref_ent",
                //"E:\\Data\\table annotation\\corpus_analysis\\100_tables\\baseline_aclshort",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_gs_aclshort",
                //"E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_gs_coling",
                "eval.csv",
                "missed.csv",true
        );

    }

    public void evaluate(String in_computed_folder,
                         String in_gs_folder,
                         String out_result_file,
                         String out_missed_file,
                         boolean gs_NE_only) throws IOException {
        PrintWriter p = new PrintWriter(out_result_file);
        p.println("File,HEADER(0)_cp_y, gs_y, cp_n, p,r,f, REL(0)_cp_y, gs_y, cp_n, p,r,f, ENT(0)_cp_y,gs_y,cp_n,p,r,f," +
                "HEADER(1)_cp_y, gs_y, cp_n, p,r,f, REL(1)_cp_y, gs_y, cp_n, p,r,f, ENT(1)_cp_y,gs_y,cp_n,p,r,f");


        PrintWriter out_missed_writer = new PrintWriter(out_missed_file);
        Set<String> processed = new HashSet<String>();
        for (File gsFile : new File(in_gs_folder).listFiles()) {
            Map<int[], List<List<String>>> gs_cells = null, gs_binaryRels = null;
            Map<Integer, List<List<String>>> gs_headers = null;

            String filename = gsFile.getName();

            if (filename.endsWith(".htm") || filename.endsWith(".html")) {
                if (!filename.contains(".triples") && !filename.contains(".keys") && !processed.contains(filename)) {
                    String entity_gs = gsFile.toString() + ".cell.keys";
                    String header_gs = gsFile.toString() + ".header.keys";
                    String binary = gsFile.toString() + ".relation.keys";

                    gs_cells = LTableAnnotationKeyFileReader.readCellAnnotation(entity_gs);
                    gs_headers = LTableAnnotationKeyFileReader.readHeaderAnnotation(header_gs,true,gs_NE_only);
                    gs_binaryRels = LTableAnnotationKeyFileReader.readColumnBinaryRelationAnnotation_GS(binary, new ArrayList<String>(), new ArrayList<String>());

                    processed.add(filename);
                } else
                    continue;
            } else {
                continue;
            }
            if (gs_cells == null || gs_binaryRels == null || gs_headers == null) {
                System.err.println(filename);
                continue;
            }

            boolean found = false;

            for (File cpFile : new File(in_computed_folder).listFiles()) {
                if (gsFile.getName().equals(cpFile.getName())) {
                    found = true;

                    String cpFile_name = cpFile.toString();
                    StringBuilder line = new StringBuilder("\"" + cpFile.getPath() + "\",");

                    Map<Integer, List<List<String>>> cp_headers =
                            LTableAnnotationKeyFileReader.readHeaderAnnotation(cpFile_name + ".header.keys",false,gs_NE_only);
                    Map<int[], List<List<String>>> cp_binaryRels =
                            LTableAnnotationKeyFileReader.readColumnBinaryRelationAnnotation_CP(cpFile_name + ".relation.keys");
                    Map<int[], List<List<String>>> cp_cells =
                            LTableAnnotationKeyFileReader.readCellAnnotation(cpFile_name + ".cell.keys");


                    double[] header_data_mode_0 = Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 0);
                    line.append(appendResult(header_data_mode_0));
                    double[] relation_data_mode_0 = Evaluator_ClassOnly.compute_prf_relation_or_entity(gs_binaryRels, cp_binaryRels, 0);
                    line.append(appendResult(relation_data_mode_0));
                    double[] cell_data_mode_0 = Evaluator_ClassOnly.compute_prf_relation_or_entity(gs_cells, cp_cells, 0);
                    line.append(appendResult(cell_data_mode_0));
                    double[] header_data_mode_1 = Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 1);
                    line.append(appendResult(header_data_mode_1));
                    double[] relation_data_mode_1 = Evaluator_ClassOnly.compute_prf_relation_or_entity(gs_binaryRels, cp_binaryRels, 1);
                    line.append(appendResult(relation_data_mode_1));
                    double[] cell_data_mode_1 = Evaluator_ClassOnly.compute_prf_relation_or_entity(gs_cells, cp_cells, 1);
                    line.append(appendResult(cell_data_mode_1));

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

    public static double[] compute_prf_header(Map<Integer, List<List<String>>> gs,
                                              Map<Integer, List<List<String>>> cp, int mode) {
        double total_correct_by_cp = 0.0, total_wrong_by_cp = 0.0, total_correct_by_gs = 0.0;

        for (Map.Entry<Integer, List<List<String>>> e : gs.entrySet()) {
            int col = e.getKey();
            List<List<String>> gs_annotations = e.getValue();
            List<List<String>> cp_annotations = cp.get(col);
            if (cp_annotations == null) {
                total_correct_by_gs += gs_annotations.size();
                continue;
            }

            if (mode == 0) {
                double correct_by_gs = gs_annotations.size();
                double correct_by_cp = 0, wrong_by_cp = 0;
                for (int i = 0; i < correct_by_gs; i++) {
                    List<String> gs_label = gs_annotations.get(i);
                    int index_in_cp = find_index_in(gs_label, cp_annotations);
                    if (i == index_in_cp) {
                        if (PREDICTION_ONE_CORRECT_COUNTS_ALL)
                            correct_by_cp++; //lanient mode. if cp predicts multi labels, as long as one correct, its ok
                        else {
                            List<String> cp_label = cp_annotations.get(index_in_cp);

                            List<String> overlap = new ArrayList<String>(cp_label);
                            overlap.retainAll(gs_label);

                            correct_by_cp += (double) overlap.size() / cp_label.size();    //strict mode. if cp predicts multi labels, must all be correct, otherwise suffers penalty
                        }
                    } else {
                        wrong_by_cp++;
                    }
                }
                total_correct_by_cp += correct_by_cp;
                total_correct_by_gs += correct_by_gs;
                total_wrong_by_cp += wrong_by_cp;
            } else {
                int correct_by_gs = gs_annotations.size();

                double correct_by_cp = 0.0, has_correct=0.0,wrong_by_cp = 0.0;
                List<List<String>> correct_by_cp_all = new ArrayList<List<String>>(gs_annotations);
                retain_overlap(correct_by_cp_all, cp_annotations);
                for (List<String> element : correct_by_cp_all) {
                    int index_in_gs = find_index_in(element, gs_annotations);
                    int index_in_cp = find_index_in(element, cp_annotations);

                    List<String> gs_label = gs_annotations.get(index_in_gs);

                    if (index_in_cp != -1) {
                        List<String> cp_label = cp_annotations.get(index_in_cp);

                        List<String> overlap = new ArrayList<String>(cp_label);
                        overlap.retainAll(gs_label);

                        correct_by_cp += (double) overlap.size() / cp_label.size() / (index_in_cp + 1);
                        has_correct+=1.0;
                    }
                    /*wrong_by_cp += 1 - correct_by_cp;*/
                }
                total_correct_by_cp += correct_by_cp;
                total_correct_by_gs += correct_by_gs;
                total_wrong_by_cp += (has_correct-correct_by_cp);
            }
        }
        return new double[]{total_correct_by_cp, total_correct_by_gs, total_wrong_by_cp};
    }


    public static double[] compute_prf_relation_or_entity(Map<int[], List<List<String>>> gs,
                                                          Map<int[], List<List<String>>> cp, int mode) {
        double total_correct_by_cp = 0.0, total_wrong_by_cp = 0.0, total_correct_by_gs = 0.0;

        for (Map.Entry<int[], List<List<String>>> e : gs.entrySet()) {
            int[] col = e.getKey();
            List<List<String>> gs_annotations = e.getValue();
            List<List<String>> cp_annotations = new ArrayList<List<String>>();
            for (Map.Entry<int[], List<List<String>>> f : cp.entrySet()) {
                int[] col_cp = f.getKey();
                if (col[0] == col_cp[0] && col[1] == col_cp[1]) {
                    cp_annotations = f.getValue();
                    break;
                }
            }
            if (cp_annotations.size() == 0) {
                total_correct_by_gs += gs_annotations.size();
                continue;
            }


            if (mode == 0) {
                int correct_by_gs = gs_annotations.size();
                int correct_by_cp = 0, wrong_by_cp = 0;
                for (int i = 0; i < correct_by_gs; i++) {
                    List<String> gs_label = gs_annotations.get(i);
                    int index_in_cp = find_index_in(gs_label, cp_annotations);
                    if (i == index_in_cp) {
                        if (PREDICTION_ONE_CORRECT_COUNTS_ALL)
                            correct_by_cp++; //lanient mode. if cp predicts multi labels, as long as one correct, its ok
                        else {
                            List<String> cp_label = cp_annotations.get(index_in_cp);

                            List<String> overlap = new ArrayList<String>(cp_label);
                            overlap.retainAll(gs_label);

                            correct_by_cp += (double) overlap.size() / cp_label.size();    //strict mode. if cp predicts multi labels, must all be correct, otherwise suffers penalty
                        }
                    } else {
                        wrong_by_cp++;
                    }
                }
                total_correct_by_cp += correct_by_cp;
                total_correct_by_gs += correct_by_gs;
                total_wrong_by_cp += wrong_by_cp;
            } else {
                int correct_by_gs = gs_annotations.size();

                double correct_by_cp = 0.0, wrong_by_cp = 0.0;
                List<List<String>> correct_by_cp_all = new ArrayList<List<String>>(gs_annotations);
                retain_overlap(correct_by_cp_all, cp_annotations);
                for (List<String> element : correct_by_cp_all) {
                    int index_in_gs = find_index_in(element, gs_annotations);
                    int index_in_cp = find_index_in(element, cp_annotations);

                    List<String> gs_label = gs_annotations.get(index_in_gs);

                    if (index_in_cp != -1) {
                        List<String> cp_label = cp_annotations.get(index_in_cp);

                        List<String> overlap = new ArrayList<String>(cp_label);
                        overlap.retainAll(gs_label);

                        correct_by_cp += (double) overlap.size() / cp_label.size() / (index_in_cp + 1);
                    }
                    wrong_by_cp += 1 - correct_by_cp;
                }
                if(correct_by_cp_all.size()==0){
                    wrong_by_cp++;
                }
                total_correct_by_cp += correct_by_cp;
                total_correct_by_gs += correct_by_gs;
                total_wrong_by_cp += wrong_by_cp;
            }
        }
        return new double[]{total_correct_by_cp, total_correct_by_gs, total_wrong_by_cp};
    }

    protected static void retain_overlap(List<List<String>> correct_by_cp_all, List<List<String>> cp_annotations) {
        Iterator<List<String>> it = correct_by_cp_all.iterator();
        while (it.hasNext()) {
            List<String> o = it.next();
            int index = find_index_in(o, cp_annotations);
            if (index == -1) {
                it.remove();
            }
        }
    }


    protected static int find_index_in(List<String> gs_label, List<List<String>> cp_annotations) {
        for (int i = 0; i < cp_annotations.size(); i++) {
            List<String> o = cp_annotations.get(i);

            List<String> copy = new ArrayList<String>(o);
            copy.retainAll(gs_label);
            if (copy.size() > 0)
                return i;

        }

        return -1;
    }


    private String appendResult(double[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append(values[0]).append(",")
                .append(values[1]).append(",")
                .append(values[2]).append(",");
        double p = values[0] / (values[0] + values[2]);
        p = values[0] == 0 ? 0.0 : p;

        double r = values[0] / (values[1]);
        r=values[0]==0?0:r;
        double f = 2 * p * r / (p + r);
        f = r == 0.0 || p == 0.0 ? 0.0 : f;

        sb.append(p).append(",")
                .append(r).append(",")
                .append(f).append(",");
        return sb.toString();

    }

}
