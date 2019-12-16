'use strict';

//var webpack = require('webpack');

var config = {
    "mode": "development",
    "context": "/home/ec2-user/openISBT/openISBTFrontend/web/js",
    "entry": {
        "main": "./openISBTFrontend"
    },
    "output": {
        "path": "/home/ec2-user/openISBT/openISBTFrontend/build/bundle",
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
            "/home/ec2-user/openISBT/openISBTFrontend/build/node_modules",
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