package uk.ac.shef.dcs.sti.xtractor;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.sti.core.model.LList;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 09:59
 * <p/>
 * implements IArticleFilter of gwtwiki library to parse wikipedia dump articles that contain
 * a "wikitable" or "* " (i.e., possible list) elements and store as objects on a file system
 */
public class WikipediaTLPageFilter implements IArticleFilter {

    private TableXtractor tXtractor;
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

    private static Logger logger = Logger.getLogger(WikipediaTLPageFilter.class.getName());

    public WikipediaTLPageFilter(TableXtractor tXtractor, String targetTableDir,
                                 ListXtractor lXtractor, String targetListDir) {
        this.tXtractor = tXtractor;
        this.targetTableDir = targetTableDir;
        this.lXtractor = lXtractor;
        this.targetListDir = targetListDir;

    }

    @Override
    public void process(WikiArticle page, Siteinfo siteinfo) throws SAXException {
        if (countMainPages % 200 == 0) {
            logger.info("Pages processed: " + countMainPages + ", tables " + countTables + ", lists " + countLists);
        }

        if (page.isMain()&&page.getText()!=null) {
            countMainPages++;
            String textLowerCase = page.getText().toLowerCase();
            if (textLowerCase.indexOf("wikitable") != -1) { //it likely contains tables or lists
                List<Table> tables = tXtractor.extract(page.getText(),
                        page.getTitle() + "_" + page.getId());
                for (Table t : tables) {
                    try {
                        TableXtractor.serialize(t, targetTableDir + File.separator + countTableDirs);
                        countTables++;
                    } catch (IOException ioe) {
                        logger.warning("Serialization failed for table " + t.toString());
                    }

                    if (countTables != 0 && countTables % tablesPerDir == 0)
                        countTableDirs++;
                }
            }
            if (textLowerCase.indexOf("* ") != -1) {
                List<LList> lists = lXtractor.extract(page.getText(),
                        page.getTitle() + "_" + page.getId());

                for (LList l : lists) {
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

    public int getCountMainPages() {
        return countMainPages;
    }

    public int getCountTables() {
        return countTables;
    }

    public int getCountLists() {
        return countLists;
    }
}
