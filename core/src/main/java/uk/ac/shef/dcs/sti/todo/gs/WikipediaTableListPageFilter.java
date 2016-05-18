package uk.ac.shef.dcs.sti.todo.gs;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.List;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.parser.list.ListXtractor;
import uk.ac.shef.dcs.sti.parser.table.TableParser;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 09:59
 * <p/>
 * implements IArticleFilter of gwtwiki library to parse wikipedia dump articles that contain
 * a "wikitable" or "* " (i.e., possible list) elements and store as objects on a file system
 */
public class WikipediaTableListPageFilter implements IArticleFilter {

    private TableParser tXtractor;
    private String targetTableDir;
    private ListXtractor lXtractor;
    private String targetListDir;

    private int countMainPages;
    private int countTables;
    private int countLists;

    private int countTableDirs;
    private int countListDirs;
    private final int tablesPerDir = 5000;
    private final int listsPerDir = 5000;

    private static Logger logger = Logger.getLogger(WikipediaTableListPageFilter.class.getName());

    public WikipediaTableListPageFilter(TableParser tXtractor, String targetTableDir,
                                        ListXtractor lXtractor, String targetListDir) {
        this.tXtractor = tXtractor;
        this.targetTableDir = targetTableDir;
        this.lXtractor = lXtractor;
        this.targetListDir = targetListDir;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(WikiArticle page, Siteinfo siteinfo) throws IOException {
        if (countMainPages % 200 == 0) {
            logger.info("Pages processed: " + countMainPages + ", tables " + countTables + ", lists " + countLists);
        }

        if (page.isMain()&&page.getText()!=null) {
            countMainPages++;
            String textLowerCase = page.getText().toLowerCase();
            if (textLowerCase.indexOf("wikitable") != -1) { //it likely contains tables or lists
                java.util.List<Table> tables = null;
                try {
                    tables = tXtractor.extract(page.getText(),
                            page.getTitle() + "_" + page.getId());
                } catch (STIException e) {
                    e.printStackTrace();
                }
                for (Table t : tables) {
                    try {
                        TableParser.serialize(t, targetTableDir + File.separator + countTableDirs);
                        countTables++;
                    } catch (IOException ioe) {
                        logger.warning("Serialization failed for table " + t.toString());
                    }

                    if (countTables != 0 && countTables % tablesPerDir == 0)
                        countTableDirs++;
                }
            }
            if (textLowerCase.indexOf("* ") != -1) {
                java.util.List<List> lists = lXtractor.extract(page.getText(),
                        page.getTitle() + "_" + page.getId());

                for (List l : lists) {
                    try {
                        ListXtractor.serialize(l, targetListDir + File.separator + countListDirs);
                        countLists++;
                    } catch (IOException ioe) {
                        logger.warning("Serialization failed for list " + l.toString());
                    }

                    if (countLists != 0 && countLists % listsPerDir == 0)
                        countListDirs++;
                }
            }
        }
    }
}
