import request from '@/utils/request'

export function getTeacherStatsOverview() {
    return request({ url: '/api/teacher/stats/overview', method: 'get' })
}

export function getTeacherStatsStudents(params) {
    return request({ url: '/api/teacher/stats/students', method: 'get', params })
}

export function getTeacherStatsStudentDetail(studentId) {
    return request({ url: `/api/teacher/stats/students/${studentId}`, method: 'get' })
}
