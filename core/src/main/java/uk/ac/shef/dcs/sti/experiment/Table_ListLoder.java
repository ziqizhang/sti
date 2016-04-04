package uk.ac.shef.dcs.sti.experiment;

import uk.ac.shef.dcs.sti.core.model.List;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.xtractor.list.ListXtractor;
import uk.ac.shef.dcs.sti.xtractor.table.TableXtractor;

import java.io.IOException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 19/10/12
 * Time: 10:45
 */
public class Table_ListLoder {

    public static Table loadTable(String filename) throws ClassNotFoundException, IOException {
        Table table=TableXtractor.deserialize(filename);
        return table;
    }
    public static List loadList(String filename) throws ClassNotFoundException, IOException {
        List list = ListXtractor.deserialize(filename);
        return list;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Table table = loadTable("D:\\work\\lodiedata\\lodie\\tablefinal\\0/_douard_Montpetit__Montreal_Metro__1313797_1");
        System.out.println();
        //LList list = loadList("");
        System.out.println();
    }
}
