#!/usr/bin/env node
var DebugServer = require('node-inspector/lib/debug-server').DebugServer;
var config = require('node-inspector/lib/config');
var spawn = require('child_process').spawn;
var open = require('open');

var debugServer = new DebugServer();
debugServer.on('close', function () {
  console.log('session closed');
  process.exit();
});
debugServer.start(config);

spawn('node', ['--debug-brk'].concat(process.argv.slice(2)));

open('http://localhost:' + config.webPort + '/debug?port=' + config.debugPort);
