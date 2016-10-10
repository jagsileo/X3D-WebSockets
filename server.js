var http = require("http");
var url = require('url');
var fs = require('fs');
var io = require('socket.io');

var server = http.createServer(function(request, response){
		//response.writeHead(404);
		//response.write("opps this doesn't exist - 404");
		//response.end();

		});

server.listen(8001);

var listener = io.listen(server);
listener.sockets.on('connection', function(socket){
		socket.emit('message', 'Hi Client');
		socket.on('hover', function(message){
			socket.emit('message', 'Server received Hover');
			
			socket.emit('point', {'point' : [Math.random()*5, Math.random()*5, Math.random()*5]
					});
		
			});
		});




