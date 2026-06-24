import { createRouter, createWebHistory } from 'vue-router'
import {login, verifyToken} from "@/api/auth.js";
import {STORAGE_KEYS, useUserStore} from "@/stores/user.js";

const router = createRouter({
    // ✅ 正确：路由 base 应该是 '/'（部署在根目录）或 '/子目录名/'
    history: createWebHistory('/'),
    routes: [
        // 路由转发
        {
            path: '/',
            redirect: '/login'  // 将根路径重定向到登录页
        },

        // 登陆页面注册
        {
            path: '/login',
            name: 'login',
            component: () => import('@/views/login/LoginPage.vue'),
            meta: { requiresAuth: false }
        },

        // 注册页面
        {
            path: '/register',
            name: 'register',
            component: () => import('@/views/login/RegisterPage.vue'),
            meta: {
                requiresAuth: false,
                title: '用户注册'
            }
        },
        // 忘记密码页面
        {
            path: '/forgot',
            name: 'forgot',
            component: () => import('@/views/login/ForgotPage.vue'),
            meta: {
                requiresAuth: false,
                title: '忘记密码'
            }
        },
        // 学生页面
        {
            path: '/exam',
            name: 'exam',
            component: () => import('@/views/exam/HomePage.vue'),
            meta: {
                requiresAuth: true,
                title: '考试主页'
            },
            children: [

                // {
                //     path: 'detail/:id',
                //     name: 'examDetail',
                //     component: () => import('@/views/exam/ExamDetail.vue'),
                //     meta: { title: '考试详情' }
                // },
                // {
                //     path: 'taking/:id',
                //     name: 'examTaking',
                //     component: () => import('@/views/exam/ExamTaking.vue'),
                //     meta: { title: '进行考试', requiresAuth: true }
                // },
                // {
                //     path: 'result/:id',
                //     name: 'examResult',
                //     component: () => import('@/views/exam/ExamResult.vue'),
                //     meta: { title: '考试结果' }
                // }
            ]
        },
        {
            path: '/exam/startExam/:id',
            name: 'startExam',
            component: () => import('@/views/exam/startExamPage.vue'),
            meta: { title: '开始考试' }
        },
        {
            path: '/exam/doingExam/:id',  // 正在考试
            name: 'doingExam',
            component: () => import('@/views/exam/doingExamPage.vue'),
            meta: {
                requiresAuth: true,
                requiresFullscreen: true  // 自定义元信息，表示需要强制全屏
            }
        },
        {
            path: '/exam/resultExam/:id',
            name: 'resultExam',
            component: () => import('@/views/exam/ResultExamPage.vue'),
            meta: {
                requiresAuth: true,
                requiresFullscreen: true  // 自定义元信息，表示需要强制全屏
            }
        },
        // router/index.js - 路由配置不变
        {
            path: '/question/questionList',
            name: 'questionList',
            component: () => import('@/views/question/QuestionListPage.vue'),
            meta: {
                requiresAuth: true,
                requiresFullscreen: true
            }
        },
        {
            path: '/question/practiceQuestion/:id',
            name: 'practiceQuestion',
            component: () => import('@/views/question/PracticeQuestionPage.vue'),
            meta: {
                requiresAuth: true,
                requiresFullscreen: true  // 自定义元信息，表示需要强制全屏
            }
        },
        {
            path: '/teacher',
            name: 'manager',
            component: () => import('@/views/teacher/TeacherManagerPage.vue'),
            meta: {
                requiresAuth: true,
                requiresFullscreen: true  // 自定义元信息，表示需要强制全屏
            }
        },
        // 管理员后台
        {
            path: '/admin',
            name: 'admin',
            component: () => import('@/views/admin/AdminPage.vue'),
            meta: {
                requiresAuth: true,
                requiresFullscreen: true
            }
        },
        // 在线聊天 - 聊天大厅
        {
            path: '/chat',
            name: 'chatHub',
            component: () => import('@/views/chat/ChatHub.vue'),
            meta: {
                requiresAuth: true,
                title: '在线交流'
            }
        },
        // 在线聊天 - 私聊
        {
            path: '/chat/private',
            name: 'chatPrivate',
            component: () => import('@/views/chat/ChatPage.vue'),
            meta: {
                requiresAuth: true,
                title: '私聊'
            }
        },
        // 在线聊天 - 群聊
        {
            path: '/chat/room',
            name: 'chatRoom',
            component: () => import('@/views/chat/ChatPage.vue'),
            meta: {
                requiresAuth: true,
                title: '群聊'
            }
        }
        // {
        //     path: '/user',
        //     component: () => import('@/layouts/UserLayout.vue'),
        //     meta: { requiresAuth: true },
        //     children: [
        //         {
        //             path: 'profile',
        //             name: 'profile',
        //             component: () => import('@/views/user/Profile.vue'),
        //             meta: { title: '个人信息' }
        //         },
        //         {
        //             path: 'history',
        //             name: 'history',
        //             component: () => import('@/views/user/History.vue'),
        //             meta: { title: '考试历史' }
        //         }
        //     ]
        // },
        // {
        //     path: '/admin',
        //     component: () => import('@/layouts/AdminLayout.vue'),
        //     meta: { requiresAuth: true, role: 'admin' },
        //     children: [
        //         {
        //             path: 'dashboard',
        //             name: 'adminDashboard',
        //             component: () => import('@/views/admin/Dashboard.vue'),
        //             meta: { title: '仪表盘' }
        //         },
        //         {
        //             path: 'questions',
        //             name: 'questionBank',
        //             component: () => import('@/views/admin/QuestionBank.vue'),
        //             meta: { title: '题库管理' }
        //         },
        //         {
        //             path: 'exams',
        //             name: 'examManage',
        //             component: () => import('@/views/admin/ExamManage.vue'),
        //             meta: { title: '考试管理' }
        //         },
        //         {
        //             path: 'users',
        //             name: 'userManage',
        //             component: () => import('@/views/admin/UserManage.vue'),
        //             meta: { title: '用户管理' }
        //         }
        //     ]
        // }
    ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
    let username = ''
    let flag = 0
    let userInfo = {}

    try {
        // 第一步：读取 localStorage 中的用户信息字符串
        const userInfoStr = localStorage.getItem(STORAGE_KEYS.USER_INFO)

        // 第二步：只有字符串存在且非空时才解析
        if (userInfoStr && userInfoStr !== 'undefined' && userInfoStr !== 'null') {
            // 解析成 JavaScript 对象
            userInfo = JSON.parse(userInfoStr)
            // 第三步：安全读取 username 属性（加兜底）
            username = userInfo.username || ''
        }
    } catch (e) {
        // 解析失败时的兜底处理（避免代码崩溃）
        console.error('解析用户信息失败:', e)
        username = ''
    }

    console.log("正在校验合法性 1：username=",username)

    // 如果访问的是登录页、注册页、忘记密码页，直接放行
    if (to.path === '/login' || to.path === '/register' || to.path === '/forgot') {
        next()
        return
    }

    // 如果访问 teacher admin 需要单独 校验权限
    if(to.path === '/teacher' || to.path === '/admin'){
        // 前端先检查角色，避免无权限请求发到后端
        const roleId = userInfo.role_id || userInfo.roleId
        if (!roleId || roleId <= 2) {
            console.warn('前端角色校验失败，无权访问', to.path)
            next('/exam')
            return
        }
        flag = 1
        console.log('进入特殊身份验证')
    }

    // 如果没有 token，跳转到登录页
    if (!token) {
        next('/login')
        return
    }

    try {
        // 请求后端验证 token 的有效性
        console.log('开始验证 token...')
        const res = await verifyToken({
            token: token,
            username:username,
            flag:flag
        })
        console.log('token 验证结果:', res)
        if (res.code === 200) {
            // token 有效，放行
            console.log('token 验证通过，放行到', to.path)
            next()
        } else {
            // token 无效，清除并跳转到登录页
            console.warn('token 验证失败，code:', res.code, ', message:', res.message)
            localStorage.removeItem(STORAGE_KEYS.TOKEN)
            localStorage.removeItem(STORAGE_KEYS.USER_INFO)
            localStorage.removeItem(STORAGE_KEYS.ROLE)
            next('/login')
        }
    } catch (error) {
        console.error('Token 验证失败 (网络异常):', error)
        // 验证出错时也清除 token 并跳转到登录页
        localStorage.removeItem(STORAGE_KEYS.TOKEN)
        localStorage.removeItem(STORAGE_KEYS.USER_INFO)
        localStorage.removeItem(STORAGE_KEYS.ROLE)
        next('/login')
    }
})

export default router