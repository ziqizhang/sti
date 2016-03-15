package uk.ac.shef.dcs.sti.util;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.sti.kb.KnowledgeBaseSearcher_Freebase;
import uk.ac.shef.dcs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 21/04/2015.
 */
public class FreebaseTypeGranularityPopulator {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //read the index, save all "types" to a file
        /*Directory dirIndex = FSDirectory.open(new File(args[0]));
        IndexReader indexReader = DirectoryReader.open(dirIndex);
        Document doc = null;
        Set<String> types = new HashSet<String>();

        System.out.println("total="+indexReader.numDocs());
        for(int i = 0; i < indexReader.numDocs(); i++) {
            System.out.println(i);
            doc = indexReader.document(i);
            BytesRef bytesRef = doc.getBinaryValue("value");
            if(bytesRef==null)
                continue;
            Object object =  SerializableUtils.deserializeBase64(bytesRef.bytes);
            try{
                List<EntityCandidate> entities = (List<EntityCandidate>) object;
                for(EntityCandidate ec: entities) {
                    for(String t: ec.getTypeIds()){
                        if(KB_InstanceFilter.ignoreType(t,t)||t.startsWith("/m/"))
                            continue;
                        types.add(t);
                    }
                }
            }catch(ClassCastException cce){
                //cce.printStackTrace();
            }
        }
        indexReader.close();
        dirIndex.close();

        List<String> allTypes = new ArrayList<String>(types);
        Collections.sort(allTypes);

        PrintWriter p =new PrintWriter(args[1]+"/types.txt");
        for(String t: allTypes)
            p.println(t);
        p.close();

        System.exit(0)*/
        ;
        //fetch freebase pages, parse them and get granularity scores
        List<String> all_types = new ArrayList<String>(new HashSet<String>(FileUtils.readList(args[1] + "/types_merge_all.txt", false)));
        Collections.sort(all_types);
        /*PrintWriter p =new PrintWriter(args[1]+"/types.txt");
        for(String t: all_types)
            p.println(t);
        p.close();
        System.exit(0);*/

        File configFileConcept = new File(args[3] + File.separator + "solr.xml");
        CoreContainer containerConcept = new CoreContainer(args[3],
                configFileConcept);
        SolrServer serverConcept = new EmbeddedSolrServer(containerConcept, "collection1");

        File configFileProperty = new File(args[4] + File.separator + "solr.xml");
        CoreContainer containerProperty = new CoreContainer(args[4],
                configFileProperty);
        SolrServer serverProperty = new EmbeddedSolrServer(containerProperty, "collection1");

        KnowledgeBaseSearcher_Freebase kbSeacher = new KnowledgeBaseSearcher_Freebase(args[2], true, null, serverConcept, serverProperty);

        //kbSeacher.find_triplesForProperty("/award/award_category/nomination_announcement");

        System.out.println("total = " + all_types.size());
        int count = 0;
        for (String t : all_types) {
            System.out.println(count + "_" + t);
            try {
                kbSeacher.find_granularityForConcept(t);
                kbSeacher.findTriplesOfConcept(t);
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
        kbSeacher.finalizeConnection();
        System.exit(0);
    }
}
