import request from "@/utils/request.js";

export const getBankList = (data) => {
    return request({
        url: '/api/bank/getBankList',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
export const toggleCollectBank = (data) => {
    return request({
        url: '/api/bank/addCollectBank',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
export const getCollectBankIds = (data) => {
    return request({
        url: '/api/bank/getCollectBankIds',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
export const getWrongBankCount = (data) => {
    return request({
        url: '/api/bank/getWrongBankCount',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
export const getWrongQuestionList = (data) => {
    return request({
        url: '/api/bank/getWrongQuestionList',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}

export const removeWrongQuestion = (data) => {
    return request({
        url: '/api/bank/removeWrongQuestion',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}

export const getAllQuestionList = (data) => {
    return request({
        url: '/api/bank/getAllQuestionList',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
export const getQuestionById = (data) => {
    return request({
        url: '/api/bank/getQuestionById',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
export const submitQuestion = (data) => {
    return request({
        url: '/api/bank/submitQuestion',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
export const runTestCode = (data) => {
    return request({
        url: '/api/bank/runTestCode',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
export const addViewCount = (data) => {
    return request({
        url: '/api/bank/addViewCount',
        method: 'POST',
        data: data // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}