package uk.ac.shef.dcs.sti.experiment;


import info.aduna.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.core.model.Table;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 13/03/14
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {

        List<String> lines =
                FileUtils.
                        readLines(
                                new File("E:\\Data\\lodie_corpus_consolidated\\LODIE_data\\ISWCdataset\\COMPUTED\\priority_2_book_goodreads\\book-goodreads-booktitle.txt.tableminer"),
                                "UTF-8");
        System.exit(0);


        String rawFileFolder = "E:\\Data\\lodie_corpus_consolidated\\LODIE_data\\ISWCdataset\\test";
        String checkListFolder="E:\\Data\\lodie_corpus_consolidated\\iswc_output_selected\\goodreads-test-3000";
        String outCopyFolder = "E:\\Data\\lodie_corpus_consolidated\\iswc_output_selected\\new";
        for(File f: new File(checkListFolder).listFiles()){
            if(f.getName().endsWith(".html")&&!f.getName().contains("attributes")){
                String name = f.getName();
                name=name.substring(0, name.lastIndexOf(".html")).trim();
                String rawFifle = rawFileFolder+"/"+name;
                FileUtil.copyFile(new File(rawFifle), new File(outCopyFolder+"/"+name));
            }
        }
        System.exit(0);


        int count=0;
        for(File f: new File("E:\\Data\\table_annotation\\limaye\\all_tables_raw(regen)").listFiles()){
            count++;
            Table table = LimayeDatasetLoader.readTable(f.toString(), null, null);
            System.out.println(count);
        }
        System.exit(0);

       /* for(File f: new File("E:\\Data\\table_annotation\\limaye_sample\\112_tables_regen\\raw").listFiles()){
            String content=FileUtils.readFileToString(f);;
            content=content.replaceAll("â€“","-");
            PrintWriter p =new PrintWriter(f.toString());
            p.println(content);
            p.close();
        }*/


        List<String> reference = new ArrayList<String>();
        for(File f: new File("E:\\Data\\table_annotation\\limaye_sample\\112_tables\\raw\\112_tables").listFiles()){
            reference.add(f.getName()+".cell.keys");
        }

        for(File f: new File("E:\\Data\\table_annotation\\limaye_sample\\112_tables_regen\\gs\\regen_entity").listFiles()){
            if(reference.contains(f.getName())){
               // FileUtil.copyFile(f, new File("E:\\Data\\table_annotation\\limaye_sample\\112_tables_regen\\raw/"+f.getName()));
            }
            else
                f.delete();
        }
/*
        List<String> reference = new ArrayList<String>();
        for(File f: new File("E:\\Data\\table_annotation\\limaye_sample\\112_tables_regen\\raw").listFiles()){
            reference.add(f.getName());
        }

        for(File f: new File("E:\\Data\\table_annotation\\limaye_sample\\112_tables\\raw\\112_tables").listFiles()){
            if(!reference.contains(f.getName())){
                FileUtil.copyFile(f, new File("E:\\Data\\table_annotation\\limaye_sample\\112_tables_regen\\raw/" + f.getName()));
            }
        }*/
    }
}
