package uk.ac.shef.dcs.sti.todo.gs;

import uk.ac.shef.dcs.sti.core.model.List;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.parser.list.validator.ListValidator;
import uk.ac.shef.dcs.sti.parser.list.validator.ListValidatorStrict;
import uk.ac.shef.dcs.sti.parser.table.TableParser;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidatorForWikipediaGSStrict;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidator;
import uk.ac.shef.dcs.sti.parser.list.ListXtractor;

import java.io.File;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 11:08
 */
public class WikipediaTL_GSCreator_Pass2 {

    private static Logger logger = Logger.getLogger(WikipediaTL_GSCreator_Pass2.class.getName());

    public static void process(String tableOutPass1Dir, String tableOutFinalDir, String listOutPass1Dir,
                               String listOutFinalDir) {
        int countTables = 0, countTableDirs = 0, countLists = 0, countListDirs = 0;
        final int tablesPerDir = 5000;
        final int listsPerDir = 5000;

        //WikipediaAPIHelper helper = new WikipediaAPIHelper();
        TableValidator tValidator = new TableValidatorForWikipediaGSStrict();

        File[] sub = new File(tableOutPass1Dir).listFiles();
        for (File dir : sub) {
            File[] files = dir.listFiles();
            for (File file : files) {
                try {
                    Table table = TableParser.deserialize(file.getPath());
                    if (tValidator.validate(table)) {//todo: if selected
                        TableParser.serialize(table, tableOutFinalDir + File.separator + countTableDirs);
                        countTables++;
                        if (countTables != 0 && countTables % tablesPerDir == 0) {
                            logger.info("Done " + countTables + " for tables");
                            countTableDirs++;
                        }
                    } else
                        System.out.print(".");

                } catch (Exception e) {
                    logger.warning("Cannot deserialize object: " + file);
                    e.printStackTrace();
                }
            }
        }

        ListValidator lValidator = new ListValidatorStrict();

        File[] subL = new File(listOutPass1Dir).listFiles();
        for (File dir : subL) {
            File[] files = dir.listFiles();
            for (File file : files) {
                try {
                    List list = ListXtractor.deserialize(file.getPath());
                    if (lValidator.isValid(list)) {//todo: if selected
                        ListXtractor.serialize(list, listOutFinalDir + File.separator + countListDirs);
                        countLists++;
                        if (countLists != 0 && countLists % listsPerDir == 0) {
                            countListDirs++;
                            logger.info("Done " + countLists + " for lists");
                        }
                    } else
                        System.out.print(".");

                } catch (Exception e) {
                    logger.warning("Cannot deserialize object: " + file);
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        process(args[0], args[1], args[2], args[3]);
    }
}
