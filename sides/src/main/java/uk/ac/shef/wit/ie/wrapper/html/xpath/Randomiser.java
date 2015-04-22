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

import java.util.Collection;
import java.util.Random;

/**
 * Class Randomiser
 * <p/>
 * Author: Neil Ireson (mailto:N.Ireson@dcs.shef.ac.uk)
 * Creation Date: 19-May-2007
 * Version: 0.1
 */
@SuppressWarnings({"ClassWithoutLogger"})
public class Randomiser <T>
{

    public T[] RandomShuffle(final Collection<T> collection, final Random random)
    {
        if (collection == null)
        {
            throw new NullPointerException("collection");
        }

        if (random == null)
        {
            throw new NullPointerException("random");
        }

        final T[] array = (T[]) collection.toArray();
        return UncheckedRandomShuffle(array, random);
    }

    public T[] RandomShuffle(final T[] array, final Random random)
    {
        if (array == null)
        {
            throw new NullPointerException("array");
        }

        if (random == null)
        {
            throw new NullPointerException("random");
        }

        return UncheckedRandomShuffle(array, random);
    }

    private T[] UncheckedRandomShuffle(final T[] array, final Random random)
    {
        for (int i = array.length - 1; i >= 0; i--)
        {
            final int j = random.nextInt(i + 1);

            if (i != j)
            {
                final T temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }

        return array;
    }

}
