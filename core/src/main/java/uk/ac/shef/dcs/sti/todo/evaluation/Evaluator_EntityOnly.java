package uk.ac.shef.dcs.sti.todo.evaluation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import uk.ac.shef.dcs.sti.todo.TAnnotationKeyFileReader;

/**

 */
public class Evaluator_EntityOnly {
    protected static boolean PREDICTION_ONE_CORRECT_COUNTS_ALL = false; //if prediction has multiple labels while gs has 1, if prediction covers gs, should it be 1 correct (true) or not (false)
    protected static List<String> filter_files = new ArrayList<String>();


    public static void main(String[] args) throws IOException {
        /*String only_files_from = "E:\\Data\\table_annotation\\limaye_sample\\112_tables\\raw\\112_tables";
        if (only_files_from.length() > 0) {
            for (File f : new File(only_files_from).listFiles()) {
                filter_files.add(f.getName());
            }
        }*/

        Evaluator_EntityOnly evaluator = new Evaluator_EntityOnly();
        /************************************************
         FORY limaye200
         *************************************************/
        evaluator.evaluate(
                /*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\entity",
                "tmp_result/limaye_entity_tm_ospd.csv",
                "tmp_result/limaye_entity_tm_ospd_missed.csv",
                true*//*

                *//*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\tm_dc_ri_ospd_nsc",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\entity",
                "tmp_result/limaye_entity_tm_ospd_nsc.csv",
                "tmp_result/limaye_entity_tm_ospd_nsc_missed.csv",
                true*//*

                *//*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_lev(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\entity",
                "tmp_result/limaye_entity_bs_sl_lev.csv",
                "tmp_result/limaye_entity_bs_sl_lev_missed.csv",
                true*//*

                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_sl_dice(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\entity",
                "tmp_result/limaye_entity_bs_sl_dice.csv",
                "tmp_result/limaye_entity_bs_sl_dice_missed.csv",
                true

                */
               /* "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\output\\baseline_nm+first(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\entity",
                "tmp_result/limaye_entity_bs_nm.csv",
                "tmp_result/limaye_entity_bs_nm_missed.csv",
                true*/

                "E:\\Data\\table_annotation\\freebase_crawl\\ti_limaye_smp-tableminer\\limaye_smp_computed",
                "E:\\Data\\table_annotation\\limayeall\\all_tables_groundtruth_freebase(regen)",
                "tmp_result/limaye_entity_smp-tableminer.csv",
                "tmp_result/limaye_entity_smp-tableminer_missed.csv",
                true

        );
        System.exit(0);

        /************************************************
         FORY imdb or musicbrainz
         *************************************************/
        evaluator.evaluate(
               /* "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_nm",
                "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                "tmp_result/imdb_entity_base_nm.csv",
                "tmp_result/imdb_entity_base_nm_missed.csv",
                false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_sl_lev",
                "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                "tmp_result/imdb_entity_base_lev.csv",
                "tmp_result/imdb_entity_base_lev_missed.csv",
                false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_tm_ospd",
                "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                "tmp_result/imdb_entity_tm_ospd.csv",
                "tmp_result/imdb_entity_tm_ospd_missed.csv",
                false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_tm_ospd_nsc",
                "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                "tmp_result/imdb_entity_tm_ospd_nsc.csv",
                "tmp_result/imdb_entity_tm_ospd_nsc_missed.csv",
                false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_sl_dice",
                "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                "tmp_result/imdb_entity_base_dice.csv",
                "tmp_result/imdb_entity_base_dice_missed.csv",
                false*/

               /*
               */


                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_nm",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                "tmp_result/mb_entity_base_nm.csv",
                "tmp_result/mb_entity_base_nm_missed.csv",
                false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_dice",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                "tmp_result/mb_entity_base_dice.csv",
                "tmp_result/mb_entity_base_dice_missed.csv",
                false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_lev",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                "tmp_result/mb_entity_base_lev.csv",
                "tmp_result/mb_entity_base_lev_missed.csv",
                false*/

                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd_nsc",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                "tmp_result/mb_entity_tm_ospd-nsc.csv",
                "tmp_result/mb_entity_tm_ospd_missed-nsc.csv",
                false


        );
        System.exit(0);

        /* Evaluator_EntityOnly evaluator = new Evaluator_EntityOnly();
        evaluator.evaluate(
                "E:\\Data\\table annotation\\freebase_crawl\\film_film\\imdb_computed_(no_ref_ent)",
                "E:\\Data\\table annotation\\freebase_crawl\\film_film\\imdb_gs_reformatted",
                "tm_eval.csv",
                "missed.csv"
        );
        System.exit(0);
        evaluator.evaluate(
                "E:\\Data\\table annotation\\freebase_crawl\\music_record_label\\musicbrainz_computed",
                "E:\\Data\\table annotation\\freebase_crawl\\music_record_label\\musicbrainz_gs_reformatted",
                "tm_eval.csv",
                "missed.csv"
        );*/
        /*Evaluator_EntityOnly evaluator = new Evaluator_EntityOnly();
        evaluator.evaluate(
                "E:\\Data\\table annotation\\freebase_crawl\\film_film\\imdb_computed_base",
                "E:\\Data\\table annotation\\freebase_crawl\\film_film\\imdb_gs_reformatted",
                "eval.csv",
                "missed.csv"
        );*/
        /*Evaluator_Generic evaluator = new Evaluator_Generic();
        evaluator.evaluate(
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_annotated(baseline)",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_gs",
                "eval.csv",
                "missed.csv"
        );*/

    }

    public void evaluate(String in_computed_folder,
                         String in_gs_folder,
                         String out_result_file,
                         String out_missed_file,
                         boolean limaye) throws IOException {
        PrintWriter p = new PrintWriter(out_result_file);
        p.println("File,ENT(0)_cp_y,gs_y,cp_n,p,r,f," +
                "ENT(1)_cp_y,gs_y,cp_n,p,r,f");


        PrintWriter out_missed_writer = new PrintWriter(out_missed_file);
        Set<String> processed = new HashSet<String>();
        int count = 0;
        for (File gsFile : new File(in_gs_folder).listFiles()) {
            Map<int[], List<List<String>>> gs_cells = null;
            String filename = gsFile.getName();

            boolean include = false;
            if (filename.endsWith(".keys")) {
                if (filter_files.size() > 0) {
                    for (String f : filter_files) {
                        if (filename.startsWith(f)) {
                            include = true;
                            break;
                        }
                    }
                } else {
                    include = true;
                }

                String entity_gs = gsFile.toString();
                gs_cells = TAnnotationKeyFileReader.readCellAnnotation(entity_gs);
                processed.add(filename);
            } else {
                continue;
            }
            if (gs_cells == null) {
                System.err.println(filename);
                continue;
            }
            if (!include) {
                continue;
            }

            count++;
            System.out.println(count+"_"+filename);

            boolean found = false;
            String gsName_revised = gsFile.getName();
            //int start= gsName.indexOf(".entity.keys"); //for musicbrainz
            if (!limaye) {
                int start = gsName_revised.indexOf(".keys"); //for imdb
                gsName_revised = start == -1 ? gsName_revised : gsName_revised.substring(0, start).trim();
                gsName_revised = gsName_revised + ".html.cell.keys";
            } else{
                int start = gsName_revised.indexOf(".cell.keys"); //for imdb
                gsName_revised = start == -1 ? gsName_revised : gsName_revised.substring(0, start).trim();
                gsName_revised = gsName_revised + ".html.cell.keys";
            }

            for (File cpFile : new File(in_computed_folder).listFiles()) {
                if (gsName_revised.equals(cpFile.getName())) {
                    found = true;

                    String cpFile_name = cpFile.toString();
                    StringBuilder line = new StringBuilder("\"" + cpFile.getPath() + "\",");

                    Map<int[], List<List<String>>> cp_cells =
                            TAnnotationKeyFileReader.readCellAnnotation(cpFile_name);

                    @SuppressWarnings("deprecation")
                    double[] cell_data_mode_0 = Evaluator_ClassOnly.compute_prf_relation_or_entity(gs_cells, cp_cells, 0);
                    line.append(appendResult(cell_data_mode_0));

                    @SuppressWarnings("deprecation")
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
        System.out.println(count);
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
