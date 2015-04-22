package uk.ac.shef.dcs.oak.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 31/10/12
 * Time: 21:29
 */
public class XPathUtils {

    public static String[] splitToSteps(String xpath) {
        return xpath.split("/");
    }

    /**
     * @param tag   e.g., tr
     * @param xpath e.g., /html/body/table[1]/tr[2]/td[2]
     * @return /html/body/table[1]/tr[2]
     */
    public static String trimXPathLastTag(String tag, String xpath) {
        int lastTagIndex = xpath.lastIndexOf(tag);
        if (lastTagIndex != -1) {
            int end = xpath.indexOf("/", lastTagIndex);
            if (end == -1) {
                return xpath;
            }
            return xpath.substring(0, end);
        }
        return null;
    }

    public static boolean hasElement(String elementTag, String xpath) {
        return xpath.toLowerCase().indexOf(elementTag.toLowerCase()) != -1;
    }

    public static int stepIndex(String step, String[] steps) {
        for (int i = 0; i < steps.length; i++) {
            String cStep = steps[i];
            if (cStep.equalsIgnoreCase(step))
                return i;
        }
        return -1;
    }

    public static int longestMatch(String[] path1, String[] path2) {
        int longest = path1.length > path2.length ? path2.length : path1.length;


        for (int i = 0; i < longest; i++) {
            String stepP1 = path1[i];
            String stepP2 = path2[i];
            if (!stepP1.equalsIgnoreCase(stepP2))
                return i - 1;
        }
        return longest;
    }

    /**
     * @param subjectXPath
     * @param predicateXPath
     * @param objectXPath
     * @return true of S, P, O in the same row or column, regardless if any one of the three elements is
     *         a parent element of others (e.g., S=tr, P=tr/td, O=tr/td/text()
     */
    public static boolean isRegularTableTriple(String subjectXPath, String predicateXPath, String objectXPath) {
        //todo: this is not a good approach. the goal is to avoid "null" xpath when predicate is "rdf:type"
        if(predicateXPath==null)
            predicateXPath=subjectXPath;


        subjectXPath = subjectXPath.toLowerCase();
        predicateXPath = predicateXPath.toLowerCase();
        objectXPath = objectXPath.toLowerCase();

        //find the path to the containing tables of S, O, P
        String subjectTableSrc = trimXPathLastTag("table", subjectXPath);
        if (subjectTableSrc == null || subjectTableSrc.equals(subjectXPath)) return false;
        String predicateTableSrc = trimXPathLastTag("table", predicateXPath);
        if (predicateTableSrc == null || predicateTableSrc.equals(predicateXPath)) return false;
        String objectTableSrc = trimXPathLastTag("table", objectXPath);
        if (objectTableSrc == null || objectTableSrc.equals(objectXPath)) return false;

        //if S,O,P not in the same table, this triple is not interesting
        if (subjectTableSrc.equals(objectTableSrc) && subjectTableSrc.equals(predicateTableSrc)) {
            //next check if the triple is in the same row? or column? otherwise, it is not interesting
            String sContainingCell = subjectXPath.substring(subjectTableSrc.length());
            String pContainingCell = predicateXPath.substring(predicateTableSrc.length());
            String oContainingCell = objectXPath.substring(objectTableSrc.length());

            if (sameRow(sContainingCell, pContainingCell, oContainingCell) ||
                    sameColumn(sContainingCell, pContainingCell, oContainingCell))
                return true;

        }

        return false;
    }

    public static boolean sameRow(String... trXPaths) {
        String prevTR = null;
        for (String path : trXPaths) {
            path=path.toLowerCase();
            int firstTRIndex = path.indexOf("tr");
            if (firstTRIndex == -1)
                return false;
            int end = path.indexOf("/", firstTRIndex);
            end = end == -1 ? path.length() : end;
            String firstTR = path.substring(firstTRIndex, end);
            if (prevTR == null) {
                prevTR = firstTR;
                continue;
            } else if (!prevTR.equals(firstTR))
                return false;
        }

        return true;

    }

    public static boolean sameColumn(String... trXPaths) {
        String prevTD = null;
        for (String path : trXPaths) {
            path=path.toLowerCase();
            int firstTRIndex = path.indexOf("tr");
            if (firstTRIndex == -1)
                return false;
            int firstTDIndex = path.indexOf("td",firstTRIndex);
            if(firstTDIndex==-1)
                return false;
            int end = path.indexOf("/", firstTDIndex);
            end=end==-1?path.length():end;
            String firstTD = path.substring(firstTDIndex, end);
            if(prevTD==null){
                prevTD=firstTD;
                continue;
            }
            else if(!prevTD.equals(firstTD)){
                return false;
            }

        }
        return true;
    }

    /**
     * Translates an xpath into manageable components by Jsoup.
     * Example:
     * <p/>
     * /HTML[1]/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV[3]
     * =>
     * {
     * {html>body>div,2},
     * {>div>div,     5},
     * {>div,         2}
     * }
     * <p/>
     * <p/>
     * <p/>
     * /HTML[1]/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV[3]/text()
     * =>
     * {
     * {html>body>div,2},
     * {>div>div,     5},
     * {>div,         2}
     * {text(),         f}            (f is an indicator that this is a function of the xpath
     * }
     * <p/>
     * <p/>
     * <p/>
     * <p/>
     * /HTML[1]/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV[3]/@href
     * =>
     * {
     * {html>body>div,2},
     * {>div>div,     5},
     * {>div,         2}
     * {\@href,       f}            (f is an indicator that this is a function of the xpath
     * }
     *
     * @param xpath
     * @return
     */
    public static List<String[]> translateXPath(String xpath) {
        List<String[]> paths = new ArrayList<String[]>();

        String[] steps = xpath.split("/");
        StringBuilder oneJSoupPath = new StringBuilder();

        for (int i = 0; i < steps.length; i++) {
            String step = steps[i].trim();
            if (step.length() == 0)
                continue;

            if (step.indexOf("[") == -1) {
                //is it a function, text() or @something
                if (step.equalsIgnoreCase("text()") || step.startsWith("@")) {
                    if (oneJSoupPath.length() > 0) {
                        String[] add = new String[]{oneJSoupPath.toString(), "0"};
                        paths.add(add);
                        oneJSoupPath = new StringBuilder();
                    }
                    paths.add(new String[]{step, "f"});
                    continue;
                }

                oneJSoupPath.append(">").append(step);
                continue;
            }

            String element = step.substring(0, step.indexOf("[")).trim();
            String indexStr = step.substring(step.indexOf("[") + 1, step.length() - 1).trim();

            int index = -1;
            try {
                index = Integer.valueOf(indexStr);
            } catch (NumberFormatException nfe) {
                oneJSoupPath.append(element);
                continue;
            }

            index = index - 1;
            if (index == 0) {
                oneJSoupPath.append(">").append(element);
                continue;
            }

            oneJSoupPath.append(">").append(element);
            String[] add = new String[]{oneJSoupPath.toString(), String.valueOf(index)};
            paths.add(add);
            oneJSoupPath = new StringBuilder();
        }

        if (oneJSoupPath.length() > 0)
            paths.add(new String[]{oneJSoupPath.toString(), "0"});

        return paths;
    }

    public static void main(String[] args) {
        System.out.println(XPathUtils.translateXPath("/HTML[1]/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV[3]"));
        System.out.println(XPathUtils.translateXPath("/HTML/BODY/DIV[3]/DIV/DIV[6]/DIV[3]/text()"));
        System.out.println(XPathUtils.translateXPath("/HTML[1]/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV"));
        System.out.println(XPathUtils.translateXPath("/HTML[1]/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV/text()"));
        System.out.println(XPathUtils.translateXPath("/HTML[1]/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV[3]/@href"));


    }
}
