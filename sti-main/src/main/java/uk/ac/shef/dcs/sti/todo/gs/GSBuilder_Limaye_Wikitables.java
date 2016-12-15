package uk.ac.shef.dcs.sti.todo.gs;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.tika.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.kbproxy.freebase.FreebaseQueryProxy;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.core.subjectcol.TColumnFeatureGenerator;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.dcs.sti.parser.table.TableParserLimayeDataset;
import uk.ac.shef.dcs.sti.parser.table.TableParserWikipedia;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizerDiscardIrregularRows;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidatorGeneric;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorWikipediaGS;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.sti.util.FileUtils;
import uk.ac.shef.dcs.websearch.WebSearch;
import uk.ac.shef.dcs.websearch.WebSearchFactory;
import uk.ac.shef.dcs.websearch.bing.v5.BingSearchResultParser;
import uk.ac.shef.dcs.websearch.WebSearchResultDoc;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 */
public class GSBuilder_Limaye_Wikitables {
    protected FreebaseQueryProxy queryHelper;
    protected SolrCache solrCache;
    protected TableParserWikipedia xtractor;
    protected WebSearch searcher;
    protected BingSearchResultParser parser;
    protected static String wikipediaURL = "http://en.wikipedia.org/wiki/";
    protected static int maxRows = 200;

    protected static Logger log = Logger.getLogger(GSBuilder_Limaye_Wikitables.class.getName());

    public GSBuilder_Limaye_Wikitables(FreebaseQueryProxy queryHelper,
                                       SolrCache cache_solr,
                                       TableParserWikipedia xtractor,
                                       String propertyFile) throws IOException {
        this.queryHelper = queryHelper;
        this.solrCache = cache_solr;
        this.xtractor = xtractor;
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
        parser = new BingSearchResultParser();
    }

    public GSBuilder_Limaye_Wikitables() {
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
/*
        find_missed_files_by_folder("E:\\Data\\table annotation\\limaye\\all_tables_freebase_groundtruth",
                "E:\\Data\\table annotation\\limaye\\all_tables_groundtruth_xml_only",
                "E:\\Data\\table annotation\\limaye/gs_limaye_empty.missed");
        System.exit(0);*/

        /* find_missed_files("E:\\Data\\table annotation\\limaye/gs_limaye.e8031313", "E:\\Data\\table annotation\\limaye/gs_limaye.missed");
        System.exit(0);*/

//todo: this will not work
        FreebaseQueryProxy queryHelper = null; //new FreebaseQueryProxy(args[3]);
        String in_original_limaye_folder = args[0];
        String in_original_limaye_annotation_folder = args[1];
        String out_gs_folder = args[2];
        String solrCache = args[4];
        int startfrom = new Integer(args[5]);
        Map<String, String> missedFile = new HashMap<String, String>();

        if (args.length == 7) {
            for (String l : FileUtils.readList(args[6], false)) {
                String[] parts = l.split("\t\t\t");
                missedFile.put(parts[0].trim().replaceAll(":", "~"), parts[1].trim());
            }
        }

        //todo: this will not work
        /*File configFile = new File(solrCache + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(solrCache,
                configFile);*/
        EmbeddedSolrServer server = null; //new EmbeddedSolrServer(container, "collection1");
        SolrCache cache = new SolrCache(server);

        TableParserWikipedia xtractor = new TableParserWikipedia(new TableNormalizerDiscardIrregularRows(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorWikipediaGS(true),
                new TableValidatorGeneric());

        GSBuilder_Limaye_Wikitables gsBuilder = new GSBuilder_Limaye_Wikitables(queryHelper,
                cache, xtractor,
                "8Yr8amTvrm5SM4XK3vM3KrLqOCT/ZhkwCfLEDtslE7o=");

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

                if (missedFile.size() > 0) {
                    boolean found = false;
                    for (String mf : missedFile.keySet()) {
                        if (f.toString().endsWith(mf)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found
                            )
                        continue;                                                      //Ã‰ric Cantona
                }

                System.out.println(count + "_" + f);
                /* if (f.toString().contains("Portal~"))
                System.out.println();*/
                Table original = new TableParserLimayeDataset().extract(f.toString(), in_original_limaye_annotation_folder + "/" + f.getName()).get(0);


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

                    try {
                        wikiPage = fetchWikipediaWebpage(wikipediaURL + wikiTitle);
                    } catch (Exception e) {
                        wikiTitle = parseToWikipediaTitle(f.toString());
                        try {
                            wikiPage = fetchWikipediaWebpage(wikipediaURL + wikiTitle);
                        } catch (Exception ee) {
                            if (wikiTitle.indexOf("~") != -1) {
                                System.err.println("ERROR:~REPLACED_NO_MATCH:" + f.getName());
                                continue;
                            }
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
                //wikiPage = StringUtils.stripAccents(wikiPage);
                //wikiPage=wikiPage.replaceAll("â€“", "-");
                Map<String, Set<String>> allLinkMap = extractLinksFromWikipediaPage(wikiPage, f.toURI().toString());
                List<Node> candidates = extractWikiTables(wikiPage, f.toURI().toString());
                Node theOne = findMatchingTable(original, candidates);
                // boolean split_large_table=false;
                if (theOne == null) {
                    boolean fuzzy = gsBuilder.annotateTable_fuzzy(original, allLinkMap, out_gs_folder + "/" + f.getName() + ".cell.keys");
                    if (fuzzy) {
                        System.err.println("WARNING:NO_TABLE:" + f.getName());
                        gsBuilder.saveAsLimaye(original, out_gs_folder + "/" + f.getName());
                    } else System.err.println("ERROR:NO_TABLE:" + f.getName());
                    continue;
                }
                Table wikitable = gsBuilder.
                        process_wikitable(theOne, f.toURI().toString(), f.toURI().toString(), original.getContexts().toArray(new TContext[0]));

                if (wikitable == null) {
                    boolean fuzzy = gsBuilder.annotateTable_fuzzy(original, allLinkMap, out_gs_folder + "/" + f.getName() + ".cell.keys");
                    if (fuzzy) {
                        System.err.println("WARNING:IRREGULAR_TABLE:" + f.getName());
                        gsBuilder.saveAsLimaye(original, out_gs_folder + "/" + f.getName());
                    } else System.err.println("ERROR:IRREGULAR_TABLE:" + f.getName());
                    continue;
                }
                if (wikitable.getNumCols() == 1 && original.getNumCols() > 0) {
                    boolean fuzzy = gsBuilder.annotateTable_fuzzy(original, allLinkMap, out_gs_folder + "/" + f.getName() + ".cell.keys");
                    if (fuzzy) {
                        System.err.println("WARNING:NO_TABLE(only 1 column):" + f.getName());
                        gsBuilder.saveAsLimaye(original, out_gs_folder + "/" + f.getName());
                    } else System.err.println("ERROR:NO_TABLE(only 1 column):" + f.getName());
                    continue;
                }
                if (wikitable.getNumRows() > maxRows) {
                    boolean fuzzy = gsBuilder.annotateTable_fuzzy(original, allLinkMap, out_gs_folder + "/" + f.getName() + ".cell.keys");
                    if (fuzzy) {
                        System.err.println("WARNING:TOO_LARGE:" + f.getName());
                        gsBuilder.saveAsLimaye(original, out_gs_folder + "/" + f.getName());
                    } else System.err.println("ERROR:TOO_LARGE:" + f.getName());
                    continue;
                }
                boolean noHeader = false;
                for (int j = 0; j < wikitable.getNumCols(); j++) {
                    TColumnHeader h = wikitable.getColumnHeader(j);
                    if (h.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN.getValue()))
                        noHeader = true;
                }
                if (noHeader) {
                    boolean fuzzy = gsBuilder.annotateTable_fuzzy(original, allLinkMap, out_gs_folder + "/" + f.getName() + ".cell.keys");
                    if (fuzzy) {
                        System.err.println("WARNING:NO_HEADER:" + f.getName());
                        gsBuilder.saveAsLimaye(original, out_gs_folder + "/" + f.getName());
                    } else System.err.println("ERROR:NO_HEADER:" + f.getName());
                    continue;
                }


                boolean annotated = gsBuilder.annotateTable(wikitable,
                        out_gs_folder + "/" + f.getName() + ".cell.keys", maxRows);
                //if(annotated)
                gsBuilder.saveAsLimaye(wikitable, out_gs_folder + "/" + f.getName());
                // else
                //   System.err.println("ERROR:FEWER_THAN_ORIGINAL_ANNOTATIONS_CREATED,SKIPPED:"+f.getName());


            } catch (Exception e) {
                System.err.println("ERROR:UNKNOWN:" + f.getName());
                e.printStackTrace();
            }
        }
        server.close();
        System.exit(0);
    }

    protected String tryWebSearch(String wikiTitle) throws Exception {
        wikiTitle = wikiTitle.replaceAll("[^a-zA-Z0-9]", " ").trim();
        if (wikiTitle.startsWith("/"))
            wikiTitle = wikiTitle.substring(1).trim();
        String url = "";
        try {
            Object o = solrCache.retrieve("websearch_" + wikiTitle);
            if (o != null)
                url = o.toString();
        } catch (Exception e) {
        }

        if (url.equals("")) {
            List<WebSearchResultDoc> docs = parser.parse(searcher.search(wikiTitle + " wikipedia"));
            for (WebSearchResultDoc d : docs) {
                String title = d.getTitle().replaceAll("[^a-zA-Z0-9]", " ").trim();
                if (d.getUrl().indexOf("wikipedia.org/wiki") == -1)
                    continue;
                if (title.startsWith(wikiTitle)) {
                    url = d.getUrl();
                    break;
                }
            }
            if (url.equals("")) {
                int best = 0, bestindex = -1;
                List<String> wikiTitle_tokens = new ArrayList<String>();
                for (String t : wikiTitle.split("\\s+")) {
                    t = t.trim();
                    if (t.length() > 0)
                        wikiTitle_tokens.add(t);
                }
                for (int i = 0; i < docs.size(); i++) {
                    String candidateTitle = docs.get(i).getTitle().replaceAll("_", " ").trim();
                    if (candidateTitle.indexOf("Wikipedia") == -1)
                        continue;
                    List<String> candidate_tokens = new ArrayList<String>();
                    for (String t : candidateTitle.split("\\s+")) {
                        t = t.trim();
                        if (t.length() > 0) {
                            candidate_tokens.add(t);
                        }
                    }
                    candidate_tokens.retainAll(wikiTitle_tokens);
                    if (candidate_tokens.size() > best) {
                        best = candidate_tokens.size();
                        bestindex = i;
                    }
                }
                if (bestindex != -1) {
                    url = docs.get(bestindex).getUrl();
                }
            }

            try {
                solrCache.cache("websearch_" + wikiTitle, url, true);
            } catch (SolrServerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        if (url.equals(""))
            return null;  //To change body of created methods use File | Settings | File Templates.

/*        int trimStart = url.lastIndexOf("/wiki/");
        if (trimStart != -1) {
            url = url.substring(trimStart + 6);
        }*/
        if (!url.contains("en.wikipedia"))
            return null;
        return fetchWikipediaWebpage(url);
    }


    //Communes_of_the_Haute-Vienne_department_a209.html_0.xml
    protected static String parseToWikipediaTitle(String limaye_data_file_name) {
        limaye_data_file_name = limaye_data_file_name.replaceAll("\\\\", "/");
        int dot_html = limaye_data_file_name.indexOf(".htm");
        if (dot_html == -1)
            return null;
        int begin = limaye_data_file_name.lastIndexOf("/");
        begin = begin == -1 ? 0 : begin + 1;
        limaye_data_file_name = limaye_data_file_name.substring(begin, dot_html);
        int arbitrary_id = limaye_data_file_name.lastIndexOf("_");
        arbitrary_id = arbitrary_id == -1 ? limaye_data_file_name.length() : arbitrary_id;
        limaye_data_file_name = limaye_data_file_name.substring(0, arbitrary_id).trim();

        if (limaye_data_file_name.indexOf("~") != -1) {
            limaye_data_file_name = limaye_data_file_name.replaceAll("~", ":");
        }
        return limaye_data_file_name;
    }


    protected static String fetchWikipediaWebpage(String wikipediaurl) throws IOException, URISyntaxException {

        URL u = new URL(wikipediaurl);
        URLConnection uc = u.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        uc.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        String wikiPage = response.toString();

        return wikiPage;
    }


    protected static String toString_LTable(Table table) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < table.getNumCols(); j++) {
            TColumnHeader header = table.getColumnHeader(j);
            if (header == null || header.getHeaderText() == null || header.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN))
                continue;
            sb.append(header.getHeaderText()).append(" ");
        }
        for (int i = 0; i < table.getNumRows(); i++) {
            for (int j = 0; j < table.getNumCols(); j++) {
                TCell tcc = table.getContentCell(i, j);
                if (tcc != null && tcc.getText() != null) {
                    sb.append(tcc.getText()).append(" ");
                }
            }
        }
        return sb.toString().trim();
    }


    protected void saveAsLimaye(Table table, String outFile) throws TransformerException, ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("entity");
        doc.appendChild(rootElement);
        // logicaltable elements
        Element logicalTable = doc.createElement("logicalTable");

        //content element
        Element content = doc.createElement("content");
        Element context = doc.createElement("tableContext");

        //write header cells
        Element headerElement = doc.createElement("header");
        for (int j = 0; j < table.getNumCols(); j++) {
            Element he = doc.createElement("cell");
            String text = "";
            String wikilink = "";
            TColumnHeader h = table.getColumnHeader(j);
            if (h != null && !h.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN)) {
                text = h.getHeaderText();
            }
            Element textElement = doc.createElement("html");
            text = StringEscapeUtils.escapeHtml4(text);
            text = text.replaceAll("&nbsp;", " ").trim();
            textElement.appendChild(doc.createTextNode(text));
            Element wikiElement = doc.createElement("wikipedia");
            wikiElement.appendChild(doc.createTextNode(wikilink));
            he.appendChild(textElement);
            he.appendChild(wikiElement);
            headerElement.appendChild(he);
        }
        content.appendChild(headerElement);

        //write table cells

        for (int r = 0; r < table.getNumRows(); r++) {
            Element rowElement = doc.createElement("row");
            for (int c = 0; c < table.getNumCols(); c++) {
                Element cell = doc.createElement("cell");
                String text = "";
                String wikilink = "";
                TCell tcc = table.getContentCell(r, c);
                if (tcc != null && tcc.getText() != null) {
                    text = tcc.getText();
                }
                //table.getTableAnnotations().getContentCellAnnotations(r, c);

                TCellAnnotation[] annotations = table.getTableAnnotations().getContentCellAnnotations(r, c);
                if (annotations != null && annotations.length > 0) {
                    TCellAnnotation ca = annotations[0];
                    wikilink = ca.getAnnotation().getId();
                    if (wikilink.startsWith("/wiki/"))
                        wikilink = wikilink.substring(6).trim();
                }

                Element textElement = doc.createElement("html");
                text = StringEscapeUtils.escapeHtml4(text);
                text = text.replaceAll("&nbsp;", " ").trim();
                textElement.appendChild(doc.createTextNode(text));
                Element wikiElement = doc.createElement("wikipedia");
                wikiElement.appendChild(doc.createTextNode(wikilink));
                cell.appendChild(textElement);
                cell.appendChild(wikiElement);
                rowElement.appendChild(cell);
            }
            content.appendChild(rowElement);
        }


        //write table contexts
        for (TContext ltc : table.getContexts()) {
            Element context_child = doc.createElement("context");
            Element context_score = doc.createElement("computeElementScores");
            context_score.appendChild(doc.createTextNode(String.valueOf(ltc.getImportanceScore())));
            Element context_text = doc.createElement("text");
            context_text.appendChild(doc.createTextNode(ltc.getText()));
            context_child.appendChild(context_score);
            context_child.appendChild(context_text);
            context.appendChild(context_child);
        }

        logicalTable.appendChild(content);

        logicalTable.appendChild(context);
        rootElement.appendChild(logicalTable);

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(outFile));

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
    }

    private boolean annotateTable(Table originalTable, String annotationFile, int maxRows) throws IOException, TransformerException, ParserConfigurationException {
        //saveAsLimaye(table, rawFile);
        TColumnFeatureGenerator.setColumnDataTypes(originalTable);

        StringBuilder annotation = new StringBuilder();

        int totalAnnotations = 0;
        for (int r = 0; r < originalTable.getNumRows(); r++) {
            if (r >= maxRows)
                break;
            for (int c = 0; c < originalTable.getNumCols(); c++) {
                DataTypeClassifier.DataType type = originalTable.getColumnHeader(c).getTypes().get(0).getType();
                if (type.equals(DataTypeClassifier.DataType.NUMBER) || type.equals(DataTypeClassifier.DataType.DATE) ||
                        type.equals(DataTypeClassifier.DataType.ORDERED_NUMBER) ||
                        type.equals(DataTypeClassifier.DataType.LONG_TEXT) ||
                        type.equals(DataTypeClassifier.DataType.LONG_STRING))
                    continue;

                System.out.println("\t\tr=" + r + ",c=" + c);
                TCell tcc = originalTable.getContentCell(r, c);
                if (tcc != null) {
                    TCellAnnotation[] annotations = originalTable.getTableAnnotations().getContentCellAnnotations(r, c);
                    String wikiTitle = "";
                    if (annotations != null && annotations.length > 0) {
                        TCellAnnotation ca = annotations[0];
                        wikiTitle = ca.getAnnotation().getId();
                        if (wikiTitle.startsWith("/wiki/")) {
                            wikiTitle = wikiTitle.substring(6).trim();
                            totalAnnotations++;
                        }
                    }

                    if (wikiTitle.length() > 0) {
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
        }

        //boolean output = false;
        int count_original_annotations = 0;
        if (originalTable.getTableAnnotations() != null) {
            for (int r = 0; r < originalTable.getNumRows(); r++) {
                for (int c = 0; c < originalTable.getNumCols(); c++) {
                    TCellAnnotation[] annotations = originalTable.getTableAnnotations().getContentCellAnnotations(r, c);
                    if (annotations != null && annotations.length > 0)
                        count_original_annotations++;
                }
            }
            if (totalAnnotations >= count_original_annotations) {
                //output = true;
            }
        }
        //if (output) {
        PrintWriter p = new PrintWriter(annotationFile);
        p.println(annotation);
        p.close();
        //   }
        return true;
    }

    protected Table process_wikitable(Node tableNode, String tableId, String sourceId, TContext... contexts) {
        Table table = xtractor.extractTable(tableNode, tableId, sourceId, contexts);
        return table;
    }

    private static String toString_Node(Node n) {
        String text = "";
        if (n.hasChildNodes()) {
            for (int i = 0; i < n.getChildNodes().getLength(); i++) {
                Node child = n.getChildNodes().item(i);
                text += toString_Node(child);
            }
        } else {
            text = n.getTextContent() + " ";
        }
        return text;
    }

    protected static double computeOverlap(String string1, String string2) {
        List<String> tokens1 = new ArrayList<String>(Arrays.asList(string1.split("\\s+")));
        List<String> tokens2 = new ArrayList<String>(Arrays.asList(string2.split("\\s+")));

        return CollectionUtils.computeFrequencyWeightedDice(tokens1, tokens2);
    }

    protected static Node findMatchingTable(Table limaye_original_table, List<Node> candidates) {
        double maxScore = 0.0;
        Node theOne = null;
        String table = toString_LTable(limaye_original_table);
        for (Node n : candidates) {
            String nodeContent = toString_Node(n);
            double overlap = computeOverlap(table, nodeContent);
            if (overlap > maxScore) {
                maxScore = overlap;
                theOne = n;
            }
        }

        int theOne_rows = 0;
        int theOne_max_cols = 0;
        if (theOne != null) {
            List<Node> children = DomUtils.findAll(theOne, "//TR");
            if (children != null)
                theOne_rows = children.size();
            for (Node n : children) {
                {
                    if (n.getChildNodes().getLength() > theOne_max_cols)
                        theOne_max_cols = n.getChildNodes().getLength();
                }
            }
        }

        if (theOne == null || (maxScore < 1.0 && theOne_rows < limaye_original_table.getNumRows()) ||
                (theOne_max_cols == 1 && limaye_original_table.getNumCols() > 1)) {
            System.err.println("(candidate table too small, likely to be incorrect so skipped)");
            return null;
        }
        return theOne;
    }

    protected static List<Node> extractWikiTables(String wikiPageContentString, String documentURI) {
        List<Node> tableNodes = new ArrayList<Node>();
        TagSoupParser parser = new TagSoupParser(IOUtils.toInputStream(wikiPageContentString), documentURI, "UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return tableNodes;
        }

        int tableCount = 0;
        List<Node> tables = DomUtils.findAll(doc, /*"//TABLE[@class='wikitable']"*/"//TABLE");
        if (tables.size() > 0)
            tableNodes.addAll(tables);
        /*  if (tables.size() > 0)
        tableNodes.addAll(tables);*/

        return tableNodes;
    }

    public static String queryWikipediaPageid(String wikipedia_title, SolrCache cache) throws IOException {
        Date startTime = new Date();

        String query = "https://en.wikipedia.org/w/api.php?action=query&titles=" + wikipedia_title;
        String pageid = null;
        try {
            Object o = cache.retrieve(query);
            if (o == null) {
                pageid = null;
            } else
                pageid = o.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pageid == null) {
            URL u = new URL(query);
            URLConnection connection = u.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            String result = response.toString();

            int start = result.indexOf("page pageid=");
            if (start != -1) {
                String pageIdLine = result.substring(start);
                int firstQuote = pageIdLine.indexOf("&quot;");
                if (firstQuote != -1) {
                    pageIdLine = pageIdLine.substring(firstQuote + 6);
                    int secondQuote = pageIdLine.indexOf("&quot;");
                    if (secondQuote != -1) {
                        pageIdLine = pageIdLine.substring(0, secondQuote).trim();
                        try {
                            pageid = String.valueOf(Long.valueOf(pageIdLine));
                        } catch (Exception e) {
                            pageid = "";
                        }
                    }
                }
            }

            if (pageid != null) {
                try {
                    cache.cache(query, pageid, true);
                } catch (SolrServerException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

        }

        Date endTime = new Date();
        log.info("queryWikipedia:" + (endTime.getTime() - startTime.getTime()));

        if (pageid != null && pageid.length() > 0)
            return pageid;
        return null;


    }

    public String createCellAnnotation(String pageid, SolrCache cache) throws IOException {
        String freebase_id = null;
        try {
            Object o = cache.retrieve(pageid);
            if (o != null) {
                freebase_id = o.toString();
            }
        } catch (SolrServerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (freebase_id == null) {

            List<String> list = queryHelper.mqlapi_topic_mids_with_wikipedia_pageid(pageid);
            if (list == null || list.size() == 0)
                freebase_id = "";
            else {
                freebase_id = list.get(0);
            }
            try {
                cache.cache(pageid, freebase_id, true);
            } catch (SolrServerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        if (freebase_id == null)
            return null;


        if (freebase_id.equals(""))
            return null;
        else {
            return freebase_id;
        }
    }


    public static void find_missed_files(String inLogFile, String outListFile) throws IOException {


        PrintWriter p = new PrintWriter(outListFile);
        for (String l : FileUtils.readList(inLogFile, false)) {
            if (l.startsWith("ERROR:")) {
                int start = l.indexOf(":");
                l = l.substring(start + 1).trim();
                p.println(l);
            }
        }
        p.close();

    }

    public static void find_missed_files_by_folder(String inFolder_with_annotations, String inFolder_raw, String out_missed_file) throws IOException {

        PrintWriter p = new PrintWriter(out_missed_file);
        List<String> annotated = new ArrayList<String>();
        for (File f : new File(inFolder_with_annotations).listFiles()) {
            annotated.add(f.getName());
        }

        for (File f : new File(inFolder_raw).listFiles()) {
            if (annotated.contains(f.getName() + ".cell.keys"))
                continue;
            else
                p.println(f.getName());
        }

        p.close();
    }

    public static Map<String, Set<String>> extractLinksFromWikipediaPage(String content, String documentURI) {
        TagSoupParser parser = new TagSoupParser(IOUtils.toInputStream(content), documentURI, "UTF-8");
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return null;
        }

        int tableCount = 0;
        List<Node> links = DomUtils.findAll(doc, /*"//TABLE[@class='wikitable']"*/"//A");
        Map<String, Set<String>> linkMap = new HashMap<String, Set<String>>();
        for (Node n : links) {
            String text = n.getTextContent();
            if (text.trim().length() < 1)
                continue;
            String link = "";
            try {
                link = n.getAttributes().getNamedItem("href").getTextContent();
            } catch (Exception e) {
            }
            if (text != null && text.length() > 0 && link.length() > 0) {
                Set<String> theLinks = linkMap.get(text);
                theLinks = theLinks == null ? new HashSet<String>() : theLinks;
                theLinks.add(link);
                linkMap.put(text, theLinks);
            }

        }

        return linkMap;
    }

    private boolean annotateTable_fuzzy(Table originalTable, Map<String, Set<String>> linkMap, String annotationFile
    ) throws IOException, TransformerException, ParserConfigurationException {
        //saveAsLimaye(table, rawFile);
        TColumnFeatureGenerator.setColumnDataTypes(originalTable);
        int count_original_annotations = 0;
        if (originalTable.getTableAnnotations() != null) {
            for (int r = 0; r < originalTable.getNumRows(); r++) {
                for (int c = 0; c < originalTable.getNumCols(); c++) {
                    TCellAnnotation[] annotations = originalTable.getTableAnnotations().getContentCellAnnotations(r, c);
                    if (annotations != null && annotations.length > 0) {
                        count_original_annotations++;
                        originalTable.getTableAnnotations().setContentCellAnnotations(r, c,new TCellAnnotation[0]);
                    }
                }
            }
        }


        StringBuilder annotation = new StringBuilder();
        int totalAnnotations = 0;
        for (int r = 0; r < originalTable.getNumRows(); r++) {
            for (int c = 0; c < originalTable.getNumCols(); c++) {
                DataTypeClassifier.DataType type = originalTable.getColumnHeader(c).getTypes().get(0).getType();
                if (type.equals(DataTypeClassifier.DataType.NUMBER) || type.equals(DataTypeClassifier.DataType.DATE) ||
                        type.equals(DataTypeClassifier.DataType.ORDERED_NUMBER) ||
                        type.equals(DataTypeClassifier.DataType.LONG_TEXT) ||
                        type.equals(DataTypeClassifier.DataType.LONG_STRING))
                    continue;

                System.out.println("\t\tr=" + r + ",c=" + c);
                TCell tcc = originalTable.getContentCell(r, c);
                if (tcc != null) {
                    String text = tcc.getText().trim();
                    Set<String> links = linkMap.get(text);
                    String wikiTitle = "";
                    if (links != null && links.size() == 1) {
                        wikiTitle = links.iterator().next();
                        if (wikiTitle.startsWith("/wiki/")) {
                            wikiTitle = wikiTitle.substring(6).trim();
                            totalAnnotations++;
                        }
                    }

                    if (wikiTitle.length() > 0) {
                        String pageid = queryWikipediaPageid(wikiTitle, solrCache);
                        if (pageid != null) {
                            String fb_id = createCellAnnotation(pageid, solrCache);
                            if (fb_id != null && fb_id.length() > 0) {
                                annotation.append(r + "," + c + "=").append(fb_id).append("\n");
                            }
                            TCellAnnotation ca =
                                    new TCellAnnotation(tcc.getText(), new Entity(wikiTitle, wikiTitle), 1.0, new HashMap<String, Double>());
                            originalTable.getTableAnnotations().setContentCellAnnotations(
                                    r, c, new TCellAnnotation[]{ca}
                            );
                        }
                    }
                }
            }
        }

        boolean output = false;
        //System.err.println(totalAnnotations+","+count_original_annotations);
        if (totalAnnotations >= count_original_annotations)
            output = true;

        if (output) {
            PrintWriter p = new PrintWriter(annotationFile);
            p.println(annotation);
            p.close();
        }
        return output;
    }


}
