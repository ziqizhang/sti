package uk.ac.shef.dcs.oak.lodie.table.experiment;

import org.apache.any23.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 10/03/14
 * Time: 20:34
 * To change this template use File | Settings | File Templates.
 */
public class BugChecker {
    public static void main(String[] args) throws IOException {

        String string1 = "/m/06qw_";
        String string2="/m/0859_";
        System.out.println(string1+","+string1.hashCode());
        System.out.println(string2+","+string2.hashCode());
        System.exit(0);


        for(File f : new File("E:\\Data\\table annotation\\corpus_analysis\\100_tables\\tableminer_aclshort_no_ref_ent").listFiles()){
            String content = FileUtils.readFileContent(f);
            if(content.toLowerCase().contains("wind")||
                    content.contains("m/0859_"))
                System.out.println(f);
        }
    }
}
