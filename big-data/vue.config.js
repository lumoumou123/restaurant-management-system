const CompressionPlugin = require('compression-webpack-plugin');
const productionGzipExtensions = /\.(js|css|json|txt|html|ico|svg)(\?.*)?$/i;
const path = require('path')
function resolve(dir) {
  return path.join(__dirname, dir)
}
module.exports = {
  publicPath: './',
  productionSourceMap: false,
  lintOnSave: false, // 是否开启eslint保存检测，有效值：ture | false | 'error'
  chainWebpack: config => {
    //设置标题  默认不设置的话是项目名字
    config.plugin('html').tap(args => {
      args[0].title = "餐厅评分系统"
      return args
    })
    config.plugin("preload").tap(() => [
      {
        rel: "preload",
        fileBlacklist: [/\.map$/, /hot-update\.js$/, /runtime\..*\.js$/],
        include: "initial"
      }
    ])
    config.set("externals", {
      'vue': "Vue",
      "vue-router": "VueRouter",
      'axios': "axios",
      'echarts': "echarts",
      'vuex':'Vuex',
      'element-ui': 'ELEMENT',
    })
    config.plugins.delete("prefetch")
    // config.plugin('webpack-bundle-analyzer').use(require('webpack-bundle-analyzer').BundleAnalyzerPlugin)
    config.resolve.alias.set('@', resolve('src'))
    // config.module
    //   .rule("images")
    //   .use("image-webpack-loader")
    //   .loader("image-webpack-loader")
    //   .options({
    //     bypassOnDebug: true
    //   })
    //   .end()
    // config.when(process.env.NODE_ENV !== 'development',
    //   config => {
    //     // 提供带 Content-Encoding 编码的压缩版的资源
    //     config.plugin('compressionPlugin')
    //       .use(new CompressionPlugin({
    //         filename: '[path].gz[query]', // 目标文件名
    //         algorithm: 'gzip',  // 压缩算法
    //         test: productionGzipExtensions, // 满足正则表达式的文件会被压缩
    //         threshold: 10240, // 只处理比这个值大的资源。按字节计算 10240=10KB
    //         minRatio: 0.8, // 只有压缩率比这个值小的资源才会被处理
    //         deleteOriginalAssets: true // 是否删除原资源
    //       }));
    //   }
    // )
  },
  css: {
    loaderOptions: {
      postcss: {
          plugins: [
            require('postcss-px2rem')({
              remUnit: 192
            })
          ]
        }
    }
  },
  devServer: {
    open: true,//项目启动时是否自动打开浏览器，我这里设置为false,不打开，true表示打开
    proxy: {
        '/map': {//代理api
            target: "https://api.map.baidu.com",// 代理接口
            changeOrigin: true,//是否跨域
            ws: true, // proxy websockets
            pathRewrite: {//重写路径
              "^/map": ''//代理路径
            }
        }
    }
}
}
