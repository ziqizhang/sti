package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.hierarchy;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.TrueOrFalseFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.TrueOrFalseFinderSparql;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;
import uk.ac.shef.dcs.oak.util.FileUtils;

import java.io.IOException;
import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/04/13
 * Time: 17:22
 */
public class HierarchyGenerator {

    private Map<String, Boolean> cacheOfEquivClasses = new HashMap<String, Boolean>();
    private TrueOrFalseFinder trueOfFalseFinder;

    public HierarchyGenerator(TrueOrFalseFinder finder) {
        trueOfFalseFinder = finder;
    }

    public List<Path> generatePaths(Collection<String> uris) {
        List<Step> steps = new ArrayList<Step>();
        for (String uri : uris) {
            Step s = new Step();
            s.add(uri);
            steps.add(s);
        }

        List<Path> paths = new ArrayList<Path>();

        for (int i = 0; i < steps.size(); i++) {
            Step a = steps.get(i);

            for (int j = i + 1; j < steps.size(); j++) {
                Step b = steps.get(j);
                //a subclassof b?
                if (isSubClassOf(a, b)) {
                    addToPaths(a, b, paths);
                    //assume only 1 superclass per class
                    break;
                } else if (isSubClassOf(b, a)) { //b subclassof a?
                    addToPaths(b, a, paths);
                    //break;        //shouldnt break because a is a superclass of b; a can also be superclasses of others (c, d, e)
                }

                if (j == steps.size() - 1&&!alreadySelected(paths, a)) { //a is not a subclass or superclass of anything
                    Path p = new Path();
                    p.getSteps().add(a);
                    paths.add(p);
                }
            }
        }
        return paths;
    }

    private void addToPaths(Step sub, Step sup, List<Path> paths) {
        if (paths.size() == 0) {
            Path p = new Path();
            p.getSteps().add(sub);
            p.getSteps().add(sup);
            paths.add(p);
        } else {
            boolean found = false;
            for (Path p : paths) {
                Step root = p.getSteps().get(p.getSteps().size() - 1);
                Step head = p.getSteps().get(0);
                //sub is the super of this path p
                if (sub.equals(root)) {
                    p.getSteps().add(sup);
                    found = true;
                    break;
                } else if (sub.equals(head)) {
                    System.out.println("duplicated?");
                } else if (sup.equals(head)) {//super is the sub of this path p
                    found = true;
                    p.getSteps().add(0, sub);
                    break;
                } else if (sup.equals(root)) { //there is already a path that contains "sup" as superclass. i.e., we found a different subclass of this sup
                    Path np = new Path();
                    np.getSteps().add(sub);
                    np.getSteps().add(sup);
                    paths.add(np);
                    found=true;
                    break;
                }
            }
            if (!found) {//add new
                Path np = new Path();
                np.getSteps().add(sub);
                np.getSteps().add(sup);
                paths.add(np);
            }
        }
    }

    private boolean isSubClassOf(Step a, Step b) {
        Boolean eq = null;
        for (String aStr : a) {
            for (String bStr : b) {
                eq = cacheOfEquivClasses.get(aStr + " subClassOf " + bStr);
                if (eq == null) {
                    String q1 = null;
                    if (aStr.indexOf("yago") != -1 && bStr.indexOf("yago") != -1)
                        q1 = CommonSPARQLQueries.createQueryIsSubClassOfStupidYAGO(aStr, bStr);
                    else
                        q1 = CommonSPARQLQueries.createQueryIsSubClassOf(aStr, bStr);
                    try {
                        eq = trueOfFalseFinder.hasStatement(q1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cacheOfEquivClasses.put(aStr + " subClassOf " + bStr, eq);
                }
                if (eq == true)
                    break;
            }
        }

        return eq == null ? false : eq;
    }

    private boolean isEquivalent(String iUri, String jUri) throws IOException, SolrServerException {
        Boolean eq = cacheOfEquivClasses.get(iUri + " equals " + jUri);
        eq = eq == null ? cacheOfEquivClasses.get(jUri + " equals " + iUri) : eq;

        if (eq == null) {
            String q1 = CommonSPARQLQueries.createQueryIsEquivalent(iUri, jUri);
            eq = trueOfFalseFinder.hasStatement(q1);
            if (!eq) {
                q1 = CommonSPARQLQueries.createQueryIsEquivalent(jUri, iUri);
                eq = trueOfFalseFinder.hasStatement(q1);
            }
            cacheOfEquivClasses.put(iUri + " equals " + jUri, eq);
            cacheOfEquivClasses.put(jUri + " equals " + iUri, eq);
        }
        return eq;
    }

    private boolean alreadySelected(List<Path> result, Step step) {
        for (Path s : result) {
            if (s.getSteps().contains(step))
                return true;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        Model on =  ModelFactory.createDefaultModel().read("http://dbpedia.org/class/yago");
        StmtIterator it = on.listStatements();
        while(it.hasNext()){
            System.out.println(it.next());
        }
        //String endpoint = "http://dbpedia.org/sparql";
        String endpoint = "http://galaxy.dcs.shef.ac.uk:8893/sparql";
        QueryCache cache = new QueryCache("D:\\work\\billiontripleindex\\solrindex_cache\\zookeeper\\solr", "collection1");

        HierarchyGenerator gen = new HierarchyGenerator(
                new TrueOrFalseFinderSparql(
                        cache, endpoint
                )
        );

        List<String> urls = new ArrayList<String>();
        List<String> lines = FileUtils.readList("D:\\work\\billiontripleindex\\solrindex_cache\\zookeeper\\solr\\collection1/urls.txt", false);
        for (String l : lines) {
            urls.add(l.split(",")[1].trim());
        }


        List<Path> paths = gen.generatePaths(urls);
        //todo: address equivalent classes
        for (Path p : paths)
            System.out.println();
    }
}
