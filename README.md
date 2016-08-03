# Semantic Table Interpretation

### DISCLAIMER

This project is based on the work described at [1]. The original code referred to in the paper is now hosted and can be downloaded from [here](http://staffwww.dcs.shef.ac.uk/people/Z.Zhang/resources/tableminerplus/sti_2015.zip). The code has then been significantly improved, undergone a lot of refactorization and re-implementation, and therefore, may not work with the cache included in the published data [here](http://staffwww.dcs.shef.ac.uk/people/Z.Zhang/resources/tableminerplus/data.tar.gz). 

In addition to TableMiner+, this project provides implementation of several other semantic table interpretation algorithms, including: Joint Inference based on Liamye2011, and Semantic Message Passing based on Mulwad2013. **However, due to many things out of our control (e.g., use of in-house software in original works, different versions of knowledge bases), please note that we cannot guarantee identical replication of the original systems or reproduction of experiment results.**


### ACKNOWLEDGEMENT
Part of this work was funded by the EPSRC project LODIE - Linked Open Data for Information Extraction, EP/ J019488/1.

### LICENCE
Apache 2.0

### Quick Start
 - Place a copy of STI on your computer
 - Run maven to install two 3rd party libraries (in 'libs') to your local maven repository. See https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html and the maven plugin with id 'maven-install-plugin' for howto.
 - Download test data, from [here](http://staffwww.dcs.shef.ac.uk/people/Z.Zhang/resources/tableminerplus/data.tar.gz)
 - Unzip the test data, into e.g., [sti_data]
 - Navigate into [sti_data/dataset], unzip, depending on the test cases: imdb.tar.gz for the IMDB dataset; musicbrainz.tar.gz for the MusicBrainz dataset; Limaye200.tar.gz for the Limaye200 dataset; Limaye_complete.tar.gz for the LimayeAll dataset
 - Configure your local copy of STI
 - - open 'sti.properties', as a minimum, you need to change 'sti.home', and 'sti.cache.main.dir'. Please follow the documentation inside the property file
 - - open 'kbsearch.properties', as a minimum, you need to change 'kb.search.result.stoplistfile', and 'fb.query.api.key' to use your own Freebase API key
 - - open 'websearch.properties', as a minimum, you need to change 'bing.keys' to use your own bing web search key
 - Run a test case. For example, to run TMP, use:  **uk.ac.shef.dcs.sti.experiment.TableMinerPlusBatch "[sti_data/Limaye200]" "[output_dir]" "/[sit_home_dir]/sti.properties"**

**Note:** 'sti.properties' distributed with code is a default configuration for Limaye200 and LimayeAll datasets; for IMDB and MusicBrainz datasets, you can edit a template inside '/resources'. For both IMDB and MusicBrainz, you may want to provide the VM variable '-Djava.util.logging.config.file=' to configure the logging output of the any23-sti module (which can produce too many logs).
 
[1] Under minor revision review: http://www.semantic-web-journal.net/content/effective-and-efficient-semantic-table-interpretation-using-tableminer-0
