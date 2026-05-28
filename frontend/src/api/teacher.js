import request from '@/utils/request'

export function loadQuestionData(data){
    return request({
        url: `/api/teacher/loadQuestionList`,
        method: 'post',
        data
    })
}
export function loadBankData(data){
    return request({
        url: `/api/teacher/loadBankList`,
        method: 'post',
        data
    })
}
export function saveBank(data){
    return request({
        url: `/api/teacher/saveBank`,
        method: 'post',
        data
    })
}
export function deleteBank(data){
    return request({
        url: `/api/teacher/deleteBank`,
        method: 'post',
        data
    })
}
export function addQuestion(data){
    return request({
        url: `/api/teacher/addQuestion`,
        method: 'post',
        data
    })
}
export function updateQuestion(data){
    return request({
        url: `/api/teacher/updateQuestion`,
        method: 'post',
        data
    })
}

export function loadExamData(data){
    return request({
        url: `/api/teacher/loadExamData`,
        method: 'post',
        data
    })
}
export function saveExam(data){
    return request({
        url: `/api/teacher/saveExam`,
        method: 'post',
        data
    })
}
export function deleteExam(data){
    return request({
        url: `/api/teacher/deleteExam`,
        method: 'post',
        data
    })
}
export function deleteQ(data){
    return request({
        url: `/api/teacher/deleteQ`,
        method: 'post',
        data
    })
}
