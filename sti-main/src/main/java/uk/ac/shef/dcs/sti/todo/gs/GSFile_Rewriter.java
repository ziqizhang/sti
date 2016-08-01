package uk.ac.shef.dcs.sti.todo.gs;

import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 */
public class GSFile_Rewriter {
    public static void main(String[] args) throws IOException {
        //String inFolder= "E:\\Data\\table annotation\\freebase_crawl\\music_record_label\\musicbrainz_gs";
        //String outFolder= "E:\\Data\\table annotation\\freebase_crawl\\music_record_label\\musicbrainz_gs_reformatted";
        String inFolder= "E:\\Data\\table annotation\\freebase_crawl\\film_film\\imdb_gs";
        String outFolder= "E:\\Data\\table annotation\\freebase_crawl\\film_film\\imdb_gs_reformatted";

        for(File f: new File(inFolder).listFiles()){
            if(!f.toString().endsWith("keys"))
                continue;
            String out = outFolder+File.separator+f.getName();
            PrintWriter p = new PrintWriter(out);
            List<String> lines = FileUtils.readList(f.toString(),false);
            for(String l: lines){
                String[] parts = l.split(",");
                if(parts.length<3)
                    System.err.println("wrong:"+l);
                String pos = parts[0]+","+parts[1]+"=";
                for(int i = 2; i<parts.length; i++){
                    pos = pos+parts[i]+"|";
                }
                p.println(pos);
            }
            p.close();


        }
    }


}
