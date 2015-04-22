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

import java.util.regex.Pattern;

/**
 * Class Seed
 * <p/>
 * Author: Neil Ireson (mailto:N.Ireson@dcs.shef.ac.uk)
 * Creation Date: 09-May-2007
 * Version: 0.1
 */
@SuppressWarnings({"ClassWithoutLogger"})
public class Seed
{
    private final Concept concept;
    private final String text;
    private final String xpathContainsText;
    private final String xpathAltContainsText;
    private final Pattern pattern;
    private final double minTextProportion;
    private SeedGroup group;

    public Seed(final Concept concept, final String text)
    {
        this(concept, text, "contains(.,'" + text + "')", "contains(@alt,'" + text + "')", null, 0);
    }

    public Seed(final Concept concept, final String text, final String xpathContainsText)
    {
        this(concept, text, xpathContainsText, "contains(@alt,'" + text + "')", null, 0);
    }

    public Seed(final Concept concept, final String text,
                final String xpathContainsText, final String xpathAltContainsText)
    {
        this(concept, text, xpathContainsText, xpathAltContainsText, null, 0);
    }

    public Seed(final Concept concept, final String text,
                final String xpathContainsText, final String xpathAltContainsText,
                final Pattern pattern)
    {
        this(concept, text, xpathContainsText, xpathAltContainsText, pattern, 0);
    }

    public Seed(final Concept concept, final String text,
                final String xpathContainsText, final String xpathAltContainsText,
                final Pattern pattern, final double minTextProportion)
    {
        this.concept = concept;
        this.text = text;
        this.xpathContainsText = xpathContainsText;
        this.xpathAltContainsText = xpathAltContainsText;
        this.pattern = pattern;
        this.minTextProportion = minTextProportion;
        group = null;
    }

    public void setGroup(final SeedGroup group)
    {
        if (group == null)
        {
            throw new RuntimeException("Cannot reset a seeds group.");
        }
        this.group = group;
    }

    public SeedGroup getGroup()
    {
        return group;
    }

    public Concept getConcept()
    {
        return concept;
    }

    public String getText()
    {
        return text;
    }

    public String getXPathContainsText()
    {
        return xpathContainsText;
    }

    public String getXPathAltContainsText()
    {
        return xpathAltContainsText;
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public double getMinTextProportion()
    {
        return minTextProportion;
    }

    public String toString()
    {
        return toString("");
    }

    public String toString(final String indent)
    {
        return indent + concept + ": " + text;
    }
}