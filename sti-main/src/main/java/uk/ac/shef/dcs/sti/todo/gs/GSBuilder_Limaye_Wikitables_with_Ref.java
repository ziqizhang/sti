package uk.ac.shef.dcs.sti.todo.gs;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.kbproxy.freebase.FreebaseQueryProxy;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.subjectcol.TColumnFeatureGenerator;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.parser.table.TableParserLimayeDataset;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidatorGeneric;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizerDiscardIrregularRows;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorWikipediaGS;
import uk.ac.shef.dcs.sti.parser.table.TableParserWikipedia;
import uk.ac.shef.dcs.sti.util.FileUtils;
import uk.ac.shef.dcs.websearch.WebSearchFactory;
import uk.ac.shef.dcs.websearch.bing.v5.BingSearchResultParser;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;

/**
 * for each table in Limaye dataset, try to computeElementScores columns with corresponding tables in the most up-to-date wikipedia page table;
 * then each cell is searched in the column, if a cell text matches a link text in any cell in the column, it is assigned the link
 */
public class GSBuilder_Limaye_Wikitables_with_Ref extends GSBuilder_Limaye_Wikitables {
    private StringMetric stringSim = StringMetrics.levenshtein();

    public GSBuilder_Limaye_Wikitables_with_Ref(FreebaseQueryProxy queryHelper,
                                                SolrCache cache_solr,
                                                TableParserWikipedia xtractor,
                                                String propertyFile) {
        this.queryHelper = queryHelper;
        this.solrCache = cache_solr;
        this.xtractor = xtractor;
        try {
            searcher = new WebSearchFactory().createInstance(
                    propertyFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        parser = new BingSearchResultParser();
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
/*
        find_missed_files_by_folder("E:\\Data\\table annotation\\limaye\\all_tables_freebase_groundtruth",
                "E:\\Data\\table annotation\\limaye\\all_tables_groundtruth_xml_only",
                "E:\\Data\\table annotation\\limaye/gs_limaye_empty.missed");
        System.exit(0);*/

        /* find_missed_files("E:\\Data\\table annotation\\limaye/gs_limaye.e8031313", "E:\\Data\\table annotation\\limaye/gs_limaye.missed");
        System.exit(0);*/

//todo: this will not work!
        FreebaseQueryProxy queryHelper =null; //= new FreebaseQueryProxy(args[2]);
        String in_original_limaye_folder = args[0];
        String out_gs_folder = args[1];
        String solrCache = args[3];
        int startfrom = new Integer(args[4]);
        Map<String, String> missedFile = new HashMap<String, String>();

        if (args.length == 6) {
            for (String l : FileUtils.readList(args[5], false)) {
                String[] parts = l.split("\t\t\t");
                missedFile.put(parts[0].trim(), parts[1].trim());
            }
        }

        //todo: this will not work.
        /*File configFile = new File(solrCache + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(solrCache,
                configFile);*/
        EmbeddedSolrServer server = null;// new EmbeddedSolrServer(container, "collection1");
        SolrCache cache = new SolrCache(server);

        TableParserWikipedia xtractor = new TableParserWikipedia(new TableNormalizerDiscardIrregularRows(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorWikipediaGS(false),
                new TableValidatorGeneric());

        GSBuilder_Limaye_Wikitables_with_Ref gsBuilder = new GSBuilder_Limaye_Wikitables_with_Ref(queryHelper,
                cache, xtractor,
                "nKegYOqCMXV0rjUHzKADJinbJ9NrkMyBMqm9h3X9vAo");

        int count = 0;
        File[] all = new File(in_original_limaye_folder).listFiles();
        List<File> sorted = new ArrayList<File>(Arrays.asList(all));
        Collections.sort(sorted);
        System.out.println(all.length);
        for (File f : sorted) {
            try {
                count++;
                if (startfrom > count)
                    continue;
                if (f.getName().startsWith("file")) {
                    System.err.println("ERROR:SKIPPED_NON_WIKI:" + f.getName());
                    continue;
                }

                System.out.println(count + "_" + f);
                Table limaye_table = new TableParserLimayeDataset().extract(f.toString(), null).get(0);

                String wikiPage = null;
                if (missedFile.size() > 0) {
                    for (String mf : missedFile.keySet()) {
                        if (f.toString().endsWith(mf)) {
                            wikiPage = fetchWikipediaWebpage(missedFile.get(mf));
                            break;
                        }
                    }
                    if (wikiPage == null)
                        continue;                                                      //Ã‰ric Cantona
                } else {                                                               //MUST REGEN GS FOR 112 TABLES!!!
                    //multiword
                    //what is corresponding wikipedia title
                    int dot_html = f.getName().indexOf(".htm");
                    if (dot_html == -1)
                        dot_html = f.getName().length();
                    String wikiTitle = f.getName().substring(0, dot_html);

                    if (wikiTitle.indexOf("~") != -1) {
                        System.err.println("ERROR:~DETECTED:" + f.getName());
                        continue;
                    }

                    try {
                        wikiPage = fetchWikipediaWebpage(wikipediaURL + wikiTitle);
                    } catch (Exception e) {
                        wikiTitle = parseToWikipediaTitle(f.toString());
                        try {
                            wikiPage = fetchWikipediaWebpage(wikipediaURL + wikiTitle);
                        } catch (Exception ee) {
                        }
                    }
                    if (wikiPage == null || wikiPage.length() == 0) {
                        wikiPage = gsBuilder.tryWebSearch(wikiTitle);
                        if (wikiPage == null) {
                            System.err.println("ERROR:NO_WIKIPAGE:" + f.getName());
                            continue;
                        }
                    }
                }
                wikiPage = StringUtils.stripAccents(wikiPage);

                List<Node> candidates = extractWikiTables(wikiPage, f.toURI().toString());
                Node theOne = findMatchingTable(limaye_table, candidates);
                if (theOne == null) {
                    System.err.println("ERROR:NO_TABLE:" + f.getName());
                    continue;
                }
                Table wikitable = gsBuilder.
                        process_wikitable(theOne, f.toURI().toString(), f.toURI().toString(), limaye_table.getContexts().toArray(new TContext[0]));
                if (wikitable == null) {
                    System.err.println("ERROR:IRREGULAR_TABLE:" + f.getName());
                    continue;
                }

                gsBuilder.annotateTable(wikitable, limaye_table,
                        out_gs_folder + "/" + f.getName() + ".cell.keys");
                gsBuilder.saveAsLimaye(wikitable, out_gs_folder + "/" + f.getName());


            } catch (Exception e) {
                System.err.println("ERROR:UNKNOWN:" + f.getName());
                e.printStackTrace();
            }
        }
        server.close();
        System.exit(0);
    }


    private void annotateTable(Table wikitable, Table limaye_table,
                               String annotationFile) throws IOException, TransformerException, ParserConfigurationException {
        //saveAsLimaye(table, rawFile);
        StringBuilder annotation = new StringBuilder();
        TColumnFeatureGenerator.setColumnDataTypes(limaye_table);

        for (int c = 0; c < limaye_table.getNumCols(); c++) {
            DataTypeClassifier.DataType type = limaye_table.getColumnHeader(c).getTypes().get(0).getType();
            if (type.equals(DataTypeClassifier.DataType.NUMBER) || type.equals(DataTypeClassifier.DataType.DATE) ||
                    type.equals(DataTypeClassifier.DataType.ORDERED_NUMBER) ||
                    type.equals(DataTypeClassifier.DataType.LONG_TEXT) ||
                    type.equals(DataTypeClassifier.DataType.LONG_STRING))
                continue;

            //find matching column from wikitable
            int matchedCol = findMatchingColumn(limaye_table, c, wikitable);
            if (matchedCol == -1) {
                System.err.println("\tERROR:no matching column=" + c + "," + limaye_table.getColumnHeader(c).getHeaderText());
                continue;
            }
            //annotate
            for (int r = 0; r < limaye_table.getNumRows(); r++) {
                String limaye_cell_text = limaye_table.getContentCell(r, c).getText();
                limaye_cell_text=StringUtils.stripAccents(limaye_cell_text);

                double bestScore = 0.0;
                TCellAnnotation bestAnnotation = null;
                for (int wr = 0; wr < wikitable.getNumRows(); wr++) {
                    TCellAnnotation[] annotations = wikitable.getTableAnnotations().getContentCellAnnotations(wr, matchedCol);
                    for (TCellAnnotation ca : annotations) {
                        double matched_score = stringSim.compare(limaye_cell_text,
                                ca.getTerm());
                        if (matched_score > bestScore) {
                            bestScore = matched_score;
                            bestAnnotation = ca;
                        }
                    }
                }

                if (bestScore > 0.9) {
                    if (bestScore != 1.0) {
                        System.out.println("\t\t\tNoPerfectMatch:" + limaye_cell_text + "(limaye)," + bestAnnotation.getTerm() + "(wiki)");
                    }

                    String wikiTitle = bestAnnotation.getAnnotation().getId();
                    if (wikiTitle.startsWith("/wiki/"))
                        wikiTitle = wikiTitle.substring(6).trim();
                    String pageid = queryWikipediaPageid(wikiTitle, solrCache);
                    if (pageid != null) {
                        String fb_id = createCellAnnotation(pageid, solrCache);
                        if (fb_id != null && fb_id.length() > 0) {
                            annotation.append(r + "," + c + "=").append(fb_id).append("\n");
                        }
                    }
                }
            }
        }

        PrintWriter p = new PrintWriter(annotationFile);
        p.println(annotation);
        p.close();
    }

    private int findMatchingColumn(Table limaye_table, int c, Table wikitable) {
        double maxScore = 0.0;
        int theOne = -1;
        String column = toString_Column(limaye_table, c);
        column=StringUtils.stripAccents(column);
        for (int wc = 0; wc < wikitable.getNumCols(); wc++) {
            String columnContent = toString_Column_Annotation(wikitable, wc);
            double overlap = computeOverlap(column, columnContent);
            if (overlap > maxScore) {

                maxScore = overlap;
                theOne = wc;
            }
        }

        return theOne;
    }


    private String toString_Column(Table table, int c) {
        String text = "";

        for (int r = 0; r < table.getNumRows(); r++) {
            TCell tcc = table.getContentCell(r, c);
            text += tcc.getText() + " ";
        }
        return text;
    }

    private String toString_Column_Annotation(Table table, int c) {
        String text = "";

        for (int r = 0; r < table.getNumRows(); r++) {
            TCellAnnotation[] annotations = table.getTableAnnotations().getContentCellAnnotations(r, c);
            for (TCellAnnotation ca : annotations) {
                text += ca.getTerm() + " ";
            }
        }
        return text;
    }

}
