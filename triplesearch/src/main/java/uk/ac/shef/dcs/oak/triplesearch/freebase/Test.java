package uk.ac.shef.dcs.oak.triplesearch.freebase;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 20/01/14
 * Time: 15:06
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) throws IOException, ParseException {
        FreebaseQueryHelper searcher =
                new FreebaseQueryHelper("D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties");
       // List<String[]>facts=searcher.search_facts_of_id("/m/02vqcq");
       /* for(String[] f: facts){
            System.out.println(f[0]+"\t"+f[1]+"\t"+f[2]+"\t"+f[3]);
        }*/
       // List<String> res1= searcher.search_topicIds_with_type("/music/album", 1000000);
        searcher.searchapi_topics_with_name_and_type("gore","any",true,10);
    }
}
