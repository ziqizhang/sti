var express = require('express');
var app = express();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var fs = require('fs');
var path = require('path');

var config = JSON.parse(fs.readFileSync('config.json', 'utf8'));

var userTmpFolderMap={};
var userCounter=1;


//$.getJSON( "/config.json", function( data ) {
var STI_LOG4J=config.log4j
var STI_LIB=config.lib;
var STI_CACHE=config.cache;
var STI_TABLE_FILE_PREVIEW_CLASS=config.tablePrevClass;
var STI_TMP_FOLDER=config.tmp;
var STI_ERROR_PAGE=config.error;
var STI_PROPERTY=config.stipropfile;
var STI_KB_PROPERTY=config.stikbpropfile;
var STI_WEB_PROPERTY=config.stiwebpropfile;
var STI_OUTPUT_FOLDER=config.output;
var STI_MAIN_CLASS=config.stiMainClass;
var STI_TABLE_PARSER_OPTIONS=config.tableParsers;
var SERVER_ADDRESS=config.server;
//});

function lock(userid){
	fd = fs.openSync(STI_CACHE+'/'+userid+'.lock', 'w');
}

function unlock(userid){
	fd = fs.unlinkSync(STI_CACHE+'/'+userid+'.lock');
}

function existLock(){
	var files = fs.readdirSync(STI_CACHE);		
	var foundFile='null';
	for(var i in files) {
	   if(path.extname(files[i]) === '.lock') {
   			return true;
   	    }       	
	}
	return false;
}

function getLocalUserId(socketId){
	return userTmpFolderMap[socketId];
}
function addLocalUserId(socketId){
	userTmpFolderMap[socketId]='user'+userCounter;
	userCounter++;	
}

function replaceAll(word, text, newWord){
  var pos = text.indexOf(word);
  if(pos === -1)
   return text;
  var str1 = text.substr(0, pos);
  var str2 = replaceAll(word, text.substr(pos+word.length), newWord);
  return str1+newWord+str2;
 }

function writePreviewErrorPage(subfolder, errMsg){
    data='<!DOCTYPE html>\n<html>\n<body>\n<h1>Preview encountered problem, please contact your admin</h1>\n'
          +'<p>'+errMsg+'</p></body></html>';
	console.log("java_preview encoutered error, preparing error page...");
	fs.writeFileSync(subfolder+'/error.html', data); 
}

function javaPreview(userid, targetUrl, parserclass, fn){   
   var spawn = require('child_process').spawn;
   var subfolder=STI_TMP_FOLDER+'/'+userid;
   var java = spawn('java', ['-Dlog4j.configuration=file:'+STI_LOG4J, '-cp', STI_LIB, STI_TABLE_FILE_PREVIEW_CLASS, targetUrl, subfolder, parserclass]);

   var error='';   

   java.stdout.on('data', function(data){
	  console.log('stdout: %s', data);
	  error=error+"<br/>"+replaceAll('\n',data.toString(),'<br/>');
   });

   java.stderr.on('data', function(data){
      console.log('stderr: %s',data);
      error=error+"<br/>"+replaceAll('\n',data.toString(),'<br/>');
   });

   var result;
   java.on('close', function(code){
   	  var returnWebpage=subfolder+'/'+STI_ERROR_PAGE;
      console.log('java process exited with code %s', code);
      if(code==='1'){
      	writePreviewErrorPage(subfolder, error);
      }else{
      	console.log('now find the file');
      	var files = fs.readdirSync(subfolder);		
		var foundFile='null';
		for(var i in files) {
		   if(path.extname(files[i]) === '.html') {
       			foundFile=files[i];
       			//console.log(foundFile.name);
       			break;
       	    }       	
		}

		if(foundFile!=='null'){
			returnWebpage=subfolder+'/'+foundFile.toString();
		}
      }
      //var xpaths = JSON.parse(fs.readFileSync(subfolder+'/xpaths.json', 'utf8'));
      var xpathFile=subfolder+'/xpaths.json';
      if (!fs.existsSync(xpathFile))
      		xpathFile='';
      result = [error,returnWebpage, xpathFile];

      fn(result);
   });
   
}


function javaSTI(userid, email, targetUrl, parserclass, selectedTableIndex, socket, fn){   
   var spawn = require('child_process').spawn;
   var java = spawn('java', ['-cp', STI_LIB, STI_MAIN_CLASS, userid, email, targetUrl, STI_OUTPUT_FOLDER, parserclass, selectedTableIndex, STI_PROPERTY, 'config.json', SERVER_ADDRESS]);

   var error='';   

   java.stdout.on('data', function(data){
   	  var txt=replaceAll('\n',data.toString(),'<br/>')+'<br/>';
   	  socket.emit('sti_info', {msg:txt});
   });

   java.stderr.on('data', function(data){
      var txt=replaceAll('\n',data.toString(),'<br/>')+'<br/>';
   	  socket.emit('sti_err', {msg:txt});
   });

   var result;
   java.on('close', function(code){
      console.log('java process exited with code %s', code);
      socket.emit('sti_complete', {msg:SERVER_ADDRESS+userid+"/index.htm"});
      fn(userid);
   });
   
}


app.use(express.static('public'));
app.use('/tmp',express.static('tmp'));
app.use('/output',express.static('output'));

app.get('/', function (req, res) {
   res.sendFile("public/" + "index.html" );
});


io.of('/').on('connection', function(socket){
  socket.emit('filltableparseroptions',{options:STI_TABLE_PARSER_OPTIONS});
 
  if(existLock()){
  	socket.emit('warn_existingProcess', {msg:"TableMiner+ is already processing a task. If you start another task now it may corrupt its cache. This is because TableMiner+ caches remote query results locally and currently does not support concurrent access to the cache."});
  }

  addLocalUserId(socket.id);
  var localUserId=getLocalUserId(socket.id);

  console.log('%s (localId=%s) is connected',socket.id, localUserId);  

  function onPrevComplete(result){
	console.log(result);
	socket.emit('java_preview_complete', {error: result[0], page: result[1], xpaths: result[2]});
  }

  socket.on('java_preview', function(data){
  		console.log('%s preview request, %s, %s', localUserId, data.url, data.tableparserClass);
  		var result=javaPreview(localUserId, data.url, data.tableparserClass, onPrevComplete//see http://stackoverflow.com/questions/14220321/how-do-i-return-the-response-from-an-asynchronous-call
  	);
  	//console.log('$s preview response: %s', localUserId, result[0]);
  	//if result has error message, emit error
  	//else ...
  });

  function onSTIComplete(userid){
	console.log("%s task completed", userid);
	unlock(userid);	
  }

  socket.on('java_sti', function(data){
  	console.log('%s sti request, %s, %s, %s, tableIndexes[%s]', localUserId, data.url, data.email, 
  		data.tableparserClass, data.tableIndexes);
  	lock(localUserId);

  	//spawn the sti java process
  	javaSTI(localUserId, data.email, data.url, data.tableparserClass, data.tableIndexes, socket, onSTIComplete);
  	
  });
  
  socket.on('setting_tmp', function(data){
        //read config file
        var fileContent = fs.readFileSync(STI_PROPERTY, "utf8");        
        //send back result
        this.emit('setting_tmp_content',{data:fileContent});
  });
  socket.on('setting_web', function(data){
        //read config file
        var fileContent = fs.readFileSync(STI_WEB_PROPERTY, "utf8");        
        //send back result
        this.emit('setting_web_content',{data:fileContent});
  });
  socket.on('setting_kb', function(data){
        //read config file
        var fileContent = fs.readFileSync(STI_KB_PROPERTY, "utf8");        
        //send back result
        this.emit('setting_kb_content',{data:fileContent});
  });
  
  socket.on('configSave', function(data){
        //read config file
        var fileContent = data.data;
        var type=data.type;
        //send back result
        if(type==='tmp'){
            fs.writeFileSync(STI_PROPERTY,fileContent);
        }
        else if(type==='web'){
            fs.writeFileSync(STI_WEB_PROPERTY,fileContent);
        }else if(type==='kb'){
            fs.writeFileSync(STI_KB_PROPERTY,fileContent);
        }
  });
});


server.listen(3000, function () {
  console.log("Server started...");
  var host = server.address().address;
  var port = server.address().port;

  console.log("Listening at http://%s:%s", host, port);


});

