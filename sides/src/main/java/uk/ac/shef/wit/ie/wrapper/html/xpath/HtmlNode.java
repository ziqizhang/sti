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
import org.w3c.dom.Element;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.Map;
import java.util.HashMap;

/**
 * Class HtmlNode
 * <p/>
 * Author: Neil Ireson (mailto:N.Ireson@dcs.shef.ac.uk)
 * Creation Date: 09-May-2007
 * Version: 0.1
 */
@SuppressWarnings({"ClassWithoutLogger"})
public class HtmlNode extends AbstractNode
{
    private static Map<Node, HtmlNode> nodeToHtmlNode = new HashMap<Node, HtmlNode>();
    private static final XPathExpression ahrefAncestor;
    private static final XPathExpression imgsrcDescendant;

    static
    {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        try
        {
            ahrefAncestor = xpath.compile(".//ancestor::a[@href]");
            imgsrcDescendant = xpath.compile(".//img[@src]");
        }
        catch (XPathExpressionException e)
        {
            throw new RuntimeException("Failed to initialise XPath Expressions...\n", e);
        }
    }

    private Element ahrefElement;
    private Element imgsrcElement;

    private HtmlNode(final Node node)
    {
        super(node);

        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            if ("img".equalsIgnoreCase(node.getNodeName()))
            {
                imgsrcElement = (Element) node;
            }
        }

        try
        {
            ahrefElement = (Element) ahrefAncestor.evaluate(node, XPathConstants.NODE);
            if (ahrefElement != null && imgsrcElement == null)
            {
                imgsrcElement = (Element) imgsrcDescendant.evaluate(ahrefElement, XPathConstants.NODE);
            }
        }
        catch (XPathExpressionException e)
        {
            e.printStackTrace();
        }
    }

    public static HtmlNode getNode(final Node node)
    {
        HtmlNode cacheNode = nodeToHtmlNode.get(node);
        if (cacheNode == null)
        {
            cacheNode = new HtmlNode(node);
            nodeToHtmlNode.put(node, cacheNode);
        }
        return cacheNode;
    }

    public void setAHrefElement(final Element ahrefElement)
    {
        this.ahrefElement = ahrefElement;
    }

    public Element getAHrefElement()
    {
        return ahrefElement;
    }

    public void setImgSrcElement(final Element imgsrcElement)
    {
        this.imgsrcElement = imgsrcElement;
    }

    public Element getImgSrcElement()
    {
        return imgsrcElement;
    }

    public String getText()
    {
        return getNode() == null ? "" :
               (getNode() == imgsrcElement ? imgsrcElement.getAttribute("alt") : getNode().getTextContent()).trim();
    }

    public String toString()
    {
        return toString("");
    }

    public String toString(final String indent)
    {
        return indent + DOMUtil.getXPath(getNode()) + "\n" + indent +
               getText() + ": " +
               (ahrefElement == null ? "" : ahrefElement.getAttribute("href")) + ": " +
               (imgsrcElement == null ? "" : imgsrcElement.getAttribute("src"));
    }

}
