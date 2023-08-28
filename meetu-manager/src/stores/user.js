import {defineStore} from 'pinia'
import router, {setRoutes} from "@/router";

export const useUserStore = defineStore('manager', {
    state: () => ({
        managerInfo: {}
    }),
    actions: {
        setManagerInfo(managerInfo) {
            this.managerInfo = managerInfo

            // 设置路由
            setRoutes(managerInfo.menus)
        },
        reset() {
            // 清除 浏览器的 token
            localStorage.removeItem('manager')
            // 跳转登入页
            router.push("/login")
        },
        getMenus() {
            return this.managerInfo.menus || []
        },
        getPageMenus() {
            return this.managerInfo.pageMenus.length ? this.managerInfo.pageMenus.map(v => v.auth) : []
        },
        getManagerRole() {
            return this.managerInfo.role || ''
        },
        setUser(data) {
            this.managerInfo = {...this.managerInfo, data}

            setRoutes(managerInfo.menus)
        }
    },
    getters: {
        getAuthorization() {
            return this.managerInfo.authorization ? 'Bearer ' + this.managerInfo.authorization : ''
        },
        getManagerInfo() {
            return this.managerInfo
        }
    },
    persist: true
})
