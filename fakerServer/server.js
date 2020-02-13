var jsf = require('json-schema-faker');
var http = require('http');
jsf.extend('faker', () => require('faker'));

var args = process.argv
var port = args[2]
if (port == null) {
    port = 9080
    console.log("Server running on default port:" + port)
} else {
    console.log("Server running on port:" + port)
}

http.createServer(function (req, res) {
  const { headers, method, url } = req;
  let body = [];
  req.on('error', (err) => {
    console.error(err);
  }).on('data', (chunk) => {
    body.push(chunk);
  }).on('end', () => {
    body = Buffer.concat(body).toString();
    var jbody = JSON.parse(body)
    var fake = jsf.generate(jbody)
    var fakeString = JSON.stringify(fake);
    res.writeHead(200, {"Content-Type": "application/json"});
    res.write(fakeString)
    res.end();
  });

}).listen(port);