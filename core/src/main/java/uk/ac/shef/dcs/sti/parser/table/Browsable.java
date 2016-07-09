package uk.ac.shef.dcs.sti.parser.table;

import uk.ac.shef.dcs.sti.STIException;

import java.util.List;

/**
 * Created by - on 08/07/2016.
 */
public interface Browsable {

    /**
     * given a file containing tables, return a list of DOM paths to the identified
     * table objects in the file, and output an html that is a modified version based on the file
     * that places a checkbox to each of the table
     * @param input
     * @param sourceId
     * @param outputFolder
     * @return
     */
    List<String> extract(String input, String sourceId, String outputFolder) throws STIException;
}
