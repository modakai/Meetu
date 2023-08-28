import requests from "@/api/requests";

// 管理员登入
export const loginAdmin = (data) => requests({
    url: '/normal/login',
    method: 'POST',
    data: JSON.stringify(data)
})
// 退出登入
export const logoutAdmin = (data) => requests({
    url: '/logout/' + data.uid + "?" + "thenLoginType=" + data.loginType,
    method: 'POST',
})

export const userPage = (data) => requests({
    url: '/user/page',
    method: 'GET',
    params: {
        name: data.name,
        pageNum: data.pageNum,
        pageSize: data.pageSize
    }
})

// 批量删除用户
export const delBatch = (data) => requests({
    url: '/user/del/batch',
    method: 'POST',
    data: JSON.stringify(data)
})

// 批量删除用户
export const delUser = (data) => requests({
    url: '/user/' + data,
    method: 'DELETE',
})

// 修改或者 添加
export const saveOrUpdate = (url, data) => requests({
    url: url,
    method: data.id ? 'put' : 'post',
    data: data
})

export const modifyUser = (data) => requests({
    url: '/user/modify',
    method: 'put',
    data: JSON.stringify(data)
})