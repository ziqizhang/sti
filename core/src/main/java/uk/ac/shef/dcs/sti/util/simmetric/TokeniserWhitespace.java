package uk.ac.shef.dcs.sti.util.simmetric;

/**
 * Created by zqz on 15/05/2015.
 */
/**
 * SimMetrics - SimMetrics is a java library of Similarity or Distance
 * Metrics, e.g. Levenshtein Distance, that provide float based similarity
 * measures between String Data. All metrics return consistant measures
 * rather than unbounded similarity scores.
 *
 * Copyright (C) 2005 Sam Chapman - Open Source Release v1.1
 *
 * Please Feel free to contact me about this library, I would appreciate
 * knowing quickly what you wish to use it for and any criticisms/comments
 * upon the SimMetric library.
 *
 * email:       s.chapman@dcs.shef.ac.uk
 * www:         http://www.dcs.shef.ac.uk/~sam/
 * www:         http://www.dcs.shef.ac.uk/~sam/stringmetrics.html
 *
 * address:     Sam Chapman,
 *              Department of Computer Science,
 *              University of Sheffield,
 *              Sheffield,
 *              S. Yorks,
 *              S1 4DP
 *              United Kingdom,
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

        import java.io.Serializable;
        import java.util.ArrayList;
        import java.util.HashSet;
        import java.util.Set;

        import uk.ac.shef.wit.simmetrics.tokenisers.InterfaceTokeniser;
        import uk.ac.shef.wit.simmetrics.wordhandlers.DummyStopTermHandler;
        import uk.ac.shef.wit.simmetrics.wordhandlers.InterfaceTermHandler;

/**
 * Package: uk.ac.shef.wit.simmetrics.tokenisers
 * Description: TokeniserWhitespace implements a simple whitespace tokeniser.
 * Date: 31-Mar-2004
 * Time: 15:17:07
 * @author Sam Chapman <a href="http://www.dcs.shef.ac.uk/~sam/">Website</a>, <a href="mailto:sam@dcs.shef.ac.uk">Email</a>.
 * @version 1.1
 */
final class TokeniserWhitespace implements InterfaceTokeniser, Serializable {

    /**
     * stopWordHandler used by the tokenisation.
     */
    private InterfaceTermHandler stopWordHandler = new DummyStopTermHandler();

    /**
     * priavte delimitors for white space within a string.
     */
    private final String delimiters = "\r\n\t \u00A0";

    /**
     * displays the tokenisation method.
     *
     * @return the tokenisation method
     */
    public final String getShortDescriptionString() {
        return "TokeniserWhitespace";
    }

    /**
     * displays the delimiters used .
     *
     * @return the delimiters used
     */
    public final String getDelimiters() {
        return delimiters;
    }

    /**
     * gets the stop word handler used.
     * @return the stop word handler used
     */
    public InterfaceTermHandler getStopWordHandler() {
        return stopWordHandler;
    }

    /**
     * sets the stop word handler used with the handler given.
     * @param stopWordHandler the given stop word hanlder
     */
    public void setStopWordHandler(final InterfaceTermHandler stopWordHandler) {
        this.stopWordHandler = stopWordHandler;
    }

    /**
     * Return tokenized version of a string .
     *
     * @param input
     * @return tokenized version of a string
     */
    public final ArrayList<String> tokenizeToArrayList(final String input) {
        final ArrayList<String> returnVect = new ArrayList<String>();
        for(String s: input.split("\\s+"))
            returnVect.add(s.trim());
        return returnVect;
    }

    /**
     * Return tokenized set of a string.
     *
     * @param input
     * @return tokenized set of a string
     */
    public Set<String> tokenizeToSet(final String input) {
        final Set<String> returnSet = new HashSet<String>();
        returnSet.addAll(tokenizeToArrayList(input));
        return returnSet;
    }
}