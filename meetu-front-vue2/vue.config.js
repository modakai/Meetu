const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    proxy: {
      '/api': {
        target: `http://localhost:8848`
      }
    }
  },
  configureWebpack: { //这个是配置
    module: {
      rules: [
        {
          test: /\.mjs$/,
          include: /node_modules/,
          type: "javascript/auto"
        }
      ]
    }
  }
})
