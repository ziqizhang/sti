# Semantic Table Interpretation
This repository contains implementation of the TableMiner+ system (see below), which implements a novel relational table annotation method that given an existing knowledge base, 1) links mentions in table cells into named entities or their properties in the knowledge base; 2) annotates columns using concepts; 3) annotates relations between columns.

**Keywords**: webtable, web table, entity linking, table annotation, table interpretation, semantic web, classification, relation extraction

### CITING
[1] Zhang, Z. 2017. Effective and efficient semantic table interpretation using tableminer+. Semantic Web 8 (6), 921-957

### DISCLAIMER

This project is based on the TableMiner+ system described in [1]. In addition to TableMiner+, this project provides implementation of several other semantic table interpretation algorithms, including: Joint Inference based on Liamye2011, and Semantic Message Passing based on Mulwad2013. **However, due to many things out of our control (e.g., use of in-house software in original works, different versions of knowledge bases), please note that we cannot guarantee identical replication of the original systems or reproduction of experiment results.**


Part of this work was funded by the EPSRC project LODIE - Linked Open Data for Information Extraction, EP/ J019488/1.


### LICENCE
Apache 2.0

### Quick Start

**NOTE (Apr 2018) about Bing web search**: Bing web search is used by TableMiner+ to detect subject columns. However, the API has now been deprecated and replaced. There is no plan to migrate to the new service in the near future, due to lack of funding. However, if you are willing to contribute, we would be more than happy to merge your pull request. Otherwise, please disable it in `websearch.properties`. 

**NOTE (Sep 2016) about Freebase**: TableMiner+ was developed using Freebase as the knowledge base. Freebase has been shutdown since 2015 and it is no longer possible to access it online. To help you start, we have shared our [datasets](https://github.com/ziqizhang/data#webtable), and also cached Freebase query data for these datasets. The cache data is in excess of 1GB and cannot be hosted here. Please [get in touch](mailto:ziqi.zhang@sheffield.ac.uk) if you need them. We have also implemented code to use DBpedia instead of Freebase. 

To get started, please follow the instructions between and [get in touch](mailto:ziqi.zhang@sheffield.ac.uk) if you encounter any problems:

 - Place a copy of STI on your computer
 - Run maven to install two 3rd party libraries (in 'libs') to your local maven repository. See https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html and the maven plugin with id 'maven-install-plugin' for howto.
 - Download test data, from [here](https://github.com/ziqizhang/data#webtable)
 - Unzip the test data, into e.g., [sti_data]
 - Navigate into [sti_data/dataset], unzip, depending on the test cases: imdb.tar.gz for the IMDB dataset; musicbrainz.tar.gz for the MusicBrainz dataset; Limaye200.tar.gz for the Limaye200 dataset; Limaye_complete.tar.gz for the LimayeAll dataset
 - Configure your local copy of STI
 - - open 'sti.properties', as a minimum, you need to change 'sti.home', and 'sti.cache.main.dir'. Please follow the documentation inside the property file
 - - open 'kbsearch.properties', as a minimum, you need to change 'kb.search.result.stoplistfile', and 'fb.query.api.key' to use your own Freebase API key
 - - open 'websearch.properties', as a minimum, you need to change 'bing.keys' to use your own bing web search key
 - Run a test case. For example, to run TMP, use:  **uk.ac.shef.dcs.sti.experiment.TableMinerPlusBatch "[sti_data/Limaye200]" "[output_dir]" "/[sit_home_dir]/sti.properties"**

**Note:** 'sti.properties' distributed with code is a default configuration for Limaye200 and LimayeAll datasets; for IMDB and MusicBrainz datasets, you can edit a template inside '/resources'. For both IMDB and MusicBrainz, you may want to provide the VM variable '-Djava.util.logging.config.file=' to configure the logging output of the any23-sti module (which can produce too many logs).


