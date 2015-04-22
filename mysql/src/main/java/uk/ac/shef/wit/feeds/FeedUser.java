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

import java.net.URL;
import java.util.Date;

public class FeedUser
{
    public String userid;
    public String username;
    public String realname;
    public String language;
    public String location;
    public URL imageUrl;
    public URL thumbnailURL;
    public String description;
    public Date creationDate;
    public Integer documentsCount;
    public Integer friendsCount;
    public Integer followersCount;
    public boolean following = false;
    public boolean blacklist = false;
}
