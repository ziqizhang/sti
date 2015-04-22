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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class XMLUtil
 * <p/>
 * Author: Neil Ireson (mailto:N.Ireson@dcs.shef.ac.uk)
 * Creation Date: 17-May-2007
 * Version: 0.1
 */
@SuppressWarnings({"ClassWithoutLogger"})
public class XMLUtil
{
    public static void transform(final String xsltFilename, final String xmlFilename, final Writer writer)
            throws TransformerException, IOException
    {
        final File xsltFile = new File(xsltFilename);
        final File xmlFile = new File(xmlFilename);

        if (!xsltFile.isFile())
        {
            throw new IOException("XLST file is not a normal file: " + xsltFile.getCanonicalPath());
        }
        if (!xmlFile.isFile())
        {
            throw new IOException("XML file is not a normal file: " + xmlFile.getCanonicalPath());
        }
        transform(xsltFile, xmlFile, writer);
    }

    public static void transform(final File xsltFile, final File xmlFile, final Writer writer)
            throws TransformerException
    {
        transform(new StreamSource(xsltFile), new StreamSource(xmlFile), writer);
    }

    public static void transform(final Source xsltSource, final Source xmlSource, final Writer writer)
                throws TransformerException
        {
        // the factory pattern supports different XSLT processors
        final TransformerFactory transFact = TransformerFactory.newInstance();
        final Transformer trans = transFact.newTransformer(xsltSource);
        //System.out.println(org.theshoemakers.which4j.Which4J.which(trans.getClass()));
        final Result result = new StreamResult(writer);
        trans.transform(xmlSource, result);
    }

    public static void htmlTransform(final Document doc, final Writer writer)
            throws TransformerException
    {
        Source xmlSource = new DOMSource(doc);
        Source xsltSource = new DOMSource(getHTMLTransform(doc.getImplementation()));
        Result result = new StreamResult(writer);

        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);
        trans.transform(xmlSource, result);

    }

    private static Document getHTMLTransform(DOMImplementation impl){
        String xslt = "http://www.w3.org/1999/XSL/Transform";

        Document doc = impl.createDocument(xslt,"stylesheet",null);

        doc.getDocumentElement().setAttribute("version","2.0");

        Element output = doc.createElementNS(xslt,"output");
        output.setAttribute("method","html");
        output.setAttribute("version","4.0");
        output.setAttribute("encoding","UTF-8");
        output.setAttribute("doctype-public","-//W3C//DTD HTML 4.01 Strict//EN");
        output.setAttribute("doctype-system","http://www.w3.org/TR/html4/strict.dtd");
        doc.getDocumentElement().appendChild(output);

        Element template = doc.createElementNS(xslt,"template");
        template.setAttribute("match","/");
        doc.getDocumentElement().appendChild(template);

        Element copyof = doc.createElementNS(xslt,"copy-of");
        copyof.setAttribute("select",".");
        template.appendChild(copyof);

        return doc;
    }

    public static String usage()
    {
        return "java " + XMLUtil.class.getName() + "\n" +
               "\t-transform <xsltfile> <xmlfile>   : transform the xmlfile using the xsltfile";
    }

    /**
     * Main method for command line interface.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
        int argIndex = 0;
        try
        {
        while (argIndex < args.length)
        {
            final String argument = args[argIndex];
            if ("-transform".equalsIgnoreCase(argument))
            {
                if ((argIndex + 2) > args.length)
                {
                    throw new IllegalArgumentException("Insufficient argument for transform");
                }
                final String xsltFilename = args[++argIndex];
                final String xmlFilename = args[++argIndex];
                if ("-".equals(xmlFilename))
                {
                    XMLUtil.transform(
                            new StreamSource(new File(xsltFilename)), 
                            new StreamSource(new InputStreamReader(System.in)),
                            new PrintWriter(System.out));
                }
                XMLUtil.transform(xsltFilename, xmlFilename, new PrintWriter(System.out));
            }
            else if ("-?".equals(argument))
            {
                System.out.println(usage());
            }
            else
            {
                throw new IllegalArgumentException("Unknown argument: " + argument);                
            }
            argIndex++;
        }
        }
        catch (IllegalArgumentException e)
        {
            System.err.println(e.getMessage());
            System.err.println(usage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
