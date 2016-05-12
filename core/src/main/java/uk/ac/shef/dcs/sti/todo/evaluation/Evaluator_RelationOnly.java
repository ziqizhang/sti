package uk.ac.shef.dcs.sti.todo.evaluation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import uk.ac.shef.dcs.sti.todo.TAnnotationKeyFileReader;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 26/02/14
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator_RelationOnly {

    protected static boolean PREDICTION_ONE_CORRECT_COUNTS_ALL = false; //if prediction has multiple labels while gs has 1, if prediction covers gs, should it be 1 correct (true) or not (false)


    public static void main(String[] args) throws IOException {

        /*Evaluator_Generic evaluator = new Evaluator_Generic();
                evaluator.evaluate(
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_annotated(disamb=0)",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_gs",
                "eval.csv",
                "missed.csv"
        );*/
        Evaluator_RelationOnly evaluator = new Evaluator_RelationOnly();
        evaluator.evaluate(
                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_cos(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_bs_cos-all.csv",
                "tmp_result/limaye_rel_bs_cos_missed-all.csv",true,false*/

                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_cos(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_bs_cos-ne.csv",
                "tmp_result/limaye_rel_bs_cos_missed-ne.csv",true,true

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_nm+first(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_bs_nm-all.csv",
                "tmp_result/limaye_rel_bs_nm_missed-all.csv",true,false
*/
                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_nm+first(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_bs_nm-ne.csv",
                "tmp_result/limaye_rel_bs_nm_missed-ne.csv",true,true*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_dice(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_bs_dice-all.csv",
                "tmp_result/limaye_rel_bs_dice_missed-all.csv",true,false*/
                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_dice(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_bs_dice-ne.csv",
                "tmp_result/limaye_rel_bs_dice_missed-ne.csv",true,true*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_lev(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_bs_lev-all.csv",
                "tmp_result/limaye_rel_bs_lev_missed-all.csv",true,false*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_lev(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_bs_lev-ne.csv",
                "tmp_result/limaye_rel_bs_lev_missed-ne.csv",true,true*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_tm_ospd-all.csv",
                "tmp_result/limaye_rel_tm_ospd_missed-all.csv",true,false*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_tm_ospd-ne.csv",
                "tmp_result/limaye_rel_tm_ospd_missed-ne.csv",true,true*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd_nsc",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_tm_ospd_nsc-all.csv",
                "tmp_result/limaye_rel_tm_ospd_nsc_missed-all.csv",true,false*/

                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd_nsc",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tmp_result/limaye_rel_tm_ospd_nsc-ne.csv",
                "tmp_result/limaye_rel_tm_ospd_nsc_missed-ne.csv",true,true*/
        );

    }

    public void evaluate(String in_computed_folder,
                         String in_gs_folder,
                         String in_header_gs_folder,
                         String out_result_file,
                         String out_missed_file, boolean tolerant_mode,boolean ne_relation_only) throws IOException {
        PrintWriter p = new PrintWriter(out_result_file);
        p.println("File,REL(0)_cp_y, gs_y, cp_n, p,r,f," +
                "REL(1)_cp_y, gs_y, cp_n, p,r,f");


        PrintWriter out_missed_writer = new PrintWriter(out_missed_file);
        Set<String> processed = new HashSet<String>();
        for (File gsFile : new File(in_gs_folder).listFiles()) {

            Map<int[], List<List<String>>>gs_binaryRels = null;
            Map<Integer, List<List<String>>> gs_header=null;

            List<String> main = new ArrayList<String>();
            List<String> tolerant = new ArrayList<String>();

            String filename = gsFile.getName();
            if(processed.contains(filename))
                continue;

            if (filename.endsWith(".keys")) {
                int dothtml = filename.lastIndexOf(".html");
                if(dothtml==-1)
                    dothtml=filename.lastIndexOf(".htm");
                if(dothtml==-1){
                    System.err.println(filename);
                    continue;
                }
                int end = dothtml+5;

                filename = filename.substring(0, end).trim();
                System.out.println(filename);
                /* if(filename.contains("Wisden"))
                System.out.println();*/
                //if (!filename.contains(".attributes") && !filename.contains(".keys") && !processed.contains(filename)) {

                String binary = gsFile.getParent()+"/"+filename + ".relation.keys";
                processed.add(binary);
                String header = in_header_gs_folder+"/"+filename+".header.keys";
                gs_header= TAnnotationKeyFileReader.readHeaderAnnotation(header, true, true);

                gs_binaryRels = TAnnotationKeyFileReader.readColumnBinaryRelationAnnotation_GS(binary, main, tolerant);

                processed.add(filename);

            } else {
                continue;
            }
            if (gs_binaryRels == null) {
                System.err.println(filename);
                continue;
            }

            boolean found = false;

            for (File cpFile : new File(in_computed_folder).listFiles()) {
                if (cpFile.getName().startsWith(filename)) {
                    found = true;

                    String cpFile_name = cpFile.toString();
                    if(processed.contains(cpFile_name)||!cpFile_name.endsWith("keys"))
                        continue;

                    String entity_cp = cpFile.getParent()+"/"+filename + ".cell.keys";
                    processed.add(entity_cp);
                    String header_cp = cpFile.getParent()+"/"+filename + ".header.keys";
                    processed.add(header_cp);
                    String binary_cp = cpFile.getParent()+"/"+filename + ".relation.keys";
                    processed.add(binary_cp);

                    StringBuilder line = new StringBuilder("\"" + cpFile.getPath() + "\",");

                    Map<int[], List<List<String>>> cp_binaryRels =
                            TAnnotationKeyFileReader.readColumnBinaryRelationAnnotation_CP(binary_cp);

                    double[] relation_data_mode_0 = compute_prf_relation(gs_binaryRels, cp_binaryRels, main, gs_header,0,ne_relation_only);
                    line.append(appendResult(relation_data_mode_0));

                    double[] relation_data_mode_1 = compute_prf_relation(gs_binaryRels, cp_binaryRels, main,gs_header, 1,ne_relation_only);
                    line.append(appendResult(relation_data_mode_1));

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

    //warning: only works for relations between subcol and other cols. if in predicated relation files there are m:n relations this will fail!!!
    public static double[] compute_prf_relation(Map<int[], List<List<String>>> gs,
                                                          Map<int[], List<List<String>>> cp,
                                                          List<String> main,
                                                          Map<Integer, List<List<String>>> gs_header,
                                                          int mode,
                                                          boolean ne_relation_only) {
        double total_correct_by_cp = 0.0, total_wrong_by_cp = 0.0, total_correct_by_gs = 0.0;

        for (Map.Entry<int[], List<List<String>>> e : gs.entrySet()) {
            int[] col = e.getKey();

            if(ne_relation_only){
                if(!gs_header.containsKey(col[0])||!gs_header.containsKey(col[1]))
                    continue;
            }

            String key = col[0]+","+col[1];

            boolean isMain=false;
            if(main.contains(key)) {  //only the main entry is incremented. for every tolerant gs answer, there must be a main answer
                total_correct_by_gs++;
                isMain=true;
            }

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
                continue;
            }

            if (mode == 0 &&isMain) {
                List<String> intersection = new ArrayList<String>(cp_first);
                intersection.retainAll(gs_vital);

                double correct_by_cp=0;
                if (intersection.size() > 0) {
                    if (PREDICTION_ONE_CORRECT_COUNTS_ALL){
                        correct_by_cp=1;
                    }
                    else {
                        double increment = (double) intersection.size() / cp_first.size();
                        correct_by_cp = increment;   //strict mode. if cp predicts multi labels, must all be correct, otherwise suffers penalty
                    }
                } else {
                }
                //   }
                total_correct_by_cp += correct_by_cp;
                total_wrong_by_cp += 1.0-correct_by_cp;
            } else if(mode==1){
                double multiplier=isMain?1.0:0.5;
                double correct_by_cp = 0.0;
                List<String> intersection_vital = new ArrayList<String>(cp_first);
                List<String> intersection_ok = new ArrayList<String>(cp_first);
                List<String> gs_ok = gs_annotations.size() == 2 ? gs_annotations.get(1) : new ArrayList<String>();

                intersection_vital.retainAll(gs_vital);
                intersection_ok.retainAll(gs_ok);

                double total_correct = intersection_vital.size() + 0.5 * intersection_ok.size();
                correct_by_cp = total_correct / (double) cp_first.size();
                correct_by_cp=correct_by_cp*multiplier;

                total_correct_by_cp += correct_by_cp;
                total_wrong_by_cp+=multiplier-correct_by_cp;
            }
        }
       // total_wrong_by_cp=total_correct_by_gs-total_correct_by_cp;
        if(total_wrong_by_cp<0){
            System.err.println("this should not happen, total wrong<0");
            total_wrong_by_cp=0;
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
