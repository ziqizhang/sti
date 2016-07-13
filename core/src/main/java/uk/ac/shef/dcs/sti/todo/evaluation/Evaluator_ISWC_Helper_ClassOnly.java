package uk.ac.shef.dcs.sti.todo.evaluation;

import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.todo.TAnnotationKeyFileReader;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseQueryProxy;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 *
 */
public class Evaluator_ISWC_Helper_ClassOnly {


    public static void main(String[] args) throws IOException {

        Evaluator_ISWC_Helper_ClassOnly evaluator = new Evaluator_ISWC_Helper_ClassOnly();
        /*evaluator.evaluate(
                "E:\\Data\\lodie_corpus_consolidated\\iswc_output_selected\\goodreads-test-3000",
                "E:\\Data\\lodie_corpus_consolidated\\iswc_output_selected/goodreads.header.keys",
                "tmp_result/mb_header_tm_ospd_nsc-all.csv",
                "tmp_result/mb_header_tm_ospd_missed_nsc-all.csv", false

        );
*/
        evaluator.evaluate(
                "E:\\Data\\lodie_corpus_consolidated\\LODIE_data\\ISWCdataset\\COMPUTED\\priority_2_music_reverbnation\\reverbnation-test-1216",
                "E:\\Data\\lodie_corpus_consolidated\\iswc_output_selected/goodreads.header.keys",
                "tmp_result/mb_header_tm_ospd_nsc-all.csv",
                "tmp_result/mb_header_tm_ospd_missed_nsc-all.csv", false

        );

    }

    public void evaluate(String in_computed_folder,
                         String in_gs_file,
                         String out_result_file,
                         String out_missed_file, boolean gs_NE_only) throws IOException {
        //todo:this will not work!
        FreebaseQueryProxy fb = null;//new FreebaseQueryProxy("D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties");
        PrintWriter p = new PrintWriter(out_result_file);
        p.println("File,HEADER(0)_cp_y, gs_y, cp_n, p,r,f," +
                "HEADER(1)_cp_y, gs_y, cp_n, p,r,f");


        PrintWriter out_missed_writer = new PrintWriter(out_missed_file);
        Set<String> processed = new HashSet<String>();
        Map<Integer, List<List<String>>> gs_headers = TAnnotationKeyFileReader.readHeaderAnnotation(in_gs_file, true, gs_NE_only);

        Map<Integer, Set<String>> unique_urls = new HashMap<Integer, Set<String>>();

        int count = 0;
        for (File cpFile : new File(in_computed_folder).listFiles()) {

            String filename = cpFile.getName();
            if (processed.contains(filename))
                continue;

            if (filename.contains(".header.keys")) {
                int dothtml = filename.lastIndexOf(".html");
                if (dothtml == -1)
                    dothtml = filename.lastIndexOf(".htm");
                if (dothtml == -1) {
                    System.err.println(filename);
                    continue;
                }
                int end = dothtml + 5;

                count++;
                filename = filename.substring(0, end).trim();
                System.out.println(count + "_" + filename);

                processed.add(filename);
                String header_cp = cpFile.getParent() + "/" + filename + ".header.keys";
                processed.add(header_cp);

                StringBuilder line = new StringBuilder("\"" + cpFile.getPath() + "\",");

                Map<Integer, List<List<String>>> cp_headers =
                        TAnnotationKeyFileReader.readHeaderAnnotation(header_cp, false, gs_NE_only);
                for (Integer i : cp_headers.keySet()) {
                    List<List<String>> ll = cp_headers.get(i);
                    Set<String> unique = unique_urls.get(i);
                    unique = unique == null ? new HashSet<String>() : unique;
                    for (List<String> l : ll)
                        unique.addAll(l);
                    /* if(unique.contains("/m/0174nj"))
                    System.out.println();*/
                    unique_urls.put(i, unique);
                }


                double[] header_data_mode_0 = Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 0);
                line.append(appendResult(header_data_mode_0));

                double[] header_data_mode_1 = Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 1);
                line.append(appendResult(header_data_mode_1));

                p.println(line.toString());
                //break;

            } else {
                continue;
            }


        }
        out_missed_writer.close();
        p.close();

        for (Integer i : unique_urls.keySet()) {

            List<String> unique_sorted = new ArrayList<String>(unique_urls.get(i));
            Collections.sort(unique_sorted);
            System.out.println(">>>" + i);
            for (String s : unique_sorted) {
                if (s.startsWith("/m/")) {
                    try {
                        List<Attribute> facts = fb.topicapi_getAttributesOfTopicID(s, "/type/object/name");
                        ;
                        if (facts.size() > 0)
                            s = s + ":" + facts.get(0).getValue();
                    } catch (Exception e) {

                    }
                }
                System.out.println(s);
            }
        }
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


    //return double[] 0-correct by cp; 1-correct by gs; 2-wrong by cp (i.e., missed)


}
