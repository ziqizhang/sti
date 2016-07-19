var express = require('express');
var bodyParser = require('body-parser');
var app = express();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var fs = require('fs');
var path = require('path');

const STI_LIB='sti/lib/*';
const STI_TABLE_FILE_PREVIEW_CLASS='uk.ac.shef.dcs.sti.ui.InputFilePreview';
const STI_TMP_FOLDER='tmp'

function writePreviewErrorPage(errMsg){
    data='<!DOCTYPE html>\n<html>\n<body>\n<h1>Preview encountered problem, please contact your admin</h1>\n'
          +'<p>'+errMsg+'</p></body></html>';
	console.log("java_preview encoutered error, preparing error page...");
	fs.writeFile(STI_TMP_FOLDER+'/error.html', data,  function(err) {
   		if (err) {
   			//emit the error to client
       		return console.error(err);
   		}
});
}


app.use(express.static('resources'));
app.use(express.static('tmp'));
app.use(express.static(__dirname));
app.use(bodyParser.urlencoded({extended : false}));

app.get('/', function (req, res) {
   res.sendFile( __dirname + "/" + "index.html" );
})



app.post('/java_preview', function (req, res) {

   var targetUrl=req.body.url;
   var parserclass=req.body.tableparserClass;
   var userId=req.body.user;
   console.log("Request for java_preivew webpage: %s, using table parser class: %s, userid:%s", targetUrl, parserclass, socket.id);

   var spawn = require('child_process').spawn;
   var java = spawn('java', ['-cp', STI_LIB, STI_TABLE_FILE_PREVIEW_CLASS, targetUrl, STI_TMP_FOLDER, parserclass]);

   var error;

   java.stdout.on('data', function(data){
	  console.log('stdout: %s', data);
   });

   java.stderr.on('data', function(data){
      console.log('stderr: %s',data);
      error=data;
   });

   java.on('close', function(code){
      console.log('java process exited with code %s', code);
      if(code=='1'){
      	writePreviewErrorPage(error);
      }else{
      	console.log('now find the file');
      	var files = fs.readdirSync(STI_TMP_FOLDER);		
		var foundFile='null';
		for(var i in files) {
		   if(path.extname(files[i]) == '.html') {
       			foundFile=i;
       			console.log(foundFile.name);
       	}       	
		}

		if(foundFile!='null'){
			//emit file for client to display
		}else{
			//emit error
		}
      }
   });   
})

io.on('connection', function(socket){
  console.log('a user with socket id %s connected',socket.id);
});

server.listen(3000, function () {
  console.log("Server started...")
  var host = server.address().address
  var port = server.address().port

  console.log("Listening at http://%s:%s", host, port)


})

