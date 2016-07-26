package uk.ac.shef.dcs.sti.parser.table;

import uk.ac.shef.dcs.sti.STIException;

import java.util.List;

/**
 *
 */
public interface Browsable {

    /**
     * given a file containing tables, return a list of DOM xpaths to the identified
     * table objects in the file, and output an html that is a modified version based on the file
     * that places a checkbox to each of the table.
     *
     * THIS FILE MUST BE placed in 'outputFolder' and should be the only html file in it.
     * The GUI will look for an html in this folder to display in a browser, and it will stop
     * at the first html file it finds.
     *
     * @param input
     * @param sourceId
     * @param outputFolder
     * @return
     */
    List<String> extract(String input, String sourceId, String outputFolder) throws STIException;
}
