package uk.ac.shef.wit.feeds;

import uk.ac.shef.wit.feeds.utils.ShutdownListener;

import java.io.IOException;

/**
 * Copyright &copy;2012 Sheffield University (OAK Group)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * <p/>
 * Contributor(s):
 * Neil Ireson (N.Ireson@dcs.shef.ac.uk)
 */
public interface DataServer
    extends ShutdownListener
{
    //todo abstract the data server so that the code is not dependent on a specific version of SOLR.or in fact SOLR at all.

    public void init(String host, int port, String path, String username, String password, boolean overwrite)
            throws IOException;

    public void addDocument(FeedDocument feedDocument, FeedUser feedUser)
            throws IOException;

    public void addRawDocument(String id, byte[] content)
            throws IOException;

    public void commitRawDocument()
            throws IOException;

    public int getDocumentCount();
}
