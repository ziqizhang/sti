package uk.ac.shef.dcs.sti.todo.gs;

import info.aduna.io.FileUtil;
import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 15/03/14
 * Time: 10:32
 * To change this template use File | Settings | File Templates.
 */
public class GSBuilder_Limaye_Wikitables_consolidate {

    public static void main(String[] args) throws IOException {
        sort_error_by_types("E:\\Data\\table_annotation\\limaye/limaye_regen_log.txt");
        //create_missed_file_tilt("E:\\Data\\table_annotation\\limaye/limaye_regen_log.txt");
    }

    public static void create_missed_file_tilt(String logFile) throws IOException {
        for (String l : FileUtils.readList(logFile, false)) {
            if (l.contains("~")&&l.startsWith("ERROR")) {
                String[] parts = processLogLine(l);
                String file = parts[1].replaceAll("~",":");
                String url = file;
                int trim =url.indexOf(".htm");
                url=url.substring(0,trim).trim();
                trim=url.lastIndexOf("_");
                url = url.substring(0, trim).trim();
                System.out.println(file+"\t\t\t"+url);
            }
        }

    }

    public static void sort_error_by_types(String logFile) throws IOException {

        List<String> errors = new ArrayList<String>();
        for (String l : FileUtils.readList(logFile, false)) {
            if(l.contains("~")&&l.startsWith("ERROR"))
                continue;
            String[] parts = processLogLine(l);
            if (parts == null){
                if (l.contains("WARN")) {
                    errors.add(l);
                    continue;
                }
                else
                    continue;
            }
            if (parts.length != 2) {
                if (l.contains("WARN")) {
                    errors.add(l.trim());
                    continue;
                } else {
                    System.err.println(l);
                    continue;
                }
            }

            errors.add("ERROR:"+parts[0] + ":" + parts[1]);
        }

        Collections.sort(errors);
        for (String l : errors)
            System.out.println(l);
    }

    public static void copy_missed_files(String logFile,
                                         String inFolder_original_raw,
                                         String outFolder_new_raw,
                                         String inFolder_original_gs,
                                         String outFolder_new_gs) throws IOException {


        for (String l : FileUtils.readList(logFile, false)) {
            String[] parts = processLogLine(l);
            if (parts != null) {
                String file = parts[1].trim();
                File original_raw = new File(inFolder_original_raw + "/" + file);
                if (!original_raw.exists()) {
                    System.err.println("raw file does not exist:" + original_raw);
                    continue;
                }
                File original_gs = new File(inFolder_original_gs + "/" + file);
                if (!original_gs.exists()) {
                    System.err.println("gs file does not exist:" + original_gs);
                    continue;
                }

                FileUtil.copyFile(original_raw, new File(outFolder_new_raw + "/" + file));
                FileUtil.copyFile(original_gs, new File(outFolder_new_gs + "/" + file));
            }
        }
    }

    public static String[] processLogLine(String line) {
        if (line.startsWith("ERROR:")) {
            line = line.substring(6).trim();
            String[] parts = line.split(":", 2);
            if (parts.length == 2)
                return parts;
        }
        return null;
    }
}
