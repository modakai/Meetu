import {defineStore} from 'pinia'

export const useUserStore = defineStore('manager', {
    state: () => ({
        managerInfo: {}
    }),
    actions: {
        setManagerInfo(managerInfo) {
            this.managerInfo = managerInfo
        },
        reset() {
            // 清除 浏览器的 token
            localStorage.removeItem('manager')
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
