import request from '@/utils/request'

export function getVipPlans() {
    return request({
        url: '/api/vip/plans',
        method: 'get'
    })
}

export function getManageVipPlans() {
    return request({
        url: '/api/vip/manage/plans',
        method: 'get'
    })
}

export function saveVipPlan(data) {
    return request({
        url: '/api/vip/manage/save',
        method: 'post',
        data
    })
}

export function deleteVipPlan(data) {
    return request({
        url: '/api/vip/manage/delete',
        method: 'post',
        data
    })
}
