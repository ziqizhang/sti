package uk.ac.shef.dcs.sti.experiment;


import uk.ac.shef.dcs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 02/12/13
 * Time: 12:11
 * To change this template use File | Settings | File Templates.
 */
public class TableCorpusSampleSelector {

    //explores a folder of files, select n samples and copy to a target folder.

    public static void main(String[] args) throws IOException {
        List<File> exclusion = new ArrayList<File>();
        /*FileUtils.listFilesRecursive(new ArrayList<File>(),
                new File("E:\\Data\\table_annotation\\limaye_sample\\112_tables_regen\\raw") );
*/


        /*List<File> files = FileUtils.listFilesRecursive(new ArrayList<File>(),
                new File("E:\\Data\\table_annotation\\limaye\\all_tables_freebase_xml(regen)"));*/
        List<File> files = FileUtils.listFilesRecursive(new ArrayList<File>(),
                new File("E:\\Data\\lodie_corpus_consolidated\\LODIE_data\\ISWCdataset\\iswc_corpora\\priority_2\\book\\test"));
        Iterator<File> it = files.iterator();
        while(it.hasNext()){
            File f = it.next();
            if(!f.toString().toLowerCase().endsWith(".xml"))
                it.remove();
        }

        Collections.shuffle(files);

        PrintWriter p = new PrintWriter("E:\\Data\\table_annotation\\limaye_sample\\more_176_tables.csv");
        p.println("FILE,");
        for(int i=0; i<3000; i++){
            File f = files.get(i);
            for(File ef: exclusion){
                if(ef.getName().equals(f.getName()))
                    continue;
            }
            Files.copy(f.toPath(), new File("E:\\Data\\lodie_corpus_consolidated\\LODIE_data\\ISWCdataset\\iswc_corpora\\priority_2\\book\\test_small"+
                    File.separator+f.getName()).toPath());

            p.println("\""+f.toPath()+"\",");

        }
        p.close();

    }
}
