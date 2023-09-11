import requests from "@/api/requests";

// 分页查询
export const commentsPage = (data) => requests({
    url: '/comments/page',
    method: 'GET',
    params: {
        name: data.name,
        pageNum: data.pageNum,
        pageSize: data.pageSize
    }
})

export const commentsTree = (dynamicId) => requests({
    url: '/comments/tree',
    method: "get",
    params: {
        dynamicId: dynamicId
    }
})

// 批量删除用户
export const delBatch = (data) => requests({
    url: '/comments/del/batch',
    method: 'POST',
    data: JSON.stringify(data)
})


// 删除
export const delOne = (id) => requests({
    url: '/comments/' + id,
    method: 'DELETE'
})

// 修改或者 添加
export const saveOrUpdate = (url, data) => requests({
    url: url,
    method: data.id ? 'put' : 'post',
    data: data
})