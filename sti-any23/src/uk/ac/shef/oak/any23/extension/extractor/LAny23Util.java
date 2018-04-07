package uk.ac.shef.oak.any23.extension.extractor;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 30/10/12
 * Time: 09:42
 */
public class LAny23Util {

    /**
     *
     * @param attributeName
     * @return   /@attributeName
     */
    public static String appendAttributeToXPath(String attributeName){
        return "/@"+attributeName;
    }

    /**
     *
     * @return  /text()
     */
    public static String appendTagTextValueToXPath(){
        return "/text()";
    }

    public static String appendArbitraryDocument(){
        return "/[this]";
    }

    /**
     * source:
     * http://stackoverflow.com/questions/5046174/get-xpath-from-the-org-w3c-dom-node
     * http://lekkimworld.com/2007/06/19/building_xpath_expression_from_xml_node.html
     *
     * @param n
     * @return
     */
    /*public static String getFullXPath(Node n) {
        // abort early
        if (null == n)
            return null;

        // declarations
        Node parent = null;
        Stack<Node> hierarchy = new Stack<Node>();
        StringBuffer buffer = new StringBuffer();

        // push element on stack
        hierarchy.push(n);

        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                parent = ((Attr) n).getOwnerElement();
                break;
            case Node.ELEMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.DOCUMENT_NODE:
                parent = n.getParentNode();
                break;
            default:
                throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
        }

        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            // push on stack
            hierarchy.push(parent);

            // get parent of parent
            parent = parent.getParentNode();
        }

        // construct xpath
        Object obj = null;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            boolean handled = false;

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;

                // is this the root element?
                if (buffer.length() == 0) {
                    // root element - simply append element name
                    buffer.append(node.getNodeName());
                } else {
                    // child element - append slash and element name
                    buffer.append("/");
                    buffer.append(node.getNodeName());

                    if (node.hasAttributes()) {
                        // see if the element has a name or id attribute
                        if (e.hasAttribute("id")) {
                            // id attribute found - use that
                            buffer.append("[@id='" + e.getAttribute("id") + "']");
                            handled = true;
                        } else if (e.hasAttribute("name")) {
                            // name attribute found - use that
                            buffer.append("[@name='" + e.getAttribute("name") + "']");
                            handled = true;
                        }
                    }

                    if (!handled) {
                        // no known attribute we could use - get sibling index
                        int prev_siblings = 1;
                        Node prev_sibling = node.getPreviousSibling();
                        while (null != prev_sibling) {
                            if (prev_sibling.getNodeType() == node.getNodeType()) {
                                if (prev_sibling.getNodeName().equalsIgnoreCase(
                                        node.getNodeName())) {
                                    prev_siblings++;
                                }
                            }
                            prev_sibling = prev_sibling.getPreviousSibling();
                        }
                        buffer.append("[" + prev_siblings + "]");
                    }
                }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                buffer.append("/@");
                buffer.append(node.getNodeName());
            }
        }
        // return buffer
        return buffer.toString();

    }*/
    public static String getFullXPath(Node n){
        return DomUtils.getXPathForNode(n);
    }

}
