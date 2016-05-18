package uk.ac.shef.dcs.sti.parser.table;

import cern.colt.matrix.ObjectMatrix2D;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetector;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizer;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreator;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidator;

import java.io.*;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/10/12
 * Time: 12:05
 * <p/>
 * interface for extracting tables from certain RAW input strings
 *
 * WARNING: this class should not be used to READ serialised Table objects. but learn raw input to create them
 */
public abstract class TableParser {
    protected TableNormalizer normalizer;
    protected TableHODetector hoDetector;
    protected TableObjCreator creator;
    protected TableValidator[] validators;

    protected TagSoupParser parser;

    public TableParser(TableNormalizer normalizer,
                       TableHODetector detector, TableObjCreator creator,
                       TableValidator... validators) {
        this.normalizer = normalizer;
        this.hoDetector = detector;
        this.creator = creator;
        this.validators = validators;
    }

    public abstract List<Table> extract(String input, String sourceId) throws STIException, IOException;

    /**
     * Processes table elements following the basic principles:
     * 1. normalize a table element (Jsoup) into a regular n x m table
     * 2. find header and orientation of the table
     * 3. extract text values in each table cell
     * 4. validate the extracted tables
     * <p/>
     * (examples of tables that will be discarded by this method include(inaddition to the tablevalidator rules):
     * tables only contain images but no texts;
     * tables only have "tr" which has no "td"
     *
     * @param tableNode must be the <table> element
     * @param sourceId
     * @return null if no valid tables are extracted; Table object if otherwise
     */
    public Table extractTable(Node tableNode, String tableId, String sourceId, TContext... contexts) {
        /*if (sourceId.startsWith("List of U.S. state songs"))
            System.out.println();*/
        List<List<Node>> norm = normalizer.normalize(tableNode);
        if (norm.size() == 0)
            return null;
        ObjectMatrix2D preTable = hoDetector.detect(norm);
        Table table = creator.create(preTable, tableId, sourceId, contexts);
        for (TableValidator tv : validators) {
            if (!tv.validate(table))
                return null;
        }
        return table;
    }


    public static void serialize(Table table, String targetDir) throws IOException {
        File dir = new File(targetDir);
        if (!dir.exists())
            dir.mkdirs();
        String filename = targetDir + File.separator + table.getSourceId().replaceAll("[^\\d\\w]", "_") + "_" + table.getTableId();

        FileOutputStream fileOut =
                new FileOutputStream(filename);
        ObjectOutputStream out =
                new ObjectOutputStream(fileOut);
        out.writeObject(table);
        out.close();
        fileOut.close();
    }

    public static Table deserialize(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fileIn =
                new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Table table = (Table) in.readObject();
        in.close();
        fileIn.close();
        return table;
    }
}
