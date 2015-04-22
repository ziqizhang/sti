/**
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Initial Developer of the Original Code is Sheffield University.
 * Portions created by Sheffield University are
 * Copyright &copy; 2005 Sheffield University (Web Intelligence Group)
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Neil Ireson (N.Ireson@dcs.shef.ac.uk)
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */

package uk.ac.shef.wit.ie.wrapper.html.xpath;

// Import log4j classes.

import org.w3c.dom.Node;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Class SeedNode
 * <p/>
 * Author: Neil Ireson (mailto:N.Ireson@dcs.shef.ac.uk)
 * Creation Date: 11-May-2007
 * Version: 0.1
 */
public class SeedNode extends AbstractNode
{
    /**
     * Define a static logger variable
     */
    static Logger logger = Logger.getLogger(SeedNode.class.getName());
    private static Map<HtmlNode, List<SeedNode>> htmlNodeToSeedNodes = new HashMap<HtmlNode, List<SeedNode>>();

    private final Seed seed;
    private final HtmlNode htmlNode;
    private final double textProportion;
    private final int textPosition;

    public static final int EQUALS = 0x01;
    public static final int STARTSWITH = 0x02;
    public static final int ENDSWITH = 0x04;

    public SeedNode(final Seed seed, final Node node)
    {
        this(seed, HtmlNode.getNode(node));
    }

    public SeedNode(final Seed seed, final HtmlNode node)
    {
        super(node.getNode());
        List<SeedNode> seedNodes = htmlNodeToSeedNodes.get(node);
        if (seedNodes == null)
        {
            seedNodes = new ArrayList<SeedNode>();
            htmlNodeToSeedNodes.put(node, seedNodes);
        }
        seedNodes.add(this);

        this.htmlNode = node;
        this.seed = seed;
        final String nodeText = node.getText();
        final String seedText = seed.getText();
        textProportion = (double) seedText.length() / nodeText.length();
        if (textProportion == 1)
        {
            textPosition = EQUALS;
        }
        else if (nodeText.startsWith(seedText))
        {
            textPosition = STARTSWITH;
        }
        else if (nodeText.endsWith(seedText))
        {
            textPosition = ENDSWITH;
        }
        else
        {
            textPosition = 0;
        }
    }

    public Seed getSeed()
    {
        return seed;
    }

    public HtmlNode getHtmlNode()
    {
        return htmlNode;
    }

    public double getTextProportion()
    {
        return textProportion;
    }

    public int getTextPosition()
    {
        return textPosition;
    }

    public String toString()
    {
        return toString("");
    }

    public String toString(final String indent)
    {
        return seed.toString(indent) + "\n" + getHtmlNode().toString(indent + INDENT);
    }

    public boolean equals(final Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        if (o instanceof Node)
        {
            return getNode().equals(o);
        }
        if (o instanceof SeedNode)
        {
            final SeedNode other = (SeedNode) o;
            return seed.equals(other.getSeed()) && getNode().equals(other.getNode());
        }
        else
        {
            return false;
        }
    }

    /**
     * Main method for command line interface.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
    }
}
