package uk.ac.shef.dcs.sti.todo;

import org.apache.any23.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 02/04/14
 * Time: 10:22
 * To change this template use File | Settings | File Templates.
 */
public class Test_GS_Fixer_find_incorrect_notable_type {
    public static void main(String[] args) throws IOException {
        for(File f: new File("E:\\Data\\table_annotation\\limaye_sample\\200_tables_regen\\gs\\header_ne+prop").listFiles()){
            String content=FileUtils.readFileContent(f);
            if(content.contains("/m/")){
                System.out.println(f);
                //break;
            }

        }
    }
}
