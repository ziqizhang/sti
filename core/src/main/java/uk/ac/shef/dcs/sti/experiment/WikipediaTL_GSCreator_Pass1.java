package uk.ac.shef.dcs.sti.experiment;

import info.bliki.wiki.dump.WikiXMLParser;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.experiment.gs.WikipediaTLPageFilter;
import uk.ac.shef.dcs.sti.xtractor.list.splitter.ListItemSplitterByURL;
import uk.ac.shef.dcs.sti.xtractor.list.validator.ListVaildatorLanient;
import uk.ac.shef.dcs.sti.xtractor.list.ListXtractorWikipedia;
import uk.ac.shef.dcs.sti.xtractor.table.TableXtractorWikipedia;
import uk.ac.shef.dcs.sti.xtractor.table.creator.TableObjCreatorWikipediaGS;
import uk.ac.shef.dcs.sti.xtractor.table.hodetector.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.xtractor.table.normalizer.TableNormalizerDiscardIrregularRows;
import uk.ac.shef.dcs.sti.xtractor.table.validator.TabValWikipediaGSLanient;

import java.io.IOException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/10/12
 * Time: 15:05
 *
 * Pass1:
 * -extracts tables, lists, filter them by "lanient" rules. These tables should be filtered a second time
 *  by pass2
 */
public class WikipediaTL_GSCreator_Pass1 {


    public static void parse(String inputWikib2zFile, String outputTableDir, String outputListDir) throws IOException, SAXException {
        WikiXMLParser parser = new WikiXMLParser(inputWikib2zFile,
                new WikipediaTLPageFilter(
                        new TableXtractorWikipedia(new TableNormalizerDiscardIrregularRows(true),
                                new TableHODetectorByHTMLTag(),
                                new TableObjCreatorWikipediaGS(false),
                                new TabValWikipediaGSLanient()),
                        outputTableDir,
                        new ListXtractorWikipedia(new ListItemSplitterByURL(),
                                new ListVaildatorLanient()),
                        outputListDir
                ));

        parser.parse();
    }


    public static void main(String[] args) throws IOException, SAXException {

        /*TableXtractor xtractor=new TableXtractorHTML(new TableNormalizerFrequentRowLength(),
                                                new TableHODetectorByHTMLTag(),
                                                new TableObjCreatorWikipediaGS(new TableCellElementXtractorByURLnList()),
                                                new TabValWikipediaGSLanient());
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

        xtractor.extract(content.toString(),"this");*/
        /*DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("localhost");
        dbConfig.setDatabase("wikipedia20120502");
        dbConfig.setUser("root");
        dbConfig.setPassword("RedShip");
        dbConfig.setLanguage(WikiConstants.Language.english);
        Wikipedia wiki = new Wikipedia(dbConfig);

        Page p = wiki.getPage("China");

        TableXtractorWikipedia xtractor = new TableXtractorWikipedia(
                new TableNormalizerFrequentRowLength(),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorWikipediaGS()
        );

        List<Table> tables = xtractor.extract(p.getText(), p.getTitle().getPlainTitle());

        System.out.println(tables);*/

        parse(args[0], args[1], args[2]);


    }
}
