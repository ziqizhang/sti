package uk.ac.shef.dcs.sti.xtractor.csv;

import java.io.File;

import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * @author Škoda Petr
 */
public interface Parser {

    /**
     * Parse given file.
     * 
     * @param inFile
     * @throws DPUException
     * @throws ParseFailed
     */
    Table parse(File inFile) throws ParseFailed, NoCSVDataException;

}
