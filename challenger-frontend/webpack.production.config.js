var path = require('path');
var webpack = require('webpack');

module.exports = {
  devtool: 'cheap-module-source-map',
  entry: [
    './src/index'
  ],
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'bundle.js',
    publicPath: '/'
  },
  resolve: {
    // Add `.ts` and `.tsx` as a resolvable extension.
    extensions: ['', '.webpack.js', '.web.js', '.ts', '.tsx', '.js']
  },

  plugins: [
      // removes a lot of debugging code in React
      new webpack.DefinePlugin({
          'process.env': {
              'NODE_ENV': JSON.stringify('production')
          }
      }),
// keeps hashes consistent between compilations
      new webpack.optimize.OccurenceOrderPlugin(),
// minifies your code
      new webpack.optimize.UglifyJsPlugin({
          compressor: {
              warnings: false
          }
      })
    /*
     new webpack.ProvidePlugin({
     "React": "react",
     }),
     new webpack.HotModuleReplacementPlugin()*/
  ],
  module: {
    loaders: [{
        test: /\.js$/,
        loaders: ['babel'],
        include: path.join(__dirname, 'src'),
        exclude: /(node_modules|bower_components)/,
      },


      { test: /\.tsx?$/,
        loaders: ['babel', 'ts-loader' ],


        include: path.join(__dirname, 'src'),
        exclude: /(node_modules|bower_components)/,
        preLoaders: [
          // All output '.js' files will have any sourcemaps re-processed by 'source-map-loader'.
          { test: /\.js$/, loader: "source-map-loader" }
        ]
      }
    ]
  }
};

