package uk.ac.shef.wit.feeds.utils;
/**
 *  Copyright &copy;2012 Sheffield University (OAK Group)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 *
 * Contributor(s):
 *   Neil Ireson (N.Ireson@dcs.shef.ac.uk)
 *
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ShutdownUtils class
 */
public class ShutdownUtils
{
    private static final Logger logger = LoggerFactory.getLogger(ShutdownUtils.class);

    private static ShutdownThread shutdownThread = null;

    public static void addShutdownListener(ShutdownListener shutdownListener)
    {
        if (shutdownThread == null)
        {
            shutdownThread = new ShutdownThread();
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }
        shutdownThread.addListener(shutdownListener);

        logger.info("Added Shutdown Listener: {}", shutdownListener.getClass().getCanonicalName());
    }

    public static void removeShutdownListener(ShutdownListener shutdownListener)
    {
        if (shutdownThread == null || !shutdownThread.removeListener(shutdownListener))
        {
            logger.warn("Did not remove Shutdown Listener as it was not in list of listeners: {}",
                        shutdownListener.getClass().getCanonicalName());
        }
        else
        {
            if (shutdownThread.size() == 0)
            {
                Runtime.getRuntime().removeShutdownHook(shutdownThread);
                shutdownThread = null;
            }
            logger.warn("Removed Shutdown Listener: {}",
                        shutdownListener.getClass().getCanonicalName());
        }
    }
}

class ShutdownThread
        extends Thread
{
    private final List<ShutdownListener> shutdownListeners = new ArrayList<ShutdownListener>();

    public int size()
    {
        return shutdownListeners.size();
    }

    public void addListener(ShutdownListener shutdownListener)
    {
        shutdownListeners.add(shutdownListener);
    }

    public boolean removeListener(ShutdownListener shutdownListener)
    {
        return shutdownListeners.remove(shutdownListener);
    }

    public void run()
    {
        for (ShutdownListener listener : shutdownListeners)
        {
            listener.shutdown();
        }
    }
}
