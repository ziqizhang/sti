package uk.ac.shef.dcs.oak.lodietest.experiment.table.gs;

import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.model.LList;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.xtractor.ListXtractor;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableXtractor;

import java.io.IOException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 19/10/12
 * Time: 10:45
 */
public class Table_ListLoder {

    public static LTable loadTable(String filename) throws ClassNotFoundException, IOException {
        LTable table=TableXtractor.deserialize(filename);
        return table;
    }
    public static LList loadList(String filename) throws ClassNotFoundException, IOException {
        LList list = ListXtractor.deserialize(filename);
        return list;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        LTable table = loadTable("D:\\work\\lodiedata\\lodie\\tablefinal\\0/_douard_Montpetit__Montreal_Metro__1313797_1");
        System.out.println();
        //LList list = loadList("");
        System.out.println();
    }
}
