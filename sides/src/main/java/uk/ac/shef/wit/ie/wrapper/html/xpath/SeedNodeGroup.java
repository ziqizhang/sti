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

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPath;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * Class SeedNodeGroup
 * <p/>
 * Author: Neil Ireson (mailto:N.Ireson@dcs.shef.ac.uk)
 * Creation Date: 09-May-2007
 * Version: 0.1
 */
public class SeedNodeGroup
{
    static Logger logger = Logger.getLogger(IEXPathWrapper.class.getName());

    private final List<SeedNodeGroup> subGroups;
    private final SeedGroup seedGroup;
    private final Document document;
    private final Map<Seed, List<SeedNode>> seedNodes;
    private SeedNodeGroup parent;
    private List<CommonNode> commonSeedNodeNodes;
    private List<CommonNode> commonSubGroupNodes;

    /**
     * Constructor creates a <code>SeedNodeGroup</code> for the <code>SeedGroup</code>
     * (and all sub-groups of that <code>SeedGroup</code>) from the document.
     *
     * @param seedGroup <code>SeedGroup</code>
     * @param document  Document from which the nodes are extracted.
     * @param xpath     The <code>XPath</code> object used in the extraction.
     * @throws IOException              If there are no nodes for at least one of the seeds
     * @throws XPathExpressionException If the <code>XPath</code> expression is illegal
     * @throws SeedNotFoundException    If a seed is not found in the document nodes.
     */
    public SeedNodeGroup(final SeedGroup seedGroup, final Document document, final XPath xpath)
            throws XPathExpressionException, IOException, SeedNotFoundException
    {
        this(null, seedGroup, document, xpath);
    }

    /**
     * Constructor creates a <code>SeedNodeGroup</code> for the <code>SeedGroup</code>
     * (and all sub-groups of that <code>SeedGroup</code>) from the document.
     *
     * @param parent    Parent <code>SeedNodeGroup</code>
     * @param seedGroup <code>SeedGroup</code>
     * @param document  Document from which the nodes are extracted.
     * @param xpath     The <code>XPath</code> object used in the extraction.
     * @throws IOException              If there are no nodes for at least one of the seeds
     * @throws XPathExpressionException If the <code>XPath</code> expression is illegal
     * @throws SeedNotFoundException    If a seed is not found in the document nodes.
     */
    private SeedNodeGroup(final SeedNodeGroup parent, final SeedGroup seedGroup, final Document document,
                          final XPath xpath)
            throws XPathExpressionException, IOException, SeedNotFoundException
    {
        subGroups = new ArrayList<SeedNodeGroup>();
        for (final SeedGroup seedSubGroup : seedGroup.getSubGroups())
        {
            final SeedNodeGroup subGroup = new SeedNodeGroup(this, seedSubGroup, document, xpath);
            subGroups.add(subGroup);
        }

        this.parent = parent;
        this.seedGroup = seedGroup;
        this.document = document;
        seedNodes = new LinkedHashMap<Seed, List<SeedNode>>(seedGroup.getSeeds().size());
        commonSeedNodeNodes = null;
        commonSubGroupNodes = null;

        for (final Seed seed : seedGroup.getSeeds())
        {
            seedNodes.put(seed, new ArrayList<SeedNode>());
        }

        for (final Seed seed : seedGroup.getSeeds())
        {
            // Get all nodes that contain the string, ignoring the contents of any subelements
            final String seedXPath = "//child::text()[" + seed.getXPathContainsText() + "] | " +
                                     "//img[@src and " + seed.getXPathAltContainsText() + "]";
            // Get the matching elements
            System.out.println("Searching for : " + seedXPath);
            final XPathExpression expr = xpath.compile(seedXPath);

            final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            addNodes(seed, nodeList);
            if (seedNodes.get(seed).isEmpty())
            {
                throw new SeedNotFoundException("No nodes found for seed: " + seed);
            }
        }
    }

    public void addToSeedGroup()
    {
        for (final SeedNodeGroup subGroup : subGroups)
        {
            subGroup.addToSeedGroup();
        }
        seedGroup.addSeedNodeGroup(document, this);
    }

    public boolean hasEmptySeedNodes()
    {
        for (final SeedNodeGroup subGroup : subGroups)
        {
            if (subGroup.hasEmptySeedNodes())
            {
                return true;
            }
        }

        for (final Seed seed : seedNodes.keySet())
        {
            if (seedNodes.get(seed).isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    public void calculateCommonNodes()
    {
        for (final SeedNodeGroup subGroup : subGroups)
        {
            subGroup.calculateCommonNodes();
        }

        calculateCommonNodesBetweenSeedNodes();

        // if there are no sub-groups
        if (seedGroup.getSubGroups().isEmpty())
        {
            commonSubGroupNodes = commonSeedNodeNodes;
        }
        else
        {
            calculateCommonNodesBetweenSeedNodesAndSubGroups();
        }
    }

    private void calculateCommonNodesBetweenSeedNodes()
    {
        commonSeedNodeNodes = new ArrayList<CommonNode>();

        final int[] listIndex = new int[seedNodes.size()];
        final int[] listSize = new int[seedNodes.size()];

        int i = 0;
        for (final Seed seed : seedNodes.keySet())
        {
            final List<SeedNode> seedNodes = this.seedNodes.get(seed);
            listSize[i] = seedNodes.size();
            i++;
        }
        boolean finished = false;
        while (!finished)
        {
            i = 0;
            final SeedNode[] seedNodeArray = new SeedNode[seedGroup.getSeeds().size()];
            final Node[] nodes = new Node[seedGroup.getSeeds().size()];
            for (final Seed seed : seedNodes.keySet())
            {
                final List<SeedNode> seedNodes = this.seedNodes.get(seed);
                nodes[i] = seedNodes.get(listIndex[i]).getNode();
                seedNodeArray[i] = seedNodes.get(listIndex[i]);
                i++;
            }

            final Node commonAncestor = DOMUtil.getCommonAncestor(nodes);
            if (commonAncestor != null)
            {
                final CommonNode commonNode = new CommonNode(this, commonAncestor, seedNodeArray);
                commonSeedNodeNodes.add(commonNode);
            }

            i = 0;
            for (; i < listIndex.length; i++)
            {
                listIndex[i]++;
                if (listIndex[i] == listSize[i])
                {
                    listIndex[i] = 0;
                }
                else
                {
                    break;
                }
            }
            if (i == listIndex.length)
            {
                finished = true;
            }
        }
    }

    private void calculateCommonNodesBetweenSeedNodesAndSubGroups()
    {
        commonSubGroupNodes = new ArrayList<CommonNode>();

        final int[] listIndex = new int[seedGroup.getSubGroups().size() + 1];
        final int[] listSize = new int[seedGroup.getSubGroups().size() + 1];

        listSize[0] = commonSeedNodeNodes.size();
        for (int i = 1; i <= seedGroup.getSubGroups().size(); i++)
        {
            final SeedGroup subGroup = seedGroup.getSubGroups().get(i - 1);
            listSize[i] = subGroup.getDocumentNodes(document).commonSubGroupNodes.size();
        }

        boolean finished = false;
        while (!finished)
        {
            final CommonNode[] commonNodes = new CommonNode[seedGroup.getSubGroups().size() + 1];
            final Node[] nodes = new Node[seedGroup.getSubGroups().size() + 1];
            commonNodes[0] = this.commonSeedNodeNodes.get(listIndex[0]);
            nodes[0] = commonNodes[0].getNode();
            for (int i = 1; i <= seedGroup.getSubGroups().size(); i++)
            {
                final SeedGroup subGroup = seedGroup.getSubGroups().get(i - 1);
                commonNodes[i] = subGroup.getDocumentNodes(document).commonSubGroupNodes.get(listIndex[i]);
                nodes[i] = commonNodes[i].getNode();
            }

            final Node commonAncestor = DOMUtil.getCommonAncestor(nodes);
            if (commonAncestor != null)
            {
                final CommonNode commonNode = new CommonNode(this, commonAncestor, commonNodes);
                commonSubGroupNodes.add(commonNode);
            }

            int i = 0;
            for (; i < listIndex.length; i++)
            {
                listIndex[i]++;
                if (listIndex[i] == listSize[i])
                {
                    listIndex[i] = 0;
                }
                else
                {
                    break;
                }
            }
            if (i == listIndex.length)
            {
                finished = true;
            }
        }
    }

    private void addNodes(final Seed seed, final NodeList nodeList)
    {
        final List<SeedNode> seedNodes = this.seedNodes.get(seed);
        if (seedNodes == null)
        {
            throw new RuntimeException
                    ("Cannot add nodes seed: " + seed + "\nNot part of seed group: " + seedGroup);
        }

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            final Node node = nodeList.item(i);
            final SeedNode seedNode = new SeedNode(seed, node);
            if (seed.getMinTextProportion() <= seedNode.getTextProportion() &&
                (seed.getPattern() == null || seed.getPattern().matcher(seedNode.getHtmlNode().getText()).matches()))
            {
                seedNodes.add(seedNode);
            }
        }
    }

    public boolean removeNode(final Seed seed, final Node node)
    {
        final List<SeedNode> seedNodes = this.seedNodes.get(seed);
        if (seedNodes == null)
        {
            throw new RuntimeException
                    ("Cannot remove nodes seed: " + seed + "\nNot part of seed group: " + seedGroup);
        }

        return seedNodes.remove(new SeedNode(seed, node));
    }

    public SeedNodeGroup getParent()
    {
        return parent;
    }

    public Document getDocument()
    {
        return document;
    }

    public SeedGroup getSeedGroup()
    {
        return seedGroup;
    }

    public Map<Seed, List<SeedNode>> getSeedNodes()
    {
        return seedNodes;
    }

    public List<SeedNode> getSeedNodes(final Seed seed)
    {
        return seedNodes.get(seed);
    }

    public Map<Seed, List<SeedNode>> getAllSeedNodes()
    {
        final Map<Seed, List<SeedNode>> allSeedNodes = new LinkedHashMap<Seed, List<SeedNode>>(seedNodes);
        for (final SeedNodeGroup subGroup : subGroups)
        {
            final Map<Seed, List<SeedNode>> subGroupSeedNodes = subGroup.getAllSeedNodes();
            for (final Seed seed : subGroupSeedNodes.keySet())
            {
                final List<SeedNode> seedNodes = subGroupSeedNodes.get(seed);
                allSeedNodes.put(seed, seedNodes);
            }
        }

        return allSeedNodes;
    }

    public List<CommonNode> getCommonSubGroupNodes()
    {
        return commonSubGroupNodes;
    }

    public String toString()
    {
        return document.getDocumentURI() + "\n" + seedGroup;
    }
}
