import requests from "@/api/requests";

// 分页查询
export const messagesPage = (data) => requests({
    url: '/messages/page',
    method: 'GET',
    params: {
        name: data.name,
        pageNum: data.pageNum,
        pageSize: data.pageSize
    }
})

// 批量删除用户
export const delBatch = (data) => requests({
    url: '/messages/del/batch',
    method: 'POST',
    data: JSON.stringify(data)
})


// 删除
export const delOne = (id) => requests({
    url: '/messages/' + id,
    method: 'DELETE'
})

// 修改或者 添加
export const saveOrUpdate = (url, data) => requests({
    url: url,
    method: data.id ? 'put' : 'post',
    data: data
})