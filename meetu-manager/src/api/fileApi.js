import requests from "@/api/requests";

// 分页查询
export const filePage = (data) => requests({
    url: '/file/page',
    method: 'GET',
    params: {
        name: data.name,
        pageNum: data.pageNum,
        pageSize: data.pageSize
    }
})

// 批量删除用户
export const delBatch = (data) => requests({
    url: '/file/del/batch',
    method: 'POST',
    data: JSON.stringify(data)
})


// 删除
export const delOne = (data) => requests({
    url: '/file',
    method: 'DELETE',
    data: JSON.stringify(data)
})

// 修改或者 添加
export const saveOrUpdate = (url, data) => requests({
    url: url,
    method: data.id ? 'put' : 'post',
    data: data
})

export const downFileBefore = (location) => requests({
    url: '/file/download/before',
    method: "get",
    params: {
        fileName: location
    }
})