package uk.ac.shef.dcs.sti.todo.evaluation;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 01/04/14
 * Time: 16:04
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator_AllInOne_MB {
    public static void main(String[] args) throws IOException {
        Evaluator_EntityOnly ent_evaluator = new Evaluator_EntityOnly();
        Evaluator_ClassOnly_IMDB_MusicBrainz cls_evaluator = new Evaluator_ClassOnly_IMDB_MusicBrainz();
        Evaluator_RelationOnly_IMDB_MusicBrainz rel_evaluator = new Evaluator_RelationOnly_IMDB_MusicBrainz();
        /************************************************
         FORY limaye200
         *************************************************/
        String method = "smp";

        if (method.equals("nm")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_nm",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                    "tmp_result/mb_entity_base_nm.csv",
                    "tmp_result/mb_entity_base_nm_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_nm",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_base_nm-all.csv",
                    "tmp_result/mb_header_base_nm_missed-all.csv", false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_nm",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_base_nm-ne.csv",
                    "tmp_result/mb_header_base_nm_missed-ne.csv", true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_nm",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_nm_rel-all.csv",
                    "tmp_result/mb_nm_rel_missed-all.csv",false,false
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_nm",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_nm_rel-ne.csv",
                    "tmp_result/mb_nm_rel_missed-ne.csv",false,true
            );

        } else if (method.equals("cos")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_cos",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                    "tmp_result/mb_entity_base_cos.csv",
                    "tmp_result/mb_entity_base_cos_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_cos",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_base_cos-all.csv",
                    "tmp_result/mb_header_base_cos_missed-all.csv", false

            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_cos",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_base_cos-ne.csv",
                    "tmp_result/mb_header_base_cos_missed-ne.csv", true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_cos",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_cos_rel-all.csv",
                    "tmp_result/mb_cos_rel_missed-all.csv",false,false
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_cos",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_cos_rel-ne.csv",
                    "tmp_result/mb_cos_rel_missed-ne.csv",false,true
            );
        } else if (method.equals("lev")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_lev",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                    "tmp_result/mb_entity_base_lev.csv",
                    "tmp_result/mb_entity_base_lev_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_lev",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_base_lev-all.csv",
                    "tmp_result/mb_header_base_lev_missed-all.csv", false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_lev",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_base_lev-ne.csv",
                    "tmp_result/mb_header_base_lev_missed-ne.csv", true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_lev",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_lev_rel-ne.csv",
                    "tmp_result/mb_lev_rel_missed-ne.csv",false,true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_lev",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_lev_rel-all.csv",
                    "tmp_result/mb_lev_rel_missed-all.csv",false,false
            );
        } else if (method.equals("dice")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_dice",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                    "tmp_result/mb_entity_base_dice.csv",
                    "tmp_result/mb_entity_base_dice_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_dice",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_base_dice-all.csv",
                    "tmp_result/mb_header_base_dice_missed-all.csv", false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_dice",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_base_dice-ne.csv",
                    "tmp_result/mb_header_base_dice_missed-ne.csv", true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_dice",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_dice_rel-all.csv",
                    "tmp_result/mb_dice_rel_missed-all.csv",false,false
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_dice",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_dice_rel-ne.csv",
                    "tmp_result/mb_dice_rel_missed-ne.csv",false,true
            );

        } else if (method.equals("tm_ospd")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                    "tmp_result/mb_entity_tm_ospd.csv",
                    "tmp_result/mb_entity_tm_ospd_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_tm_ospd-all.csv",
                    "tmp_result/mb_header_tm_ospd_missed-all.csv", false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_tm_ospd-ne.csv",
                    "tmp_result/mb_header_tm_ospd_missed-ne.csv", true

            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_tm_ospd_rel-ne.csv",
                    "tmp_result/mb_tm_ospd_rel_missed-ne.csv",false,true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_tm_ospd_rel-all.csv",
                    "tmp_result/mb_tm_ospd_rel_missed-all.csv",false,false
            );
        } else if (method.equals("tm_ospd_nsc")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\tableminer_df\\mb_df_combined",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                    "tmp_result/mb_entity_tm_ospd-nsc.csv",
                    "tmp_result/mb_entity_tm_ospd_missed-nsc.csv",
                    false
            );
            /*cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\tableminer_df\\mb_df_random",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_header_tm_ospd_nsc-all.csv",
                    "tmp_result/mb_header_tm_ospd_missed_nsc-all.csv", false
            );*/
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\tableminer_df\\mb_df_combined",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_tm_ospd_nsc-ne.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_tm_ospd_missed_nsc-ne.csv", true
            );
            /*rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\tableminer_df\\mb_df_random",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_tm_ospd-nsc_rel-ne.csv",
                    "tmp_result/mb_tm_ospd-nsc_rel_missed-ne.csv",false,true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\tableminer_df\\mb_df_randomd",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "tmp_result/mb_tm_ospd-nsc_rel-all.csv",
                    "tmp_result/mb_tm_ospd-nsc_rel_missed-all.csv",false,false
            );*/
        }else if (method.equals("smp")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_smp_tm+granularity\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                    "D:\\Work\\sti\\core\\tmp_result/mb_entity_smp-tm+grn.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_entity_smp-tm+grn_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_smp_tm+granularity\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_smp-tm+grn-all.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_smp-tm+grn-missed-all.csv", false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_smp_tm+granularity\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_smp-tm+grn-ne.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_smp-tm+grn-missed-ne.csv", true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_smp_tm+granularity\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_rel_smp-tm+grn-ne.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_rel_smp-tm+grn-ne_missed.csv",false,true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_smp_tm+granularity\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_rel_smp-tm+grn-all.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_rel_smp-tm+grn_missed-all.csv",false,false
            );
        }
        else if (method.equals("ji")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_ji\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs\\musicbrainz_gs(entity)_reformatted",
                    "D:\\Work\\sti\\core\\tmp_result/mb_entity_ji.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_entity_ji_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_ji\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_ji-all.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_ji-missed-all.csv", false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_ji\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_ji-ne.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_header_ji-missed-ne.csv", true
            );
            /*rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_smp_tableminer\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_rel_smp-tm-ne.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_rel_smp-tm_missed-ne.csv",false,true
            );
            rel_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_mb_smp_tableminer\\musicbrainz_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                    "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                    "D:\\Work\\sti\\core\\tmp_result/mb_rel_smp-tm-all.csv",
                    "D:\\Work\\sti\\core\\tmp_result/mb_rel_smp-tm_missed-all.csv",false,false
            );*/
        }

        System.exit(0);
    }
}
