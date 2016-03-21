package uk.ac.shef.dcs.oak.sti.xtractor.csv;

import java.io.File;
import uk.ac.shef.dcs.oak.sti.rep.LTable;

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
    LTable parse(File inFile) throws ParseFailed, NoCSVDataException;

}
