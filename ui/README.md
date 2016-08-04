# STI GUI (Still under construction)
This application provides a UI interface to interact with STI and visualize results. It is implemented based on Node.js.

### Installation
- Download STI
- Install Node.js on your computer (see https://nodejs.org/en/download/)
- You need to ensure you have the following Node modules: `express` and `socket.io`. By default, these are included in the download (/node_modules)
- Build STI. `cd` into the parent folder of this folder, run `mvn clean install`. If build is successful, you should see a `sti-[version]-jar-with-dependencies.jar` file in `target` folder
- `cd ui` into the root folder. Edit `config.json` following descriptions below.
- Start the serer by `node main.js`
- Open a browser and point your address to `http://localhost:3000/`

### Configuration `config.json`
This file configures the UI application. It links to other files used to configure TableMiner+. The following fields are defined. If you do not need email notification when a task completes, you do not need to change anything. Otherwise, as minimum, you must change **emaillogin, emailpass, emailhost, emailport, emailfrom** to configure your own email account. 

- **lib**: should point to the jars required to run TableMiner+. By default, this is '../target/*', where your maven build will place a `sti-[version]-jar-with-dependencies.jar`
- **cache**: should point to the folder containing the Solr index caches. By default this is '../resources/cache'
- **tablePrevClass**: a class used to download the webpage specified by user and preview its content before annotating the contained tables. This should be `uk.ac.shef.dcs.sti.ui.InputfilePreview`
- **stiMainClass**: the main class to run the TableMiner+ task. This should be `uk.ac.shef.dcs.sti.ui.TableMinerPlusSingle`
- **tmp**: a system required folder to keep intermediary files
- **error**: an error webpage to display when things go wrong
- **output**: a system required folder to contain output files
- **log4j**: the log4j configuration file. You must set this correctly to see the progress logs. By default this is '../config/log4j.properties'
- **stiprofile**: the configuration file for running TableMiner+. By default this is '../config/sti.properties'. You will be able to chanage the settings through the UI. Detailed instructions are included in the file.
- **stiwebprofile**: the configuration file for Web search used by TableMiner+. By default this is '../config/websearch.properties'. You will be able to chanage the settings through the UI. Detailed instructions are included in the file.
- **stikbprofile**: the configuration file for knowledge base search used by TableMiner+. By default this is '../config/kbsearch.properties'. You will be able to chanage the settings through the UI. Detailed instructions are included in the file.
- **server**: the server address.
- **emaillogin**: TableMiner+ emails you when a task is complete. To do this it uses an email account. This is the login email address for that account. If not provided, you will not receive email notification.
- **emailpass**: The password to log into the above email account.
- **emailhost**: The host of the email account
- **emailport**: Port number of the email host
- **emailfrom**: Sender of the email
- **tableParsers**: input files (e.g., Webpages) must be preprocessed to extract the `HTML <table>` elements from the Webpage and these `<table>` must be represented as an internal Java object. A table parser is responsible for this job. Depending on different Webpages, you may encounter different `<table>` templates that require different processing. Hence Website-specific table parsers may have to be implemented from time to time. Currently there are four table parsers implemented, that can be found in the `uk.ac.shef.dcs.sti.parser.table` package. To support new Webpages, you need to create a new implementation of `TableParser`, which must implement `Browsable` if you want it to support Webpage preview in the UI. Then add that class into this JSON property. 