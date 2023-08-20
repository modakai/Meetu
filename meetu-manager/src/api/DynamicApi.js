import requests from "@/api/requests";

// 分查询
export const dynamicPage = (data) => requests({
    url: '/dynamic/page',
    method: 'GET',
    params: {
        name: data.name,
        pageNum: data.pageNum,
        pageSize: data.pageSize
    }
})
// 批量删除
export const delBatch = (data) => requests({
    url: '/dynamic/del/batch',
    method: 'POST',
    data: JSON.stringify(data)
})

// 删除
export const delOne = (id) => requests({
    url: '/dynamic/' + id,
    method: 'DELETE'
})

// 修改或者 添加
export const saveOrUpdate = (url, data) => requests({
    url: url,
    method: data.id ? 'put' : 'post',
    data: data
})
