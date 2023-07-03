import Vue from 'vue'
import App from './App.vue'

import router from '@/router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

import { createPinia, PiniaVuePlugin } from 'pinia'
Vue.use(ElementUI)

import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
Vue.use(PiniaVuePlugin)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate);
Vue.use(pinia)
// 引入全局样式
import '@/assets/css/global.css'

Vue.config.productionTip = false

new Vue({
  render: h => h(App),
  router,
  pinia,
}).$mount('#app')
