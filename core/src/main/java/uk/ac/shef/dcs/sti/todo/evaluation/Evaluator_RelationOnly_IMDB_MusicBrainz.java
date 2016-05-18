package uk.ac.shef.dcs.sti.todo.evaluation;

import uk.ac.shef.dcs.kbsearch.freebase.FreebaseQueryProxy;
import uk.ac.shef.dcs.sti.todo.TAnnotationKeyFileReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**

 */
public class Evaluator_RelationOnly_IMDB_MusicBrainz {
    public static void main(String[] args) throws IOException {

        /*Evaluator_Generic evaluator = new Evaluator_Generic();
                evaluator.evaluate(
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_annotated(disamb=0)",
                "E:\\Data\\table annotation\\corpus_analysis\\100_tables\\100_tables_gs",
                "eval.csv",
                "missed.csv"
        );*/
        /*Evaluator_RelationOnly_IMDB_MusicBrainz evaluator = new Evaluator_RelationOnly_IMDB_MusicBrainz();
        evaluator.evaluate(
                *//*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\tableminer\\tm_dc_ri",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "tm_limaye_rel.csv",
                "tm_limaye_rel_missed.csv",false,true*//*
                *//*  "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\baseline\\baseline_nm+first(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "bs_limaye_rel.csv",
                "bs_limaye_rel_missed.csv",false*//*
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\baseline\\baseline_nm+first(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "bs_nm_limaye_rel.csv",
                "bs_nm_limaye_rel_missed.csv", false, true
                *//*"E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\baseline\\baseline_sl(RI)",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\relation",
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop",
                "bs_sl_limaye_rel.csv",
                "bs_sl_limaye_rel_missed.csv",false,true*//*
        );*/


        Evaluator_RelationOnly_IMDB_MusicBrainz evaluator = new Evaluator_RelationOnly_IMDB_MusicBrainz();
        evaluator.evaluate(
                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_nm",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_nm_rel-all.csv",
                "tmp_result/mb_nm_rel_missed-all.csv",false,false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_nm",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_nm_rel-ne.csv",
                "tmp_result/mb_nm_rel_missed-ne.csv",false,true*/

               /* "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_dice",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_dice_rel-all.csv",
                "tmp_result/mb_dice_rel_missed-all.csv",false,false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_dice",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_dice_rel-ne.csv",
                "tmp_result/mb_dice_rel_missed-ne.csv",false,true
*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_lev",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_lev_rel-all.csv",
                "tmp_result/mb_lev_rel_missed-all.csv",false,false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_base_sl_lev",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_lev_rel-ne.csv",
                "tmp_result/mb_lev_rel_missed-ne.csv",false,true*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_tm_ospd_rel-all.csv",
                "tmp_result/mb_tm_ospd_rel_missed-all.csv",false,false*/

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_tm_ospd_rel-ne.csv",
                "tmp_result/mb_tm_ospd_rel_missed-ne.csv",false,true*/

                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd_nsc",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_tm_ospd-nsc_rel-all.csv",
                "tmp_result/mb_tm_ospd-nsc_rel_missed-all.csv",false,false

                /*"E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\output\\musicbrainz_tm_ospd_nsc",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.relation.keys",
                "E:\\Data\\table_annotation\\freebase_crawl\\music_record_label\\gs/musicbrainz.header.keys",
                "tmp_result/mb_tm_ospd-nsc_rel-ne.csv",
                "tmp_result/mb_tm_ospd-nsc_rel_missed-ne.csv",false,true*/

        );

    }

    public void evaluate(String in_computed_folder,
                         String in_gs_relation_file,
                         String in_gs_header_file,
                         String out_result_file,
                         String out_missed_file, boolean tolerant_mode, boolean ne_relation_only) throws IOException {
        PrintWriter p = new PrintWriter(out_result_file);
        p.println("File,REL(0)_cp_y, gs_y, cp_n, p,r,f," +
                "REL(1)_cp_y, gs_y, cp_n, p,r,f");
        //todo: this will not work
        FreebaseQueryProxy fb = null;//new FreebaseQueryProxy("D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties");

        PrintWriter out_missed_writer = new PrintWriter(out_missed_file);
        Set<String> processed = new HashSet<String>();
        List<String> main = new ArrayList<String>();
        List<String> tolerant = new ArrayList<String>();
        Map<int[], List<List<String>>> gs_binaryRels =
                TAnnotationKeyFileReader.readColumnBinaryRelationAnnotation_GS(in_gs_relation_file, main, tolerant);
        Map<Integer, List<List<String>>> gs_header =
                TAnnotationKeyFileReader.readHeaderAnnotation(in_gs_header_file, true, true);

        Map<String, Set<String>> unique_urls = new HashMap<String, Set<String>>();

        int count=0;
        for (File cpFile : new File(in_computed_folder).listFiles()) {
            if (cpFile.getName().contains("relation.keys")) {

                String cpFile_name = cpFile.toString();
                if (processed.contains(cpFile_name))
                    continue;

                count++;
                System.out.println(count + "_" + cpFile_name);

                processed.add(cpFile_name);

                StringBuilder line = new StringBuilder("\"" + cpFile.getPath() + "\",");

                Map<int[], List<List<String>>> cp_binaryRels =
                        TAnnotationKeyFileReader.readColumnBinaryRelationAnnotation_CP(cpFile_name);

                for (int[] i : cp_binaryRels.keySet()) {
                    List<List<String>> ll = cp_binaryRels.get(i);
                    Set<String> unique = unique_urls.get(i);
                    unique = unique == null ? new HashSet<String>() : unique;
                    for (List<String> l : ll)
                        unique.addAll(l);
                    String key =i[0]+","+i[1];
                    unique_urls.put(key, unique);
                }
                double[] relation_data_mode_0 =
                        Evaluator_RelationOnly.compute_prf_relation(gs_binaryRels, cp_binaryRels, main, gs_header, 0, ne_relation_only);
                line.append(appendResult(relation_data_mode_0));

                double[] relation_data_mode_1 =
                        Evaluator_RelationOnly.compute_prf_relation(gs_binaryRels, cp_binaryRels, main, gs_header, 1, ne_relation_only);
                line.append(appendResult(relation_data_mode_1));

                p.println(line.toString());
            }
        }


        out_missed_writer.close();
        p.close();

        /*for (String i : unique_urls.keySet()) {

            List<String> unique_sorted = new ArrayList<String>(unique_urls.get(i));
            Collections.sort(unique_sorted);
            System.out.println(">>>" + i);
            for (String s : unique_sorted) {
                if (s.startsWith("/m/")) {
                    try {
                        List<String[]> facts = fb.topicapi_getAttributesOfTopicID(s, "/type/object/name");
                        ;
                        if (facts.size() > 0)
                            s = s + ":" + facts.get(0)[1];
                    } catch (Exception e) {

                    }
                }
                System.out.println(s);
            }
        }*/
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
