package cz.cuni.mff.xrg.odalic.feedbacks.input;

/**
 * Input parser.
 * 
 * @author Václav Brodec
 *
 */
public interface InputParser {
  Input parse(String content);
}
