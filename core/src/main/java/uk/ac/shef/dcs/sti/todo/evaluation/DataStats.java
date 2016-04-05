package uk.ac.shef.dcs.sti.todo.evaluation;

import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 23/02/14
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
public class DataStats {

    public static void main(String[] args) throws IOException {
        /*calculate_average_query_time("D:\\Work\\lodie/lodie.LOG");
        System.exit(0);*/

        /////////////////////////////////////////////// BING SEARCH CONVERGENCE ////////////////////////////////////////////////////////////
        /*calculate_ws_converge("E:\\Data\\table_annotation\\efficiency_analysis_data\\SUBCOL_FINAL_limaye200_convergence.txt",
                "D:\\Work\\lodie\\tmp_result/convergence_ws_limaye200.csv");
        calculate_ws_converge("E:\\Data\\table_annotation\\efficiency_analysis_data\\SUBCOL_FINAL_limayeall_convergence.txt",
                "D:\\Work\\lodie\\tmp_result/convergence_ws_limayeall.csv");
        calculate_ws_converge("E:\\Data\\table_annotation\\efficiency_analysis_data\\SUBCOL_FINAL_musicbrainz_convergence.txt",
                "D:\\Work\\lodie\\tmp_result/convergence_ws_musicbrainz.csv");

        System.exit(0);*/

        ///////////////////////////////////////////////---DONE--- i-inf CONVERGENCE in column interpretation////////////////////////////////////////////////////////////
        /*calculate_converge_column_interpretation(
                "E:\\Data\\table_annotation\\limaye200\\200_tables_regen\\raw",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/random_nostop.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/random_nostop.csv");
        calculate_converge_column_interpretation(
                "E:\\Data\\table_annotation\\limaye200\\200_tables_regen\\raw",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/nonempty.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/nonempty.csv");
        calculate_converge_column_interpretation(
                "E:\\Data\\table_annotation\\limaye200\\200_tables_regen\\raw",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/tokens.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/tokens.csv");
        calculate_converge_column_interpretation(
                "E:\\Data\\table_annotation\\limaye200\\200_tables_regen\\raw",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/namelength.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/namelength.csv");
        calculate_converge_column_interpretation(
                "E:\\Data\\table_annotation\\limaye200\\200_tables_regen\\raw",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/random.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/random.csv");
        calculate_converge_column_interpretation(
                "E:\\Data\\table_annotation\\limaye200\\200_tables_regen\\raw",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/ospd_random.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200/ospd_random.csv");
        System.exit(0);


        calculate_converge_column_interpretation(
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/random.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/random.csv");
        calculate_converge_column_interpretation(
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/nonempty.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/nonempty.csv");
        calculate_converge_column_interpretation(
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/tokens.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/tokens.csv");
        calculate_converge_column_interpretation(
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/namelength.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/namelength.csv");
        calculate_converge_column_interpretation(
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/combined.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/combined.csv");
        calculate_converge_column_interpretation(
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/ospd_random.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_baseline\\limaye200/ospd_random.csv");
        System.exit(0);



        calculate_converge_column_interpretation(
                "E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen",
                "E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limaye200_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/learning_phase_converengece-limaye200-tm.csv");
        calculate_converge_column_interpretation(
                "limaye/all",
                "E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limayeall_tm_sysout.LOG",
                "D:\\Work\\lodie\\tmp_result/learning_phase_converengece-limayeall-tm.csv");
        calculate_converge_column_interpretation(
                "musicbrainz_raw/",
                "E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_mb_tm_sysout.LOG",
                "D:\\Work\\lodie\\tmp_result/learning_phase_converengece-musicbrainz-tm.csv");
        calculate_converge_column_interpretation(
                "imdb_raw/",
                "E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_imdb_tm_sysout.LOG",
                "D:\\Work\\lodie\\tmp_result/learning_phase_converengece-imdb-tm.csv");
        System.exit(0);*/


        /////////////////////////////////////////////// ---done--- Candidate counting (overall, runPreliminaryColumnClassifier-consolidate only, new entity at update, all entity at update) ////////////////////////////////////////////////////////////
        calculate_entity_candidate_savings(
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\random_nostop.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\candidate_savings_random_nostop.csv");
        calculate_entity_candidate_savings(
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\random.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\candidate_savings_random.csv");
        calculate_entity_candidate_savings(
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\ospd_random.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\candidate_savings_ospd_random.csv");
        calculate_entity_candidate_savings(
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\nonempty.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\candidate_savings_nonempty.csv");
        calculate_entity_candidate_savings(
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\tokens.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\candidate_savings_tokens.csv");
        calculate_entity_candidate_savings(
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\namelength.LOG",
                "D:\\Work\\lodie\\tmp_result\\ospd_tm_iswc\\limaye200\\candidate_savings_namelength.csv");



        System.exit(0);


        calculate_entity_candidate_savings(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_limaye200_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result\\entity_reduction_overall-limaye200-tm.csv");
        calculate_entity_candidate_savings_update_only(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_limaye200_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result\\entity_reduction_learning_consol-limaye200-tm.csv");
        calculate_new_entity_candidate_at_update(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_limaye200_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/entity_new_in_update-limaye200-tm.csv"
        );
        calculate_old_entity_candidate_at_update(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_limaye200_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/entity_old_in_update-limaye200-tm.csv"
        );

        calculate_entity_candidate_savings(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_limayeall_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result\\entity_reduction_overall-limayeall-tm.csv");
        calculate_entity_candidate_savings_update_only(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_limayeall_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result\\entity_reduction_learning_consol-limayeall-tm.csv");
        calculate_new_entity_candidate_at_update(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_limayeall_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/entity_new_in_update-limayeall-tm.csv"
        );
        calculate_old_entity_candidate_at_update(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_limayeall_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/entity_old_in_update-limayeall-tm.csv"
        );

        calculate_entity_candidate_savings(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_imdb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result\\entity_reduction_overall-imdb-tm.csv");
        calculate_entity_candidate_savings_update_only(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_imdb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result\\entity_reduction_learning_consol-imdb-tm.csv");
        calculate_new_entity_candidate_at_update(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_imdb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/entity_new_in_update-imdb-tm.csv"
        );
        calculate_old_entity_candidate_at_update(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_imdb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/entity_old_in_update-imdb-tm.csv"
        );

        calculate_entity_candidate_savings(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_mb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result\\entity_reduction_overall-musicbrainz-tm.csv");
        calculate_entity_candidate_savings_update_only(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_mb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result\\entity_reduction_learning_consol-musicbrainz-tm.csv");
        calculate_new_entity_candidate_at_update(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_mb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/entity_new_in_update-musicbrainz-tm.csv"
        );
        calculate_old_entity_candidate_at_update(
                "E:\\Data\\table_annotation\\efficiency_analysis_data\\FINAL_mb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/entity_old_in_update-musicbrainz-tm.csv"
        );
        System.exit(0);


        /////////////////////////////////////////// -----------done ------------- candidate counting baseline //////////////////////////////////
        /*calculate_entity_candidate_savings_baseline("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limaye200_bs_sl_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/candidate_entity_limaye200_bs_sl.csv");
        calculate_entity_candidate_savings_baseline("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limayeall_bs_sl_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/candidate_entity_limayeall_bs_sl.csv");
        calculate_entity_candidate_savings_baseline("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_imdb_bs_sl_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/candidate_entity_imdb_bs_sl.csv");
        calculate_entity_candidate_savings_baseline("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_mb_bs_sl_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/candidate_entity_mb_bs_sl.csv");
        System.exit(0);*/

        /////////////////////////////////////////// UNIQUE ENTITIES processed
        /*calculate_entity_candidate_unique_for_calculating_running_time
                ("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limaye200_bs_sl_sysout.txt");
        calculate_entity_candidate_unique_for_calculating_running_time
                ("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limaye200_tm_sysout.txt");*/
        calculate_entity_candidate_unique_for_calculating_running_time
                ("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limayeall_bs_sl_sysout.txt");
        /*calculate_entity_candidate_unique_for_calculating_running_time
                ("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limayeall_tm_sysout.txt");
        calculate_entity_candidate_unique_for_calculating_running_time
                ("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_mb_tm_sysout.txt");
        calculate_entity_candidate_unique_for_calculating_running_time
                ("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_mb_base_sl_sysout.txt");
        calculate_entity_candidate_unique_for_calculating_running_time
                ("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_imdb_tm_sysout.txt");
        calculate_entity_candidate_unique_for_calculating_running_time
                ("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_imdb_base_sl_sysout.txt");*/
        System.exit(0);


        /////////////////////////////////////////// ----- done ----- iterations at update
        calculate_iterations_in_update("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limaye200_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/iterations_in_update-limaye200-tm.csv");
        calculate_iterations_in_update("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_limayeall_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/iterations_in_update-limayeall-tm.csv");
        calculate_iterations_in_update("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_imdb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/iterations_in_update-imdb-tm.csv");
        calculate_iterations_in_update("E:\\Data\\table_annotation\\efficiency_analysis_data/FINAL_mb_tm_sysout.txt",
                "D:\\Work\\lodie\\tmp_result/iterations_in_update-musicbrainz-tm.csv");

        System.exit(0);


    }

    private static void count_NE_cells(String in_system_out_file) throws IOException {
        List<String> lines = FileUtils.readList(in_system_out_file, false);
        boolean stopCounting = false;

        int count = 0;

        int currentCol = -1;
        int totalCol = 0;
        int maxRow = 0;
        for (String l : lines) {

            if (l.contains("_E:\\")) {
                //System.out.println(l);
                System.out.println(totalCol + "," + maxRow + "," + totalCol * maxRow);

                currentCol = -1;
                totalCol = 0;
                maxRow = 0;
                //consolidate numbers
            }

            if (l.contains(">> Column=")) {
                String col = l.split("=")[1].trim();
                int c = Integer.valueOf(col) + 1;
                if (c != currentCol)
                    totalCol++;
            }
            if (l.contains(" position at ")) {
                int start_pos = l.indexOf("(");
                String nl = l.substring(start_pos + 1);
                int row = Integer.valueOf(nl.split(",")[0].trim());
                if (row > maxRow)
                    maxRow = row;
            }

            if (l.contains("UPDATE ITERATION "))
                stopCounting = true;
            if (l.contains("UPDATE STABLIZED AFTER"))
                stopCounting = false;

            if (!stopCounting) {
                if (l.contains(" position at ("))
                    count++;
            }
        }
        System.out.println(totalCol + "," + maxRow + "," + totalCol * maxRow);
        System.out.println(count);
    }

    private static void calculate_new_entity_candidate_at_update(String tableminer_convergence_log,
                                                                 String outfile) throws IOException {
        PrintWriter p = new PrintWriter(outfile);
        p.println("Initial, Reduced to");

        List<String> lines = FileUtils.readList(tableminer_convergence_log, false);

        boolean count = false;

        int initial = 0, reduced = 0;
        for (String l : lines) {
            if (l.contains("BACKWARD UPDATE...")) {
                count = true;
                continue;
            }
            if (l.contains("FORWARD LEARNING")) {
                count = false;
                continue;
            }

            if (count && l.contains("(QUERY_KB:")) {
                String[] parts = l.trim().split(":");
                String value = parts[1].trim();
                String[] vs = value.split("\\s+");
                initial = Integer.valueOf(vs[0].trim());
                reduced = Integer.valueOf(vs[2].trim());
                if (reduced == 0) {
                    initial = 0;
                }

                continue;
            }

            if (count && l.contains("ALREADY BUILT FOR")) {
                String[] parts = l.trim().split("=");
                int already = Integer.valueOf(parts[1].trim());
                int todo = reduced - already;
                if (todo != 0) {
                    p.println(todo);
                }
                initial = 0;
                reduced = 0;
            }
        }


        p.close();
    }

    private static void calculate_old_entity_candidate_at_update(String tableminer_convergence_log,
                                                                 String outfile) throws IOException {
        PrintWriter p = new PrintWriter(outfile);
        p.println("Initial, Reduced to");

        List<String> lines = FileUtils.readList(tableminer_convergence_log, false);

        boolean count = false;

        int initial = 0, reduced = 0;
        for (String l : lines) {
            if (l.contains("BACKWARD UPDATE...")) {
                count = true;
                continue;
            }
            if (l.contains("FORWARD LEARNING")) {
                count = false;
                continue;
            }

            if (count && l.contains("(QUERY_KB:")) {
                String[] parts = l.trim().split(":");
                String value = parts[1].trim();
                String[] vs = value.split("\\s+");
                initial = Integer.valueOf(vs[0].trim());
                reduced = Integer.valueOf(vs[2].trim());
                if (reduced == 0) {
                    continue;
                }
                p.println(reduced);
                continue;
            }
        }


        p.close();
    }

    private static void calculate_entity_candidate_savings(String tableminer_convergence_log,
                                                           String outfile) throws IOException {
        PrintWriter p = new PrintWriter(outfile);
        p.println("Initial, Reduced to");

        List<String> lines = FileUtils.readList(tableminer_convergence_log, false);

        boolean count = false;
        int countline=-1;
        for (String l : lines) {
            countline++;
            if (l.contains("BACKWARD UPDATE...")) {
                count = false;
                continue;
            }
            if (l.contains("FORWARD LEARNING")) {
                count = true;
                continue;
            }

            if (count && l.contains("(QUERY_KB:")) {
                String[] parts = l.trim().split(":");
                String value = parts[1].trim();
                String[] vs = value.split("\\s+");
                String initial = vs[0].trim();
                String reduced = vs[2].trim();
                if(reduced.contains("|"))
                    reduced=reduced.split("\\|",2)[0].trim();
                if (reduced.equals("0"))
                    continue;

                String nextline = lines.get(countline+1);
                int repeat=1;
                if(nextline.contains(">> Disambiguation")&&nextline.contains("position at")){
                    int start= nextline.indexOf("([")+2;
                    int end = nextline.indexOf("],");
                    nextline=nextline.substring(start,end).trim();
                    repeat=nextline.split(",").length;
                }

               // for(int i=0; i<repeat; i++)
                    p.println(initial + "," + reduced);
            }
        }


        p.close();
    }


    private static void calculate_entity_candidate_savings_baseline(String tableminer_convergence_log,
                                                                    String outfile) throws IOException {
        PrintWriter p = new PrintWriter(outfile);
        p.println("Initial, Reduced to");

        List<String> lines = FileUtils.readList(tableminer_convergence_log, false);


        for (String l : lines) {


            if (l.contains("(QUERY_KB:")) {
                String[] parts = l.trim().split(":");
                String value = parts[1].trim();
                String[] vs = value.split("\\s+");
                String initial = vs[0].trim();
                String reduced = vs[2].trim();
                if (reduced.equals("0"))
                    continue;

                p.println(initial + "," + reduced);
            }
        }


        p.close();
    }


    private static void calculate_entity_candidate_unique_for_calculating_running_time(String tableminer_convergence_log) throws IOException {

        List<String> lines = FileUtils.readList(tableminer_convergence_log, false);

        Set<String> all = new HashSet<String>();
        for (String l : lines) {


            if (l.contains("(QUERY_KB:")) {
                String[] parts = l.trim().split(":");
                String value = parts[1].trim();
                String[] vs = value.split("\\s+");
                String initial = vs[0].trim();
                String reduced = vs[2].trim();
                if (reduced.equals("0"))
                    continue;

                if (reduced.contains("|")) {
                    try {
                        String ids = reduced.split("\\|")[1].trim();
                        for (String id : ids.split(",")) {
                            if (id.trim().length() > 0)
                                all.add(id.trim());
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                //p.println(initial + "," + reduced);
            }
        }
        System.out.println(all.size());
    }

    private static void calculate_entity_candidate_savings_update_only(String tableminer_convergence_log,
                                                                       String outfile) throws IOException {
        PrintWriter p = new PrintWriter(outfile);
        p.println("Initial, Reduced to");

        List<String> lines = FileUtils.readList(tableminer_convergence_log, false);

        boolean count = false;
        for (String l : lines) {
            if (l.contains("BACKWARD UPDATE...")) {
                count = false;
                continue;
            }
            if (l.contains("LEARN (Consolidate) begins")) {
                count = true;
                continue;
            }

            if (count && l.contains("(QUERY_KB:")) {
                String[] parts = l.trim().split(":");
                String value = parts[1].trim();
                String[] vs = value.split("\\s+");
                String initial = vs[0].trim();
                String reduced = vs[2].trim();
                if (reduced.equals("0"))
                    continue;

                p.println(initial + "," + reduced);
            }
        }


        p.close();
    }

    private static void calculate_average_query_time(String logfile) throws IOException {
        List<String> lines = FileUtils.readList(logfile, false);
        long totalTime = 0;
        int countQueries = 0;
        for(String l: lines){
            if(l.contains("QueryFreebase")){
                int start =l.lastIndexOf(":")+1;
                long time = Long.valueOf(l.substring(start).trim());
                totalTime=totalTime+time;
                countQueries++;
            }
        }
        System.out.println(totalTime+","+countQueries);
    }

    private static void calculate_iterations_in_update(String tableminer_convergence_log,
                                                       String outfile) throws IOException {
        PrintWriter p = new PrintWriter(outfile);

        List<String> lines = FileUtils.readList(tableminer_convergence_log, false);

        boolean count = false;
        for (String l : lines) {
            if (l.contains("UPDATE STABLIZED AFTER")) {
                String[] tokens = l.trim().split("\\s+");
                int index = 0;
                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].equals("AFTER"))
                        index = i + 1;
                }
                String itr = tokens[index].trim();
                p.println(Integer.valueOf(itr));
            }
        }
        p.close();

    }

    private static void calculate_converge_column_interpretation
            (String dataset,
             String tableminer_convergence_log,
             String outfile) throws IOException {
        PrintWriter p = new PrintWriter(outfile);
        p.println("convergence, max, savings");

        List<String> lines = FileUtils.readList(tableminer_convergence_log, false);
        boolean started = false;
        for (String l : lines) {
            if (l.contains(dataset)) {
                //new file
                started = false;
                continue;
            }
            if (l.contains("FORWARD LEARNING...")) {
                started = true;
                continue;
            }

            if (started) {
                if (l.contains("Convergence iteration")) {
                    String[] parts = l.trim().split(",");
                    String append = "";
                    for (String value : parts) {
                        value = value.trim();
                        if (value.length() == 0 || value.indexOf("=") == -1) continue;
                        String split = value.split("=")[1];
                        append += split + ",";
                    }
                    p.println(append);
                }
            }
        }


        p.close();
    }

    public static int calculate_total_queries_issued_baseline(String sysoutFile) throws IOException {
        int sum = 0;
        List<String> lines = FileUtils.readList(sysoutFile, false);
        for (String l : lines) {
            if (l.contains("(QUERY_KB:")) {
                sum++;
                int index = l.indexOf("=>");
                l = l.substring(index + 2).trim();
                int candidates = Integer.valueOf(l);
                sum = sum + candidates;
            }
        }
        System.out.println(sum);
        return sum;
    }

    public static int calculate_total_queries_issued_tableminer(String sysoutFile) throws IOException {
        int sum = 0;
        List<String> lines = FileUtils.readList(sysoutFile, false);

        boolean count = false;
        for (String l : lines) {
            if (l.contains("FORWARD LEARNING")) {
                count = true;
                continue;
            }

            if (count && l.contains("(QUERY_KB:")) {
                sum++;
                int index = l.indexOf("=>");
                l = l.substring(index + 2).trim();
                int candidates = Integer.valueOf(l);
                sum = sum + candidates;
            }
        }
        System.out.println(sum);
        return sum;
    }


    //convergence time
    public static void calculate_ws_converge(String logFile, String outFile) throws IOException {
        PrintWriter p = new PrintWriter(outFile);
        p.println("convergence, max, savings");
        List<String> lines = FileUtils.readList(logFile, false);
        for (String l : lines) {
            if (l.contains("Convergence iteration")) {
                String[] parts = l.trim().split(",");
                String append = "";
                for (String value : parts) {
                    value = value.trim();
                    if (value.length() == 0 || value.indexOf("=") == -1) continue;
                    String split = value.split("=")[1];
                    append += split + ",";
                }
                p.println(append);
            }
        }
        p.close();
    }

    /*  public static void calcuate_update_stats_until_convergence(String logFile, String outputFile) throws IOException {
        PrintWriter p = new PrintWriter(outputFile);
        List<String> lines = FileUtils.readList(logFile, false);

        String text_to_search = "UPDATE STABLIZED AFTER ";
        for (String l : lines) {
            if (l.contains(text_to_search)) {
                int start = l.indexOf(text_to_search) + text_to_search.length();
                l = l.substring(start).trim();
                String iterations = l.split("\\s+")[0].trim();
                p.println(iterations);
            }
        }
        p.close();


    }*/


}
