package uk.ac.shef.dcs.oak.sti.test;


import org.openrdf.model.URI;
import uk.ac.shef.dcs.oak.sti.any23.Any23Xtractor;
import uk.ac.shef.dcs.oak.util.FileUtils;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;

import java.io.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 26/03/14
 * Time: 12:13
 * To change this template use File | Settings | File Templates.
 */
public class TestWebpagesForLODAnnotations {

    private static List<String> valid_keywords = new ArrayList<String>();
    static {
        valid_keywords.add("schema.org");
        valid_keywords.add("RDF");
        valid_keywords.add("rdf-");
        valid_keywords.add("foaf");
    }
    public static void main(String[] args) throws IOException {
        String inFolder = args[0];

        for (File f : new File(inFolder).listFiles()) {
            System.out.println(f);
            if(!f.isFile())
                continue;
            int countline=0;

            Map<String, Integer> domains = new HashMap<String, Integer>();
            Map<String, Integer> triples = new HashMap<String, Integer>();
            for (String l : FileUtils.readList(f.toString(), false)) {
                countline++;
                System.out.println(countline);
                String[] parts = l.split("\t");
                if (parts.length < 2)
                    continue;
                String url = parts[1].trim();
                int trim = url.indexOf("//");
                if (trim == -1)
                    continue;
                String domain = url.substring(trim +2 );
                trim = domain.indexOf("/");
                trim = trim == -1 ? domain.length() : trim;
                domain = domain.substring(0, trim).trim();

                Integer count = domains.get(domain);
                count = count == null ? 0 : count;
                count++;
                domains.put(domain, count);

                if(domain.contains("amazon")||domain.contains("wikipedia"))
                    continue;


                if (count < 4) {
                    try {
                        List<LTriple> triples_from_page = Any23Xtractor.extract_from_url(url);
                        if(triples_from_page.size()>0){
                            int countTriples =0;
                            for(LTriple t: triples_from_page){
                                URI p_url = t.getTriple().getPredicate();
                                if(p_url!=null){
                                    String s = p_url.toString();
                                    for(String v: valid_keywords){
                                        if(s.contains(v)){
                                            countTriples++;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(countTriples>0){
                                Integer triples_from_domain = triples.get(domain);
                                triples_from_domain=triples_from_domain==null?0:triples_from_domain;
                                triples_from_domain=triples_from_domain+countTriples;
                                triples.put(domain,triples_from_domain);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    System.out.println("ignoring domain (already finished)"+domain);
                }
            }


            PrintWriter p = new PrintWriter(args[1]+"/"+f.getName());
            for(String d: domains.keySet()){
                int webpages = domains.get(d);
                if(webpages<50)
                    continue;

                p.println(d);
            }
            p.close();
        }
    }
}
