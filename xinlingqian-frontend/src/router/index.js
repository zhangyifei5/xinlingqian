import { createRouter, createWebHistory } from 'vue-router'
import { moodTestApi, userApi } from '../api'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/forgot-password',
      name: 'forgotPassword',
      component: () => import('../views/ForgotPasswordView.vue'),
    },
    {
      path: '/register-test',
      name: 'registerTest',
      component: () => import('../views/RegisterTestView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/daily-test',
      name: 'dailyTest',
      component: () => import('../views/DailyTestView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/mood-trend',
      name: 'moodTrend',
      component: () => import('../views/MoodTrendView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/mood-calendar',
      name: 'moodCalendar',
      component: () => import('../views/MoodCalendarView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/history-report/:date',
      name: 'historyReport',
      component: () => import('../views/HistoryReportView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('../views/CommunityView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/counselor',
      name: 'counselor',
      component: () => import('../views/CounselorListView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/counselor/:id',
      name: 'counselorDetail',
      component: () => import('../views/CounselorDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/counselor/apply',
      name: 'counselorApply',
      component: () => import('../views/CounselorApplyView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/consultation/chat/:id',
      name: 'consultationChat',
      component: () => import('../views/ConsultationChatView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/advanced-report',
      name: 'advancedReport',
      component: () => import('../views/AdvancedReportView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/counselor-messages',
      name: 'counselorMessages',
      component: () => import('../views/CounselorMessageView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/customer-service',
      name: 'adminCustomerService',
      component: () => import('../views/AdminCustomerServiceView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/membership',
      name: 'membership',
      component: () => import('../views/MembershipView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/meihua-yijing',
      name: 'meihuaYijing',
      component: () => import('../views/MeihuaYijingView.vue'),
      meta: { requiresAuth: true }
    },
  ],
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  console.log('路由守卫 - 目标路径:', to.path, 'Token存在:', !!token)
  
  if (to.meta.requiresAuth && !token) {
    console.log('路由守卫 - 需要认证但没有Token，跳转到登录')
    next('/login')
    return
  }
  
  if (to.meta.requiresAdmin) {
    try {
      const userInfo = await userApi.getUserInfo()
      if (userInfo.data.code === 200 && userInfo.data.data.role !== 'ADMIN') {
        console.log('路由守卫 - 用户不是管理员，跳转到首页')
        next('/')
        return
      }
    } catch (e) {
      console.error('路由守卫 - 检查管理员权限失败:', e)
      next('/')
      return
    }
  }
  
  if (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') {
    if (token) {
      try {
        console.log('路由守卫 - 已登录用户访问公开页面，检查基线状态')
        const baselineRes = await moodTestApi.checkBaselineStatus()
        console.log('路由守卫 - 基线状态响应:', baselineRes.data)
        if (baselineRes.data.code === 200 && !baselineRes.data.data) {
          console.log('路由守卫 - 用户未完成注册测试，跳转到注册测试页面')
          next('/register-test')
        } else {
          console.log('路由守卫 - 用户已完成注册测试，跳转到首页')
          next('/')
        }
        return
      } catch (e) {
        console.error('路由守卫 - 检查基线状态失败:', e)
        next('/')
        return
      }
    }
    next()
    return
  }
  
  if (to.meta.requiresAuth && token) {
    if (to.path !== '/register-test') {
      try {
        console.log('路由守卫 - 已登录用户访问受保护页面，检查基线状态')
        const baselineRes = await moodTestApi.checkBaselineStatus()
        console.log('路由守卫 - 基线状态响应:', baselineRes.data)
        if (baselineRes.data.code === 200 && !baselineRes.data.data) {
          console.log('路由守卫 - 用户未完成注册测试，跳转到注册测试页面')
          next('/register-test')
          return
        }
      } catch (e) {
        console.error('路由守卫 - 检查基线状态失败:', e)
      }
    }
  }
  
  console.log('路由守卫 - 放行')
  next()
})

export default router
