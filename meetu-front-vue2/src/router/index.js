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
            path: '/404',
            component: () => import('@/views/404.vue')
        }
    ]
})

// 全局前置路由守卫
router.beforeEach((to, from, next) => {
    console.log("路由日志: {}", to.matched)
    const state = useUserStore()
    // TODO 后期使用 token 代替
    const userInfo = state.userInfo
    if (!userInfo) {
        if (to.path !== '/login') {
            next('/login')
        } else {
            next()
        }
    } else {
        next()
    }
    // if (!to.matched.length) {
    //     next('/login')
    // } else {
    //     next()
    // }
})

export default router