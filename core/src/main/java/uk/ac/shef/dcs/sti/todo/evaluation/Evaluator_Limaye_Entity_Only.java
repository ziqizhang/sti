package uk.ac.shef.dcs.sti.todo.evaluation;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 01/04/14
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator_Limaye_Entity_Only {
    public static void main(String[] args) throws IOException {
        Evaluator_EntityOnly ent_evaluator = new Evaluator_EntityOnly();
        Evaluator_ClassOnly cls_evaluator = new Evaluator_ClassOnly();
        Evaluator_RelationOnly rel_evaluator = new Evaluator_RelationOnly();
        /************************************************
         FORY limaye200
         *************************************************/
        String method = "smp";


        if (method.equals("nm")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\limaye_sample\\all\\baseline_nm+first(RI)",
                    "E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(regen)",
                    //"E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(limaye_original)",
                    "tmp_result/limaye_entity_bs_nm.csv",
                    "tmp_result/limaye_entity_bs_nm_missed.csv",
                    true
            );
        } else if (method.equals("cos")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\limaye_sample\\all\\old_baseline_sl_cos(RI)",
                    "E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(limaye_original)",
                    "tmp_result/limaye_entity_bs_cos.csv",
                    "tmp_result/limaye_entity_bs_cos_missed.csv",
                    true
            );

        } else if (method.equals("lev")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\limaye_sample\\all\\old_baseline_sl_lev(RI)",
                    "E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(limaye_original)",
                    "tmp_result/limaye_entity_bs_lev.csv",
                    "tmp_result/limaye_entity_bs_lev_missed.csv",
                    true
            );
        } else if (method.equals("dice")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\limaye_sample\\all\\old_baseline_sl_dice(RI)",
                    "E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(limaye_original)",
                    "tmp_result/limaye_entity_bs_dice.csv",
                    "tmp_result/limaye_entity_bs_dice_missed.csv",
                    true
            );

        } else if (method.equals("tm_ospd")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\limaye_sample\\all\\tm_dc_ri_ospd",
                    "E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(regen)",
                    "tmp_result/limaye_entity_tm_ospd.csv",
                    "tmp_result/limaye_entity_tm_ospd_missed.csv",
                    true
            );

        } else if (method.equals("tm_ospd_nsc")) {
            ent_evaluator.evaluate(
                    //"E:\\Data\\table_annotation\\limaye_sample\\all\\old_tm_dc_ri_ospd_nsc(sqrtx2,ctxu1,normI)",
                    //"E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(limaye_original)",

                    "E:\\Data\\table_annotation\\tableminer_df\\limayeall\\baseline\\limaye_df_random_ospd",
                    "E:\\Data\\table_annotation\\limayeall\\all_tables_groundtruth_freebase(regen)",
                    "tmp_result/limaye_entity_tm_ospd_nsc.csv",
                    "tmp_result/limaye_entity_tm_ospd_nsc_missed.csv",
                    true
            );

        } else if (method.equals("smp")) {
            ent_evaluator.evaluate(
                    //"E:\\Data\\table_annotation\\limaye_sample\\all\\old_tm_dc_ri_ospd_nsc(sqrtx2,ctxu1,normI)",
                    //"E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(limaye_original)",

                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_limaye_smp-tableminer+grn\\limaye_smp_computed",
                    "E:\\Data\\table_annotation\\limayeall\\all_tables_groundtruth_freebase(regen)",
                    "tmp_result/limaye_entity_smp-tm+grn.csv",
                    "tmp_result/limaye_entity_smp-tm+grn_missed.csv",
                    true
            );
            System.exit(0);
        }    else if (method.equals("ji")) {
            ent_evaluator.evaluate(
                    //"E:\\Data\\table_annotation\\limaye_sample\\all\\old_tm_dc_ri_ospd_nsc(sqrtx2,ctxu1,normI)",
                    //"E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(limaye_original)",

                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_limaye_ji\\limaye_smp_computed",
                    "E:\\Data\\table_annotation\\limayeall\\all_tables_groundtruth_freebase(regen)",
                    "tmp_result/limaye_entity_ji.csv",
                    "tmp_result/limaye_entity_ji_missed.csv",
                    true
            );}
    }
}
