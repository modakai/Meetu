import axios from "axios";
import {ElMessage} from 'element-plus'
import {useUserStore} from "@/stores/user";
import router from "@/router";

const requests = axios.create({
    // 基础路径, 每次请求都会在前面加上
    baseURL: '/api',
    // 连接超时世界
    timeout: 5000
})

// 请求拦截器
requests.interceptors.request.use((config) => {

    config.headers['Content-Type'] = 'application/json;charset=utf-8'
    config.headers['Authorization'] = useUserStore().getAuthorization
    return config;
})

// 响应拦截器
requests.interceptors.response.use((res) => {
    let result = res.data;
    if (res.config.responseType === 'blob') {
        return res
    }

    if (typeof result === 'string') {
        result = result ? JSON.parse(result) : result
    }

    if (result.code === '401') {
        ElMessage.error(result.msg)
        router.push('/login')
    }
    return result

}, (err) => {
    ElMessage.error('未知错误')
    console.log(err)
    // return Promise.reject(new Error('fail'))
    return err.msg
})

export default requests;