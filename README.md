# sti
Implementation of algorithms for semantic table implementation

# To test TableMiner+ on top of HTML tables from IMDB:
- download the cache, and experimental datasets, from: http://staffwww.dcs.shef.ac.uk/people/Z.Zhang/resources/tableminerplus/data.tar.gz
- unpack data.tar.gz, then keep going to unpack contained archieves (only those related to IMDB data - cache-imdb, dataset-imdb)
- run uk.ac.shef.dcs.oak.sti.experiment.TestTableInterpretation_IMDB with the following parameters (if you use intellij, open the project folder /sti/core, it should load all the app configurations and that saves you lots of time)
uk.ac.shef.dcs.oak.sti.experiment.TestTableInterpretation_IMDB 1 2 3 4 5 6
1: create the input folder which contains one HTML page from IMDB, e.g., http://www.imdb.com/title/tt0133093/ . Specify path to this folder as first param 
2: create the output folder, specify the path to this folder as second param.
3: create freebase.properties file and introduce a path to it (see below for the structure fo the file)
4: the cache folder to save and re-use data gathered from freebase (taken from the unziped archive data.tar.gz): “.../cache/cache/tableminer_cache_imdb/solrindex_cache/zookeeper/solr"
5: nlp resources (taken from the unziped archive data.tar.gz): “…/sti/resources/nlp_resources"
6: just put “0”, meaning which webpage to start with (so that in case your program breaks half-way you can resume)
7: put “false”, which means do not learn relations across columns (imdb dataset has only 1 data column)

# Freebase properties file structure
BING_API_KEYS=khkFliAQ0J5RBQeUWEWTOJ4afIF1t1hDTKivb3ExAhQ
FREEBASE_API_KEY=AIzaSyBgOUGvZTEzlkgks4S3Juj-JpI60acEfRs
FREEBASE_MAX_QUERY_PER_SECOND=10
FREEBASE_MAX_QUERY_PER_DAY=100000
FREEBASE_MQL_QUERY_URL=https://www.googleapis.com/freebase/v1/mqlread
FREEBASE_TOPIC_QUERY_URL=https://www.googleapis.com/freebase/v1/topic
FREEBASE_SEARCH_QUERY_URL=https://www.googleapis.com/freebase/v1/search
FREEBASE_HOMEPAGE=http://www.freebase.com
FREEBASE_LIMIT=1000

Note: Do not forget to change your API_KEY (you may get one) and, also BING_API_KEYS. For the latter, you can obtain one for free from https://datamarket.azure.com/dataset/bing/search.

