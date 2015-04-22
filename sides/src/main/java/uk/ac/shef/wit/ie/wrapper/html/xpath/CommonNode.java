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

import org.w3c.dom.Node;

import java.util.List;
import java.util.ArrayList;

/**
 * Class CommonNode
 * <p/>
 * Author: Neil Ireson (mailto:N.Ireson@dcs.shef.ac.uk)
 * Creation Date: 13-May-2007
 * Version: 0.1
 */
@SuppressWarnings({"ClassWithoutLogger"})
public class CommonNode extends AbstractNode
{
    SeedNodeGroup seedNodeGroup;
    private CommonNode parent;
    private AbstractNode[] nodes;

    public CommonNode(final SeedNodeGroup seedNodeGroup, final Node commonNode, final AbstractNode[] nodes)
    {
        super(commonNode);
        this.seedNodeGroup = seedNodeGroup;
        this.nodes = nodes;
        parent = null;
        if (nodes instanceof CommonNode[])
        {
            for (final CommonNode node : (CommonNode[]) nodes)
            {
                node.parent = this;
            }
        }
    }

    public AbstractNode[] getNodes()
    {
        return nodes;
    }

    public CommonNode getParent()
    {
        return parent;
    }

    List<SeedNode> getSeedNodes()
    {
        final List<SeedNode> seedNodes = new ArrayList<SeedNode>(nodes.length);
        if (nodes instanceof SeedNode[])
        {
            for (final SeedNode seedNode : (SeedNode[]) nodes)
            {
                seedNodes.add(seedNode);
            }
        }
        else if (nodes instanceof CommonNode[])
        {
            for (final CommonNode commonNode : (CommonNode[]) nodes)
            {
                // Note that it is not possible to determine if an instance is of a particular invocation
                // of a generic type, i.e. (nodes instanceof CommonNode<CommonNode>[]) is illegal
                // so the following method will produce an "unchecked warning"
                seedNodes.addAll(commonNode.getSeedNodes());
            }
        }
        return seedNodes;
    }

    public void collectCommonNodes(final List<CommonNode> commonNodes)
    {
        commonNodes.add(this);
        if (!isLeafNode())
        {
            for (final CommonNode commonNode : (CommonNode[]) nodes)
            {
                commonNode.collectCommonNodes(commonNodes);
            }
        }
    }

    public void collectNodes(final List<AbstractNode> nodes)
    {
        nodes.add(this);
        if (isLeafNode())
        {
            for (final SeedNode seedNode : (SeedNode[]) this.nodes)
            {
                nodes.add(seedNode);
            }
        }
        else
        {
            for (final CommonNode commonNode : (CommonNode[]) this.nodes)
            {
                commonNode.collectNodes(nodes);
            }
        }
    }

    public boolean isLeafNode()
    {
        return (nodes instanceof SeedNode[]);
    }

    List<CommonNode> getLeafCommonNodes()
    {
        final List<CommonNode> commonNodes = new ArrayList<CommonNode>(nodes.length);
        if (nodes instanceof CommonNode[])
        {
            for (final CommonNode commonNode : (CommonNode[]) nodes)
            {
                if (commonNode.isLeafNode())
                {
                    commonNodes.add(commonNode);
                }
                else
                {
                    commonNodes.addAll(commonNode.getLeafCommonNodes());
                }
            }
        }
        return commonNodes;
    }

    public String toString()
    {
        return toString("");
    }

    public String toString(final String indent)
    {
        final StringBuffer buffer = new StringBuffer();

        buffer.append(indent).append(DOMUtil.getXPath(getNode()));
        for (final AbstractNode node : nodes)
        {
            buffer.append("\n").append(node.toString(indent + INDENT));
        }

        return buffer.toString();
    }

}
