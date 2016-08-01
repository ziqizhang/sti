package uk.ac.shef.dcs.sti.todo.evaluation;

import uk.ac.shef.dcs.sti.todo.TAnnotationKeyFileReader;
import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 */
public class Evaluator_Generic_Classification_ignoreUpdateIterations {
    public static void main(String[] args) throws IOException {
        Evaluator_Generic_Classification_ignoreUpdateIterations evaluator = new Evaluator_Generic_Classification_ignoreUpdateIterations();
        evaluator.evaluate(
                "check list file",
                "E:\\Data\\table annotation\\test_evaluator\\cp",
                "E:\\Data\\table annotation\\test_evaluator\\gs",
                "eval.csv",
                "missed.csv",
                true

        );
    }

    private Map<String, List<Integer>> readCheckListFile(String file) throws IOException {
        List<String> lines = FileUtils.readList(file, false);
        Map<String, List<Integer>> rs = new HashMap<String, List<Integer>>();
        for (String l : lines) {
            String[] parts = l.split("\\|");
            String thefile = parts[0].trim();
            if (thefile.startsWith("\""))
                thefile = thefile.substring(1).trim();
            if (thefile.endsWith("\""))
                thefile = thefile.substring(0, thefile.length() - 1).trim();

            String[] cols = parts[1].trim().split(",");
            List<Integer> columns = new ArrayList<Integer>();
            for (String c : cols) {
                c = c.trim();
                if (c.length() < 1) continue;
                columns.add(Integer.valueOf(c));
            }
            rs.put(thefile, columns);
        }
        return rs;
    }

    public void evaluate(String checklist_file,
                         String in_header_computed_folder,
                         String in_header_gs_folder,
                         String out_result_file,
                         String out_missed_file, boolean gs_NE_only) throws IOException {
        PrintWriter p = new PrintWriter(out_result_file);
        p.println("File,HEADER(0)_cp_y, gs_y, cp_n, p,r,f," +
                "HEADER(1)_cp_y, gs_y, cp_n, p,r,f");

        Map<String, List<Integer>> checklist = readCheckListFile(checklist_file);

        PrintWriter out_missed_writer = new PrintWriter(out_missed_file);
        Set<String> processed = new HashSet<String>();
        for (File gsFile : new File(in_header_gs_folder).listFiles()) {
            Map<Integer, List<List<String>>> gs_headers = null;

            String filename = gsFile.getName();
            if (filename.endsWith(".htm") || filename.endsWith(".html")) {
                if (!filename.contains(".attributes") && !filename.contains(".keys") && !processed.contains(filename)) {
                    String header_gs = gsFile.toString() + ".header.keys";

                    gs_headers = TAnnotationKeyFileReader.readHeaderAnnotation(header_gs, true, gs_NE_only);

                    processed.add(filename);
                } else
                    continue;
            } else {
                continue;
            }
            if (gs_headers == null) {
                System.err.println(filename);
                continue;
            }

            boolean found = false;

            for (File cpFile : new File(in_header_computed_folder).listFiles()) {
                if (gsFile.getName().equals(cpFile.getName())) {
                    found = true;

                    String cpFile_name = cpFile.toString();
                    StringBuilder line = new StringBuilder("\"" + cpFile.getPath() + "\",");

                    Map<Integer, List<List<String>>> cp_headers =
                            TAnnotationKeyFileReader.readHeaderAnnotation(cpFile_name + ".header.keys", false, gs_NE_only);


                    double[] header_data_mode_0 =
                            Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 0);
                    line.append(appendResult(header_data_mode_0));
                    double[] header_data_mode_1 =
                            Evaluator_ClassOnly.compute_prf_header(gs_headers, cp_headers, 1);
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

    //values - 0-cp correct; 1-gs correct; 2-cp wrong
    private String appendResult(double[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append(values[0]).append(",")
                .append(values[1]).append(",")
                .append(values[2]).append(",");
        double p = values[0] / (values[0] + values[2]);
        p = values[0] == 0 ? 0.0 : p;

        double r = values[0] / (values[1]);
        double f = 2 * p * r / (p + r);
        f = r == 0.0 || p == 0.0 ? 0.0 : f;

        sb.append(p).append(",")
                .append(r).append(",")
                .append(f).append(",");
        return sb.toString();

    }

}
