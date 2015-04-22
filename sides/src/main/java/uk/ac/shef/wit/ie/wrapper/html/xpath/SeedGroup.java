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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Class SeedGroup
 * <p/>
 * Author: Neil Ireson (mailto:N.Ireson@dcs.shef.ac.uk)
 * Creation Date: 09-May-2007
 * Version: 0.1
 */
@SuppressWarnings({"ClassWithoutLogger"})
public class SeedGroup
{
    public static String INDENT = "  ";

    private final List<Seed> seeds;
    private final SeedGroup parent;
    private final List<SeedGroup> subGroups = new ArrayList<SeedGroup>();
    private final List<Relation.Cardinality> subGroupCardinality = new ArrayList<Relation.Cardinality>();
    private final Map<Document, SeedNodeGroup> docSeedNodeGroup = new LinkedHashMap<Document, SeedNodeGroup>();

    public SeedGroup(final SeedGroup parent, final List<Seed> seeds)
    {
        this.parent = parent;
        if (seeds == null || seeds.isEmpty())
        {
            throw new RuntimeException("Attempting to construct SeedGroup from null or empty seed List");
        }

        this.seeds = seeds;
        for (final Seed seed : seeds)
        {
            seed.setGroup(this);
        }
    }

    public void addSeedNodeGroup(final Document document, final SeedNodeGroup seedNodeGroup)
    {
        docSeedNodeGroup.put(document, seedNodeGroup);
    }

    public SeedNodeGroup getDocumentNodes(final Document document)
    {
        return docSeedNodeGroup.get(document);
    }

    public Map<Seed, List<SeedNode>> getSeedNodes(final Document document)
    {
        final SeedNodeGroup seedNodeGroup = docSeedNodeGroup.get(document);
        if (seedNodeGroup != null)
        {
            return seedNodeGroup.getSeedNodes();
        }
        return null;
    }

    public List<SeedNode> getSeedNodes(final Document document, final Seed seed)
    {
        final SeedNodeGroup seedNodeGroup = docSeedNodeGroup.get(document);
        if (seedNodeGroup != null)
        {
            return seedNodeGroup.getSeedNodes(seed);
        }
        return null;
    }

    public Map<Seed, List<SeedNode>> getAllSeedNodes(final Document document)
    {
        final SeedNodeGroup seedNodeGroup = docSeedNodeGroup.get(document);
        if (seedNodeGroup != null)
        {
            return seedNodeGroup.getAllSeedNodes();
        }
        return null;
    }

    public List<Seed> getSeeds()
    {
        return seeds;
    }

    public List<Seed> getAllSeeds()
    {
        final List<Seed> allSeeds = new ArrayList<Seed>(seeds);
        for (final SeedGroup subGroup : subGroups)
        {
            allSeeds.addAll(subGroup.getAllSeeds());
        }
        return allSeeds;
    }

    public SeedGroup getParent()
    {
        return parent;
    }

    public List<SeedGroup> getSubGroups()
    {
        return subGroups;
    }

    public void addSubGroup(final SeedGroup subGroup, final Relation.Cardinality subGroupCardinality)
    {
        subGroups.add(subGroup);
        this.subGroupCardinality.add(subGroupCardinality);
    }

    public Relation.Cardinality getbGroupCardinality(final SeedGroup subGroup)
    {
        final int subGroupIndex = subGroups.indexOf(subGroup);
        if (subGroupIndex == -1)
        {
            return null;
        }
        return subGroupCardinality.get(subGroupIndex);
    }

    public String toString()
    {
        return toString("");
    }

    public String toString(final String indent)
    {
        final StringBuffer buffer = new StringBuffer();

        for (final Seed seed : seeds)
        {
            buffer.append("\n").append(indent).append(":").append(seed);
        }
        for (final SeedGroup seedGroup : subGroups)
        {
            buffer.append(seedGroup.toString(indent + INDENT));
        }

        return buffer.toString();
    }
}
