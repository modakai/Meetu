import requests from "@/api/requests";

// 分页查询
export const noticePage = (data) => requests({
    url: '/notice/page',
    method: 'GET',
    params: {
        name: data.name,
        pageNum: data.pageNum,
        pageSize: data.pageSize
    }
})

export const noticeAll = () => requests({
    url: '/notice',
    method: 'GET'
})
export const noticeReleaseList = () => requests({
    url: '/notice/release/list',
    method: 'GET'
})

// 批量删除用户
export const delBatch = (data) => requests({
    url: '/notice/del/batch',
    method: 'POST',
    data: JSON.stringify(data)
})


// 删除
export const delOne = (id) => requests({
    url: '/notice/' + id,
    method: 'DELETE'
})

// 修改或者 添加
export const saveOrUpdate = (url, data) => requests({
    url: url,
    method: data.id ? 'put' : 'post',
    data: data
})
