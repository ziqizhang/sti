/*
 * Copyright 2011 OAK Group, University of Sheffield
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package uk.ac.shef.wit.feeds;

//import org.apache.solr.common.SolrInputDocument;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedDocument
{
    private static final Logger logger = LoggerFactory.getLogger(FeedDocument.class);

    public String id;
    public String source;
    public String userid;
    public String inReplyToId;
    public String inReplyToUserId;
    public String username;
    public Date creationDate;
    public Date sourceDate;
    public Date uploadedDate;
    public String imageUrl;
    public String thumbnailURL;
    public String title;
    public String originalText;
    public String text;
    public String cleanedText;
    public String audioURL;
    public String videoURL;
    public String language;
    public String link;
    public Double latitude;
    public Double longitude;
    public Integer localisationAccuracy;
    public String localisationName;
    public List<String> locations = new ArrayList<String>();
    public List<String> tags = new ArrayList<String>();
    public List<String> autoTags = new ArrayList<String>();
    public List<String> userMentions = new ArrayList<String>();
    public List<String> urls = new ArrayList<String>();
    public Integer repeatedCount;
    public String comment;
    public Integer rating;
    public Boolean spam;
    public List<String> contentType = new ArrayList<String>();
    public String sentiment;
    public String sentimentStrict;
    public Long sentimentNorm;
    public Long sentimentRaw;
    public List<String> nlpLocationName = new ArrayList<String>();
    public List<String> nlpLocationType = new ArrayList<String>();




    private enum RETURN_VALUE
    {
        min, max, value
    }



}
