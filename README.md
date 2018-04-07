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

**NOTE (Apr 2018) about Bing web search**: Bing web search is used by TableMiner+ to detect subject columns. However, the API has now been deprecated and replaced. There is no plan to migrate to the new service in the near future, due to lack of funding. However, if you are willing to contribute, we would be more than happy to merge your pull request. Otherwise, please disable it by setting `sti.subjectcolumndetection.ws=false` in `sti.properties` (as already default in the current distribution). 

**NOTE (Sep 2016) about Freebase**: TableMiner+ was developed using Freebase as the knowledge base. Freebase has been shutdown since 2015 and it is no longer possible to access it online. While it is still possible to access Freebase data by mapping its topic IDs to Wikidata entries, currently this has not been implemented. If you are willing contribute, please [get in touch](mailto:ziqi.zhang@sheffield.ac.uk). For now, please use DBpedia instead. See `kbsearch.properties` for details. 

To get started, please follow the instructions between and [get in touch](mailto:ziqi.zhang@sheffield.ac.uk) if you encounter any problems:

 - Place a copy of STI on your computer
 - Run maven to install two 3rd party libraries (in 'libs') to your local maven repository. See https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html for howto. There is also a script `install_missing_libs.sh` to help you on this.
 - Download test data, from [here](https://github.com/ziqizhang/data#webtable)
 - Unzip the test data, into e.g., [sti_data]
 - Navigate into [sti_data/dataset], unzip, depending on the test cases: imdb.tar.gz for the IMDB dataset; musicbrainz.tar.gz for the MusicBrainz dataset; Limaye200.tar.gz for the Limaye200 dataset; Limaye_complete.tar.gz for the LimayeAll dataset
 - Configure your local copy of STI: please read within each `.properties` file for detailed descriptions of each parameter
 - - open `config/sti.properties`, as a minimum, you need to change `sti.home`, and `sti.cache.main.dir`. 
 - - open `kbsearch.properties`, as a minimum, you need to change `kb.search.result.stoplistfile`, and `fb.query.api.key` to use your own Freebase API key
 - - open `websearch.properties`, as a minimum, you need to change `bing.keys` to use your own bing web search key
 - STI uses log4j for logging. **Make sure you have a copy of `config/log4j.properties` within your compiled java class folder** for the progress to be displayed properly. 
 - Run a test case. For example, to run TMP, use:  **uk.ac.shef.dcs.sti.experiment.TableMinerPlusBatch "[sti_data/Limaye200]" "[output_dir]" "/[sit_home_dir]/sti.properties"**

**Note:** `sti.properties` distributed with code is a default configuration for Limaye200 and LimayeAll datasets; for IMDB and MusicBrainz datasets, you can edit a template inside `/resources`. For both IMDB and MusicBrainz, you may want to provide the VM variable '-Djava.util.logging.config.file=' to configure the logging output of the any23-sti module (which can produce too many logs).


### Why is it so slow?
Semantic Table Interpretation requires fetching data from a knowledge base. This is currently configured to use a **remote** knowledge base by calling its APIs or web services, such as the DBPedia SPARQL endpoint. This is the part of the process that takes 99.99% of processing time in a typical STI application. If possible, please consider to host a local copy of the knowledge base before you start. For example, you can deploy a local DBpedia server, which then can result in orders of magnitude of performance improvement. 
