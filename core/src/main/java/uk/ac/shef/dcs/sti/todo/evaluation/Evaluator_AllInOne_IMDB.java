package uk.ac.shef.dcs.sti.todo.evaluation;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 01/04/14
 * Time: 15:50
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator_AllInOne_IMDB {
    public static void main(String[] args) throws IOException {
        Evaluator_EntityOnly ent_evaluator = new Evaluator_EntityOnly();
        Evaluator_ClassOnly_IMDB_MusicBrainz cls_evaluator = new Evaluator_ClassOnly_IMDB_MusicBrainz();
        //Evaluator_RelationOnly rel_evaluator = new Evaluator_RelationOnly();
        /************************************************
         FORY limaye200
         *************************************************/
        String method = "smp";


        if (method.equals("nm")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_nm",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                    "tmp_result/imdb_entity_base_nm.csv",
                    "tmp_result/imdb_entity_base_nm_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_nm",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs/imdb.header.keys",
                    "tmp_result/imdb_header_base_nm.csv",
                    "tmp_result/imdb_header_base_nm_missed.csv", true
            );

        } else if (method.equals("cos")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_sl_cos",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                    "tmp_result/imdb_entity_base_cos.csv",
                    "tmp_result/imdb_entity_base_cos_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_sl_cos",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs/imdb.header.keys",
                    "tmp_result/imdb_header_base_cos.csv",
                    "tmp_result/imdb_header_base_cos_missed.csv", true
            );

        } else if (method.equals("lev")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_sl_lev",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                    "tmp_result/imdb_entity_base_lev.csv",
                    "tmp_result/imdb_entity_base_lev_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_sl_lev",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs/imdb.header.keys",
                    "tmp_result/imdb_header_base_lev.csv",
                    "tmp_result/imdb_header_base_lev_missed.csv", true

            );
        } else if (method.equals("dice")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_sl_dice",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                    "tmp_result/imdb_entity_base_dice.csv",
                    "tmp_result/imdb_entity_base_dice_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_base_sl_dice",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs/imdb.header.keys",
                    "tmp_result/imdb_header_base_dice.csv",
                    "tmp_result/imdb_header_base_dice_missed.csv", true
            );

        } else if (method.equals("tm_ospd")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_tm_ospd",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                    "tmp_result/imdb_entity_tm_ospd.csv",
                    "tmp_result/imdb_entity_tm_ospd_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\output\\imdb_tm_ospd",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs/imdb.header.keys",
                    "tmp_result/imdb_header_tm_ospd.csv",
                    "tmp_result/imdb_header_tm_ospd_missed.csv", true
            );

        } else if (method.equals("tm_ospd_nsc")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\tableminer_slim\\imdb_computed",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                    "tmp_result/imdb_entity_tm_ospd_nsc.csv",
                    "tmp_result/imdb_entity_tm_ospd_nsc_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\tableminer_slim\\imdb_computed",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs/imdb.header.keys",
                    "tmp_result/imdb_header_tm_ospd_nsc.csv",
                    "tmp_result/imdb_header_tm_ospd_nsc_missed.csv", true
            );

        }else if (method.equals("smp")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_imdb_smp_tm+granularity\\imdb_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                    "tmp_result/imdb_entity_smp-tm+grn.csv",
                    "tmp_result/imdb_entity_smp-tm+grn_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_imdb_smp_tm+granularity\\imdb_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs/imdb.header.keys",
                    "tmp_result/imdb_header_smp-tm+grn.csv",
                    "tmp_result/imdb_header_smp-tm+grn_missed.csv", true
            );
        }else if (method.equals("ji")) {
            ent_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_imdb_ji\\imdb_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs\\imdb_gs(entity)_reformatted",
                    "tmp_result/imdb_entity_ji.csv",
                    "tmp_result/imdb_entity_ji_missed.csv",
                    false
            );
            cls_evaluator.evaluate(
                    "E:\\Data\\table_annotation\\freebase_crawl\\ti_imdb_ji\\imdb_computed_smp",
                    "E:\\Data\\table_annotation\\freebase_crawl\\film_film\\gs/imdb.header.keys",
                    "tmp_result/imdb_header_ji.csv",
                    "tmp_result/imdb_header_ji_missed.csv", true
            );
        }
        System.exit(0);
    }
}
