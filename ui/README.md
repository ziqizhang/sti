# STI GUI
This application provides a UI interface to interact with STI and visualize results. It is implemented based on Node.js.

### Installation
- Download this folder
- Install Node.js on your computer (see https://nodejs.org/en/download/)
- You need to ensure you have the following Node modules: `express` and `socket.io`. By default, these are included in the download (/node_modules)
- Build STI. `cd` into the parent folder of this folder, run `mvn clean install`. If build is successful, you should see a `sti-[version]-jar-with-dependencies.jar` file in `target` folder
- `cd ui` into the root folder. Then start the serer by `node main.js`
- open a browser and point your address to `http://localhost:3000/`

### Configuration
You do not need to change any configuration for the application to run. However, the following property files can be edited depending on your requirements:

- `config.json`: this is the property file to configure the UI application. 



