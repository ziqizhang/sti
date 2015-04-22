package uk.ac.shef.dcs.oak.lodie.seeding;

import com.sindice.result.SearchResult;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import uk.ac.shef.dcs.oak.lodie.any23.Any23Xtractor;
import uk.ac.shef.dcs.oak.lodie.architecture.Document;
import uk.ac.shef.dcs.oak.lodie.architecture.LodieException;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;
import uk.ac.shef.dcs.oak.lodie.table.validator.TabValGeneric;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableNormalizerFrequentRowLength;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableObjCreatorHTML;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableXtractorHTML;
import uk.ac.shef.dcs.oak.lodie.util.WorkerThread;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.RegexUtils;
import uk.ac.shef.dcs.oak.util.XPathUtils;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 24/10/12
 * Time: 12:25
 * <p/>
 * This class extracts tables from a page; then check if the triples of interest are found in the table
 *
 * //todo: must completely check this class!!!!!!! changed on 6 Jan 14
 */
@Deprecated
public class WorkerThreadTableProcessor extends WorkerThread {

    private SearchResult sindiceSearchResult;
    private TableXtractorHTML tXtractor;
    private String sindiceTripleQuery;

    /**
     * @param r
     * @param sindiceTripleQuery must match the sindice api's ntriple pattern for a single triple, ie.
     *                           <subject> <predicate> <object>
     */
    public WorkerThreadTableProcessor(SearchResult r, String sindiceTripleQuery) {
        this(r, sindiceTripleQuery,
                new TableXtractorHTML(new TableNormalizerFrequentRowLength(true),
                        new TableHODetectorByHTMLTag(), new TableObjCreatorHTML(),
                        new TabValGeneric()));
    }

    public WorkerThreadTableProcessor(SearchResult r, String sindiceTripleQuery, TableXtractorHTML xtractor) {
        this.sindiceSearchResult = r;
        this.tXtractor = xtractor;
        this.sindiceTripleQuery = sindiceTripleQuery;
    }


    @Override
    public Object process() {
        try {
            String[] querySplits = sindiceTripleQuery.split("\\s+");

            //todo: testing only. delete this
            //URL url = new URL(URLDecoder.decode(sindiceSearchResult.getLink(), "UTF-8"));
            URL url = new URL("http://staffwww.dcs.shef.ac.uk/people/Z.Zhang/resources/textrdfa_annotated_doc.html"
            );

            url.openStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            List<String> declaredNS = RegexUtils.findNameSpaces(content.toString());
            if (possiblyContainsTable(content.toString())) {
                /*
                given the query pattern , e.g., <dog> isa ?
                  compare against triples found on the page to find all triples matching this pattern, e.g.,
                     <dog> isa animal, mammal
                     BUT also keep ALL triples (3_All)
                 */
                List<LTriple> triples = Any23Xtractor.extract_from_url(url.toString());
                List<Integer> selectedTriplesIndex = new ArrayList<Integer>();

                /*
               for each matching triple, check
                 is it found in a table? (check the xpath) does it partially match the query?
                    if yes, add to selectedTriplesIndex (we only interested in triples in tables)
                */
                for (int i = 0; i < triples.size(); i++) {
                    LTriple triple = triples.get(i);
                    if (isPartialMatchWithQuery(triple, querySplits) && isRegularTableTriple(triple))  //ensures only triples on the same row/column are kept
                        selectedTriplesIndex.add(i);
                }

                //if some triples are found int ables, extract tables from this page; note that tables will be
                //validated and invalid tables will be discarded. so even if "selectedTriplesIndex" is not empty
                //the extractedtables may still be empty and therefore no tables will be extracted for this page
                if (selectedTriplesIndex.size() > 0) {
                    List<LTable> tables = tXtractor.extract(content.toString(), url.toString());
                    for (LTable table : tables) {
                        annotateTableByTriples(table, triples, selectedTriplesIndex, declaredNS);
                    }
                    if (tables.size() > 0) {
                        Document<LTable> doc = new Document<LTable>(url);
                        doc.setContent(tables);
                        return doc;
                    }
                }
            }
            return null;
        } catch (MalformedURLException fe) {
            return null;
        } catch (IOException ioe) {
            return null;
        } catch (LodieException e) {
            return null;
        }
    }

    /**
     * @param triple      e.g.,        <dog> isa <animal> . . .             <tower bridge> isa <building> . . .
     * @param querySplits e.g.,    <dog> isa *                          * isa *
     * @return true for the above examples
     */
    private boolean isPartialMatchWithQuery(LTriple triple, String[] querySplits) {
        //todo: revert changes

        return true;

        /*if (querySplits.length != 3)
            return false;

        for (int i = 0; i < querySplits.length; i++) {
            String n = querySplits[i];
            if (n.equals("*"))
                continue;

            try {
                if (("<" + triple.getTriple().getSubject().stringValue() + ">").equalsIgnoreCase(querySplits[i])
                        || triple.getTriple().getSubject().stringValue().equalsIgnoreCase(querySplits[i]))
                    return true;
                if (("<" + triple.getTriple().getPredicate().stringValue() + ">").equalsIgnoreCase(querySplits[i])
                        || triple.getTriple().getPredicate().stringValue().equalsIgnoreCase(querySplits[i]))
                    return true;
                if (("<" + triple.getTriple().getObject().stringValue() + ">").equalsIgnoreCase(querySplits[i])
                        || triple.getTriple().getObject().stringValue().equalsIgnoreCase(querySplits[i]))
                    return true;
            } catch (NullPointerException npe) {
                System.err.println("");
            }
        }

        return false;*/
    }

    private boolean possiblyContainsTable(String htmlCode) {
        return htmlCode.indexOf("<table") != -1 || htmlCode.indexOf("<TABLE") != -1;
    }

    private boolean isRegularTableTriple(LTriple triple) {
        String sXPath = triple.getsXPath();
        String pXPath = triple.getpXPath();
        String oXPath = triple.getoXPath();

        if ((sXPath != null && !XPathUtils.hasElement("table", sXPath))
                || (pXPath != null && !XPathUtils.hasElement("table", pXPath))
                || (oXPath != null && !XPathUtils.hasElement("table", oXPath)))
            return false;

        return XPathUtils.isRegularTableTriple(sXPath, pXPath, oXPath);
    }

    /*
   for each table, check its xpath (modify table extractor)
       is the xpath matching (as-in "contains") the triples of interest
          yes:check selected against cells in tables, annotate table object
          no:skip
    */

    private void annotateTableByTriples(LTable table, List<LTriple> triples, List<Integer> selectedTripleIndices, List<String> declaredNS) {
        Iterator<Integer> itSelected = selectedTripleIndices.iterator();
        while (itSelected.hasNext()) {
            int index = itSelected.next();
            LTriple triple = triples.get(index);
            //System.out.println(triple.toString());
            String sXPath = triple.getsXPath();
            String pXPath = triple.getpXPath();
            String oXPath = triple.getoXPath();

            //is this triple in this table?
            String tableXPath = table.getTableXPath();
            if ((sXPath != null && !sXPath.startsWith(tableXPath)) |
                    (oXPath != null && !oXPath.startsWith(tableXPath)) |
                    (pXPath != null && !pXPath.startsWith(tableXPath)))
                continue;


            //ok so this triple is found in this table. In what row do we find this triple?
            int containingRow = -1;
            for (int r = 0; r < table.getNumRows(); r++) {
                String rowXPath = table.getRowXPaths().get(r);
                if ((sXPath != null && !sXPath.startsWith(rowXPath)) |
                        (oXPath != null && !oXPath.startsWith(rowXPath)) |
                        (pXPath != null && !pXPath.startsWith(rowXPath)))
                    continue;
                else {
                    containingRow = r;
                    break;
                }
            }
            if (containingRow == -1)
                System.err.println("table doesnt contain this triple");


            //now we found the row. lets annotate this row
            // -1 = no match; Integer.Max = match row

            int[] xPathsMap = new int[3]; //stores the mapping between S-table_cell, P-table_cell, O-table_cell
            for (int i = 0; i < xPathsMap.length; i++)
                xPathsMap[i] = -1;

            //for each element in a triple, go thru all cols in the row, and check
            for (int c = 0; c < table.getNumCols(); c++) {
                String cellXPath=table.getContentCell(containingRow,c).getxPath();

                if (sXPath != null && sXPath.startsWith(cellXPath)) {
                    xPathsMap[0] = c;
                } else {
                    String rowXPath = table.getRowXPaths().get(containingRow);
                    if (sXPath != null && xPathsMap[0] == -1 && sXPath.startsWith(rowXPath))
                        xPathsMap[0] = Integer.MAX_VALUE;
                }
                if (pXPath != null && pXPath.startsWith(cellXPath)) {
                    xPathsMap[1] = c;
                } else {
                    String rowXPath = table.getRowXPaths().get(containingRow);
                    if (pXPath != null && xPathsMap[1] == -1 && pXPath.startsWith(rowXPath))
                        xPathsMap[1] = Integer.MAX_VALUE;
                }
                if (oXPath != null && oXPath.startsWith(cellXPath)) {
                    xPathsMap[2] = c;
                } else {
                    String rowXPath = table.getRowXPaths().get(containingRow);
                    if (oXPath != null && xPathsMap[2] == -1 && oXPath.startsWith(rowXPath))
                        xPathsMap[2] = Integer.MAX_VALUE;
                }
            }

            //check: is the xpaths of the triple mapped to this row? (true if: s,p,o each mapped to a td; or mapped to tr
            boolean matched = true;
            for (int i = 0; i < xPathsMap.length; i++) {
                if (i == 0) {
                    if (sXPath != null && xPathsMap[i] == -1) {
                        matched = false;
                        break;
                    }
                } else if (i == 1) {
                    if (pXPath != null && xPathsMap[i] == -1) {
                        matched = false;
                        break;
                    }
                } else if (i == 2) {
                    if (oXPath != null && xPathsMap[i] == -1) {
                        matched = false;
                        break;
                    }
                }
            }


            if (!matched)
                continue;

            //so there is a match. let's annotate this table row
            annotateRowWithTriple(xPathsMap, triple, table, containingRow, declaredNS);
        }


    }

    /*
    xPathMap: each entry is a 2-array that maps: the index-in-triple, column-index-in-table.
    The "index-in-triple" will be 0, 1, 2, corresponding to the S, P, O of the triple So the xpath for them will be
    "index-in-triple"+3; "index-in-triple" can be -1, if there is no xpath found for S, P, or O
     */
    private void annotateRowWithTriple(int[] xPathsMap, LTriple triple, LTable table, int row, List<String> declaredNS) {
        int subjectCol = xPathsMap[0];

        if (triple.getsXPath() != null && subjectCol != -1) { //subject of the triple has an xpath, and it is mapped to this row
            String subject = triple.getTriple().getSubject().stringValue();
            String subjectXPath = triple.getsXPath();
            String subjectXPathEquiv = subjectXPath;

            //firstly annotate subject in the table row
            int type = typeOfElement(0, triple, declaredNS);
            if (type == 0) {
                //is the subject's xpath pointing to the row of the table rather than a column? if so, we ASSUME it means the first column
/*                if (subjectCol == Integer.MAX_VALUE) {
                    ltc = table.getTableCell(row, 0);
                    subjectXPathEquiv = table.getContentCellXPath(row, 0);
                } else {
                    ltc = table.getTableCell(row, subjectCol);
                }*/
                table.getTableAnnotations().setContentCellAnnotations(row, 0,
                        new CellAnnotation[]{new CellAnnotation(subjectXPath, new EntityCandidate(subject,subject), 1.0, new HashMap<String, Double>())});//todo: check what text is added
            } else if (type == 1) { //the subject is not a resource, but a property, or class used to describe the header. (Note that literal is not interesting so we ignore that for subject)
                if (subjectCol == Integer.MAX_VALUE){
                    table.getTableAnnotations().setHeaderAnnotation(0,
                           new HeaderAnnotation[]{new HeaderAnnotation(subjectXPath,subject,subject,1.0)} );
                }

                else {
                    table.getTableAnnotations().setHeaderAnnotation(subjectCol,
                            new HeaderAnnotation[]{new HeaderAnnotation(subjectXPath,subject,subject,1.0)} );
                }

            } else { //literal, or subject=null
            }


            int objectCol = xPathsMap[2];
            String object = null, objectXPath = null;
            //next
            if (triple.getoXPath() != null && objectCol != -1 /*&& objectCol != Integer.MAX_VALUE*/) { //object of the triple has an xpath, and it is mapped to a COLUMN)
                object = triple.getTriple().getObject().stringValue();
                objectXPath = objectCol==Integer.MAX_VALUE? table.getContentCell(row,0).getxPath():triple.getoXPath();
                type = typeOfElement(2, triple, declaredNS);
                if (type == 2) {
                } //if the object's a literal, we dont annotate this cell
                else {
                    if (type == 0) { //if the object's a resource, we annoate this cell
                        if(objectCol==Integer.MAX_VALUE){
                            table.getTableAnnotations().setContentCellAnnotations(row,0,
                                    new CellAnnotation[]{new CellAnnotation(objectXPath, new EntityCandidate(object,object), 1.0,new HashMap<String, Double>())});
                        }
                        else{
                            table.getTableAnnotations().setContentCellAnnotations(row,objectCol,
                                    new CellAnnotation[]{new CellAnnotation(objectXPath,new EntityCandidate(object,object),1.0,new HashMap<String, Double>())});
                        }

                    } else if (type != -1) { //if the object is an ontology term, we annotate this cell's header
                        if(objectCol==Integer.MAX_VALUE){
                            table.getTableAnnotations().setHeaderAnnotation(0,
                                    new HeaderAnnotation[]{new HeaderAnnotation(objectXPath, object,object, 1.0)});
                        }
                        else{
                            table.getTableAnnotations().setHeaderAnnotation(row,
                                    new HeaderAnnotation[]{new HeaderAnnotation(objectXPath, object, object,1.0)});
                        }


                    } else {
                    }
                }
            }

            //next annotate predicate
            int predicateCol = xPathsMap[1];
            if (triple.getpXPath() != null && predicateCol != -1 && predicateCol != Integer.MAX_VALUE) { //pred of the triple has an xpath, and it is mapped to a COLUMN in this row
                //we always assume predicate uses an ontology term
                String predicate = triple.getTriple().getPredicate().stringValue();
                String predicateXPath = triple.getpXPath();
                if (XPathUtils.sameColumn(subjectXPathEquiv, predicateXPath)) { //if the pred and sub in the same td, the pred should be considered a header label because it is likely to be property not relation
                    table.getTableAnnotations().setHeaderAnnotation(predicateCol,
                            new HeaderAnnotation[]{new HeaderAnnotation(predicateXPath,predicate,predicate,1.0)});
                } else { //if the pred and sub not in the same td, this pred is likely to be a relation between two columns
                    if (objectCol != Integer.MAX_VALUE && objectCol != -1) {
                        int header1;
                        if (subjectCol == Integer.MAX_VALUE){
                            header1 = 0;
                        }
                        else{
                            header1 = subjectCol;
                        }
                        int header2 = objectCol;
                        Key_SubjectCol_ObjectCol key = new Key_SubjectCol_ObjectCol(header1, header2);
                        //todo: this only creates a relation annotation on the entire table; what about individual rows????

                        CellBinaryRelationAnnotation relation = new CellBinaryRelationAnnotation(key,0, predicate, "",
                                new ArrayList<String[]>(),1.0);
                        table.getTableAnnotations().addRelationAnnotation_per_row(relation);
                    }
                }
                //finally if pred and obj in the same column, the pred is likely to be a property and so considered a header for object column
                if (objectXPath != null && XPathUtils.sameColumn(predicateXPath, objectXPath)) {
                    table.getTableAnnotations().setHeaderAnnotation(objectCol,
                            new HeaderAnnotation[]{new HeaderAnnotation(predicateXPath,predicate,predicate,1.0)});
                }
            }


        }
    }

    /*
    Given a URI, its position (s, p, o) in a triple, and a list of namespaces declared in the document,
    determine if this URI denotes an ontology term (concept, property), or a unique resource (i.e.,entity)
              rules:
                   triple-s always an instance
                   triple-p always an ontology term
                   triple-o: if "literal", then propery
                             if not literal, but starts with a namespace
                             if not literal, but p means "type of", o is an ontology term
                             if not literal, and p means else, o is a resource

                   if triple-s or triple-o-instance, annotate td cell
                   if triple-p,annotate header/column
                   if triple-o,literal, annotate header/column

                   returns 0 if instance; 1 if ontology term; 2 if literal
    */
    private int typeOfElement(int pos, LTriple triple, List<String> declaredNS) {
        Value v = null;
        if (pos == 0)
            v = triple.getTriple().getSubject();
        else if (pos == 1)
            v = triple.getTriple().getPredicate();
        else if (pos == 2)
            v = triple.getTriple().getObject();
        if (v == null)
            return -1;
        String uri = v.stringValue();

        for (String ns : declaredNS)
            if (uri.startsWith(ns))
                return 1;

        if (v instanceof Literal)
            return 2;
        if (pos == 0) //it is a subject, very likely to be an entity/resource
            return 0;
        if (pos == 1)
            return 1;//it is a property/relation

        return hasClassRange(triple.getTriple().getPredicate().stringValue()) ? 1 : 0;
    }


    /*
    if the predicate's range is a class
     */
    private boolean hasClassRange(String uri) {
        if (uri.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
            return true;
        return false;
    }

}
