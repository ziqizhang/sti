package uk.ac.shef.dcs.oak.lodie.test;

import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 21/03/14
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class TestSelectFilesUtil {
    public static void main(String[] args) throws IOException {
        String from_annotation_folder="E:\\Data\\table_annotation\\limaye\\all_tables_groundtruth_freebase(regen)";
        String from_rawfile_folder="E:\\Data\\table_annotation\\limaye\\all_tables_raw(regen)";
        String to_annotation_folder="E:\\Data\\table_annotation\\limaye_sample\\88_tables_regen\\gs_new";
        String to_rawfile_folder="E:\\Data\\table_annotation\\limaye_sample\\88_tables_regen\\raw_new";

        String filter_files_folder="E:\\Data\\table_annotation\\limaye_sample\\88_tables_regen\\raw";
        List<String> filter = new ArrayList<String>();
        for(File f: new File(filter_files_folder).listFiles()){
            filter.add(f.getName());
        }

        for(File f: new File(from_annotation_folder).listFiles()){
            for(String fl: filter){
                if(f.getName().startsWith(fl)){
                    FileUtil.copyFile(f, new File(to_annotation_folder+"/"+f.getName()));
                    break;
                }
            }
        }

        for(File f: new File(from_rawfile_folder).listFiles()){
            for(String fl: filter){
                if(f.getName().equals(fl)){
                    FileUtil.copyFile(f, new File(to_rawfile_folder+"/"+f.getName()));
                    break;
                }
            }
        }
    }
}
