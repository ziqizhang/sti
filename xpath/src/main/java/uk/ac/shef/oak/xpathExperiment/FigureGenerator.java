package uk.ac.shef.oak.xpathExperiment;

import uk.ac.shef.dcs.oak.util.FileUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 08/02/13
 * Time: 15:00
 */
public class FigureGenerator {

    /**
     * InFile must be a CSV that contains 2 columns: 1st column is the accuracy; 2nd column is the identifier
     * of data, e.g., domain-website-attribute
     *
     * @param inFile
     * @param lv1Facet: e.g., domain
     * @param lv2Facet: e.g., attribute
     */
    public static void computeFiguresPerDoubleFacets(String inFile, boolean ignoreZero,
                                                     int lv1Facet, int lv2Facet) throws IOException {
        List<String> lines = FileUtils.readList(inFile, false);

        Map<String, Map<String, List<Double>>> container = new HashMap<String, Map<String, List<Double>>>();

        int count = 1;
        for (String l : lines) {
            String[] cols = l.split(",");
            double figure = 0.0;

            if (cols.length < 2) {
                System.err.println("LINE has less than 2 columns, skipped " + count + ": " + l);
                continue;
            }

            try {
                figure = Double.valueOf(cols[0].trim());
            } catch (NumberFormatException nfe) {
                System.err.println("LINE doesnt begin with a number, skipped " + count + ": " + l);
                continue;
            }

            String[] identifiers = cols[1].split("\\-");
            if (identifiers.length > 3) {
                System.err.println("DATA identifier has less than three elements: " + cols[1]);
                continue;
            }

            String lv1FacetLabel = identifiers[lv1Facet].trim();
            Map<String, List<Double>> lv2FacetValueMap = container.get(lv1FacetLabel);
            lv2FacetValueMap = lv2FacetValueMap == null ? new TreeMap<String, List<Double>>() : lv2FacetValueMap;

            String lv2FacetLabel = identifiers[lv2Facet].trim();
            List<Double> values = lv2FacetValueMap.get(lv2FacetLabel);
            values = values == null ? new ArrayList<Double>() : values;

            if (ignoreZero && figure == 0)
                continue;
            values.add(figure);
            lv2FacetValueMap.put(lv2FacetLabel, values);
            container.put(lv1FacetLabel, lv2FacetValueMap);
            count++;
        }
        PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("./domain&facet.csv"));

        //now compute average
        for (Map.Entry<String, Map<String, List<Double>>> e : container.entrySet()) {
            StringBuilder header = new StringBuilder();

            header.append(e.getKey()).append("\t");
            for (Map.Entry<String, List<Double>> lv2FacetValuePair : e.getValue().entrySet()) {
                header.append(lv2FacetValuePair.getKey().replace(".txt", "")).append(" (").append(lv2FacetValuePair.getValue().size()).
                        append(")\t ");
            }
            System.out.println(header.toString());
            out.println(header.toString());

            //now print values
            header = new StringBuilder("\t");
            for (Map.Entry<String, List<Double>> lv2FacetValuePair : e.getValue().entrySet()) {
                double avg =
                        e.getValue().size() == 0 ? 0 : averageOf(lv2FacetValuePair.getValue());

                header.append(avg).append("\t");
            }

            System.out.println(header);
            out.println(header);

        }
        out.close();
		}catch (Exception e) {

e.printStackTrace();		}
		
    }

    /**
     * InFile must be a CSV that contains 2 columns: 1st column is the accuracy; 2nd column is the identifier
     * of data, e.g., domain-website-attribute
     *
     * @param inFile
     * @param facetId: 0-domain; 1:website; 2:attribute
     */
    public static void computeFiguresPerSingleFacet(String inFile, boolean ignoreZero, int facetId) throws IOException {
        List<String> lines = FileUtils.readList(inFile, false);

        Map<String, List<Double>> container = new HashMap<String, List<Double>>();
        int count = 1;
        for (String l : lines) {
            String[] cols = l.split(",");
            double figure = 0.0;

            if (cols.length < 2) {
                System.err.println("LINE has less than 2 columns, skipped line " + count + ": " + l);
                continue;
            }

            try {
                figure = Double.valueOf(cols[0].trim());
            } catch (NumberFormatException nfe) {
                System.err.println("LINE doesnt begin with a number, skipped " + count + ": " + l);
                continue;
            }

            String[] identifiers = cols[1].split("\\-");
            if (identifiers.length > 3) {
                System.err.println("DATA identifier has less than three elements: " + cols[1]);
                continue;
            }

            String facet = identifiers[facetId].trim();
            List<Double> values = container.get(facet);
            values = values == null ? new ArrayList<Double>() : values;

            if (ignoreZero && figure == 0)
                continue;

            values.add(figure);
            container.put(facet, values);
            count++;
        }

		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("./domain.csv"));


        //now compute average
        for (Map.Entry<String, List<Double>> e : container.entrySet()) {
        	
            if (e.getValue().size() == 0) {
                System.out.println(e.getKey() + "\t0.0");
                out.println(e.getKey() + "\t0.0");
                continue;
            }
            System.out.println(e.getKey() + "\t" + averageOf(e.getValue()));
            out.println(e.getKey() + "\t" + averageOf(e.getValue()));

            
        }

        out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private static double averageOf(List<Double> values) {
        double sum = 0.0;
        for (double d : values) {
            sum = sum + d;
        }
        return sum / values.size();
    }

    public static void splitToAccuracyAndCoverage(String fileTxt, String outfile) throws IOException {
        List<String> lines = FileUtils.readList(fileTxt, false);

        int count = 1;
        List<String> accuracy = new ArrayList<String>();
        List<String> coverage = new ArrayList<String>();
        for (String l : lines) {
            System.out.println(l);
            String[] parts = l.split("\\t");
            if (count % 2 == 0) {
                for (int i = 0; i < parts.length; i++) {
                    accuracy.add(parts[i].trim());
                }
                //print
                if(accuracy.size()!=coverage.size()){
                    System.err.println("ERROR");
                    continue;
                }

                PrintWriter p = new PrintWriter(outfile);
                for(int i=0; i<accuracy.size(); i++){
                    p.println(accuracy.get(i)+","+coverage.get(i));
                }

                p.close();
                //clear
                accuracy.clear();
                coverage.clear();
            } else {
                for (int i = 1; i < parts.length; i++) {
                    String value = parts[i].trim();
                    int index = value.indexOf("(");
                    value = value.substring(index+1, value.length()-1).trim();
                    coverage.add(value);
                }
            }
            count++;
        }

    }

    public static void main(String[] args) throws IOException {
        computeFiguresPerSingleFacet(args[0], true, 0);
        //computeFiguresPerSingleFacet(args[0], true, 1);

        System.out.println();
        computeFiguresPerDoubleFacets(args[0], true, 0,1);
        //computeFiguresPerDoubleFacets("D:\\work\\Dropbox\\LODIE\\KCAPpaper\\csvOfResults/sindice_strategySingleXpath.csv", true, 0, 1);
        splitToAccuracyAndCoverage(args[0],args[1]);
    }

}
