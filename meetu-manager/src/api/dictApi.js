import requests from "@/api/requests";

// 分页查询
export const dictPage = (data) => requests({
    url: '/dict/page',
    method: 'GET',
    params: {
        name: data.name,
        pageNum: data.pageNum,
        pageSize: data.pageSize
    }
})

// 根据 type 查询
export const dictTypeList = (type) => requests({
    url: '/dict/type/' + type,
    method: 'GET'
})

// 批量删除用户
export const delBatch = (data) => requests({
    url: '/dict/del/batch',
    method: 'POST',
    data: JSON.stringify(data)
})


// 删除
export const delOne = (id) => requests({
    url: '/dict/' + id,
    method: 'DELETE'
})

// 修改或者 添加
export const saveOrUpdate = (url, data) => requests({
    url: url,
    method: data.id ? 'put' : 'post',
    data: data
})