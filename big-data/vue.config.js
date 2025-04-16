const CompressionPlugin = require('compression-webpack-plugin');
const productionGzipExtensions = /\.(js|css|json|txt|html|ico|svg)(\?.*)?$/i;
const path = require('path')
const fs = require('fs')

function resolve(dir) {
  return path.join(__dirname, dir)
}

// 安全地读取环境变量，确保API密钥可用
const MAPS_API_KEY = process.env.VUE_APP_GOOGLE_MAPS_API_KEY || '';
// 如果环境变量为空，尝试从.env.local文件直接读取
if (!MAPS_API_KEY) {
  try {
    const envPath = path.resolve(__dirname, '.env.local');
    if (fs.existsSync(envPath)) {
      const envContent = fs.readFileSync(envPath, 'utf8');
      const match = envContent.match(/VUE_APP_GOOGLE_MAPS_API_KEY=(.+)/);
      if (match && match[1]) {
        process.env.VUE_APP_GOOGLE_MAPS_API_KEY = match[1].trim();
        console.log('从.env.local文件成功读取API密钥');
      }
    }
  } catch (error) {
    console.error('读取.env.local文件失败:', error);
  }
}

module.exports = {
  transpileDependencies: [],
  publicPath: './',
  productionSourceMap: false,
  lintOnSave: false, // 是否开启eslint保存检测，有效值：ture | false | 'error'
  chainWebpack: config => {
    //设置标题  默认不设置的话是项目名字
    config.plugin('html').tap(args => {
      args[0].title = "餐厅评分系统"
      // 使用从环境变量中读取的API密钥
      args[0].VUE_APP_GOOGLE_MAPS_API_KEY = process.env.VUE_APP_GOOGLE_MAPS_API_KEY;
      console.log('传递给模板的API密钥(为保护隐私只显示前10个字符):', 
                  args[0].VUE_APP_GOOGLE_MAPS_API_KEY ? 
                  args[0].VUE_APP_GOOGLE_MAPS_API_KEY.substring(0, 10) + '...' : 
                  '未设置');
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
    config.resolve.alias.set('@', resolve('src'))
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
