package uk.ac.shef.dcs.oak.sti.table.xtractor;

import cern.colt.matrix.ObjectMatrix2D;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.oak.sti.table.rep.LTable;
import uk.ac.shef.dcs.oak.sti.table.rep.LTableContext;
import uk.ac.shef.dcs.oak.sti.table.validator.TableValidator;

import java.io.*;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/10/12
 * Time: 12:05
 * <p/>
 * interface for extracting tables from certain RAW input strings
 *
 * WARNING: this class should not be used to READ serialised LTable objects. but process raw input to create them
 */
public abstract class TableXtractor {
    protected TableNormalizer normalizer;
    protected TableHODetector hoDetector;
    protected TableObjCreator creator;
    protected TableValidator[] validators;

    protected TagSoupParser parser;

    public TableXtractor(TableNormalizer normalizer,
                         TableHODetector detector, TableObjCreator creator,
                         TableValidator... validators) {
        this.normalizer = normalizer;
        this.hoDetector = detector;
        this.creator = creator;
        this.validators = validators;
    }

    public abstract List<LTable> extract(String input, String sourceId);

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
    public LTable extractTable(Node tableNode, String tableId, String sourceId, LTableContext... contexts) {
        /*if (sourceId.startsWith("List of U.S. state songs"))
            System.out.println();*/
        List<List<Node>> norm = normalizer.apply(tableNode);
        if (norm.size() == 0)
            return null;
        ObjectMatrix2D preTable = hoDetector.detect(norm);
        LTable table = creator.create(preTable, tableId, sourceId, contexts);
        for (TableValidator tv : validators) {
            if (!tv.validate(table))
                return null;
        }
        return table;
    }


    public static void serialize(LTable table, String targetDir) throws IOException {
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

    public static LTable deserialize(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fileIn =
                new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        LTable table = (LTable) in.readObject();
        in.close();
        fileIn.close();
        return table;
    }
}
