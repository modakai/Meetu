import {defineStore} from "pinia";

export const useUserStore = defineStore('user', {
    // 表示 user 对象
    state: () => ({
        userInfo: {}
    }),
    // 用于操作 state中的数据
    actions: {
        setUserInfo(data) {
            this.userInfo = data
        }
    },
    // 开启数据持久化
    // persist: {
    //     // 使用会话进行存储
    //     storage: sessionStorage
    // }
    persist: true
})