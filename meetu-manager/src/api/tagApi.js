import requests from "@/api/requests";

// 分页查询
export const tagPage = (data) => requests({
    url: '/tag/page',
    method: 'GET',
    params: {
        name: data.name,
        pageNum: data.pageNum,
        pageSize: data.pageSize
    }
})

export const tagAll = () => requests({
    url: '/tag',
    method:"get"
})

// 批量删除用户
export const delBatch = (data) => requests({
    url: '/tag/del/batch',
    method: 'POST',
    data: JSON.stringify(data)
})


// 删除
export const delOne = (id) => requests({
    url: '/tag/' + id,
    method: 'DELETE'
})

// 修改或者 添加
export const saveOrUpdate = (url, data) => requests({
    url: url,
    method: data.id ? 'put' : 'post',
    data: data
})