import Vue from "vue";
import VueRouter from "vue-router";
import { useUserStore } from "@/store/user";

Vue.use(VueRouter)

const router = new VueRouter({
    mode: 'history',
    routes: [
        {
            path: '/',
            component: () => import('@/views/HomeView.vue')
        },
        {
            path: '/login',
            component: () => import('@/views/Login.vue')
        },
        {
            path: '/register',
            component: () => import('@/views/Register.vue')
        },
        {
            path: '/*',
            component: () => import('@/views/404.vue')
        }
    ]
})
// 无需登入的路径
const noPermissionPaths = ['/login', '/register']
// 全局前置路由守卫
router.beforeEach((to, from, next) => {
    console.log("路由日志: {}", to.matched)
    const state = useUserStore()
    // TODO 后期使用 token 代替
    const userInfo = state.userInfo
    if (!userInfo && !noPermissionPaths.includes(to.path)) {
        next('/login')
    } else {
        next()
    }
})

export default router