var webpack = require('webpack');
var WebpackDevServer = require('webpack-dev-server');
var config = require('./webpack.config');

new WebpackDevServer(webpack(config), {
  proxy:{
    '/api/**': { target: 'http://localhost:9080', secure: false, changeOrigin: true },
    '/oauth2/**': { target: 'http://localhost:9080', secure: false, changeOrigin: true }

  },

  publicPath: config.output.publicPath,
  hot: true,
  historyApiFallback: true
}).listen(3000, 'localhost', function (err, result) {
  if (err) {
    return console.log(err);
  }

  console.log('Listening at http://localhost:3000/');
});
