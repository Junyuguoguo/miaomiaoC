import request from '@/utils/request'

// 考试相关API
export const examApi = {





    /**
     * 记录违规行为
     * @param {Object} data - 违规记录数据
     * @param {string|number} data.examId - 考试ID
     * @param {string} data.violationType - 违规类型
     * @param {number} data.count - 违规次数
     * @returns {Promise} 返回记录结果
     */
    logViolation(data) {
        return request({
            url: '/api/exam/violation',
            method: 'post',
            data
        })
    },
    /**
     * 保存答题进度
     * @param {Object} data - 保存进度的数据
     * @returns {Promise} 返回保存结果
     */
    saveProgress(data) {
        return request({
            url: '/api/exam/progress',
            method: 'post',
            data
        })
    },

    /**
     * 获取答题进度
     * @param {string|number} examId - 考试ID
     * @returns {Promise} 返回已保存的进度
     */
    getProgress(examId, userId) {
        return request({
            url: `/api/exam/${examId}/progress`,
            method: 'get',
            params: { userId }
        })
    }
}
export function getExamList(data){
    return request({
        url: `/api/exam/getExamList`,
        method: 'post',
        data
    })
}
export function getExamByExamId(data){
    return request({
        url: `/api/exam/getExamByExamId`,
        method: 'post',
        data
    })
}
export function getExamQuestion(data){
    return request({
        url: `/api/exam/getExamQuestion`,
        method: 'post',
        data
    })
}
export function submitQuestion(data){
    return request({
        url: `/api/exam/submitQuestion`,
        method: 'post',
        data
    })
}
/**
 * 运行代码
 * @param {Object} data - 运行代码的数据
 * @param {string|number} data.examId - 考试ID
 * @param {string} data.code - 用户代码
 * @param {string} data.language - 编程语言
 * @returns {Promise} 返回运行结果
 */
export function runTestCode(data) {
    return request({
        url: '/api/exam/runTestCode',
        method: 'post',
        data
    })
}
/**
 * 提交考试
 * @param {Object} data - 提交考试的数据
 * @param {string|number} data.examId - 考试ID
 * @param {string} data.code - 最终代码
 * @param {number} data.fullscreenExitCount - 退出全屏次数
 * @param {number} data.timeSpent - 用时（秒）
 * @returns {Promise} 返回提交结果
 */
export function submitExam(data) {
    return request({
        url: '/api/exam/submitExam',
        method: 'post',
        data
    })
}
export function getExamResultById(data) {
    return request({
        url: '/api/exam/getExamResultById',
        method: 'post',
        data
    })
}
export function getExamRecordList(data) {
    return request({
        url: '/api/exam/getExamRecordList',
        method: 'post',
        data
    })
}
export const batchUpdateExamStatus = (updates) => {
    return request({
        url: '/api/exam/batchUpdateStatus',
        method: 'POST',
        data: updates // 格式: [{ id: 1, newStatus: 1 }, ...]
    })
}
