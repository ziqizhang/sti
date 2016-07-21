package cz.cuni.mff.xrg.odalic.feedbacks.input;

import java.io.IOException;

/**
 * Input parser.
 * 
 * @author Václav Brodec
 *
 */
public interface InputParser {
  Input parse(String content, String fileIdentifier) throws IOException;
}
