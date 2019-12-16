'use strict';

//var webpack = require('webpack');

var config = {
    "mode": "development",
    "context": __dirname + "/web/js",
    "entry": {
        "main": "./openISBTFrontend"
    },
    "output": {
        "path": __dirname + "/build/bundle",
        "filename": "[name].bundle.js",
        "chunkFilename": "[id].bundle.js",
        "publicPath": "/"
    },
    "module": {
        "rules": [

        ]
    },
    "resolve": {
        "modules": [
            "../web/js",
            "resources/main",
            __dirname + "/build/node_modules",
            "node_modules"
        ]
    },
    "plugins": [

    ],
	"devServer": {
		"disableHostCheck": true
	}
};

module.exports = config;
console.log("Custom webpack.config loaded");