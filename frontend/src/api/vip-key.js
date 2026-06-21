import request from '@/utils/request'

export function generateVipKeys(data) {
    return request({
        url: '/api/vip/key/generate',
        method: 'post',
        data
    })
}

export function listVipKeys(data) {
    return request({
        url: '/api/vip/key/list',
        method: 'post',
        data
    })
}

export function redeemVipKey(data) {
    return request({
        url: '/api/vip/key/redeem',
        method: 'post',
        data
    })
}

export function deleteVipKey(data) {
    return request({
        url: '/api/vip/key/delete',
        method: 'post',
        data
    })
}
