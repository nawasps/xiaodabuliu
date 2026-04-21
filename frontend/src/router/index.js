import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/views/front/layout/FrontLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/front/home/HomeIndex.vue')
      },
      {
        path: 'book/:id',
        name: 'BookDetail',
        component: () => import('@/views/front/book/BookDetail.vue')
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/front/search/SearchIndex.vue')
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/front/cart/CartIndex.vue')
      },
      {
        path: 'order/confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/front/order/OrderConfirm.vue')
      },
      {
        path: 'order/pay/:id',
        name: 'OrderPay',
        component: () => import('@/views/front/order/OrderPay.vue')
      },
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('@/views/front/order/OrderList.vue')
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/front/order/OrderDetail.vue')
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/front/user/UserProfile.vue')
      },
      {
  path: 'user/reports',
  name: 'MyReports',
  component: () => import('@/views/front/user/MyReports.vue')
},
      {
  path: 'user/publish/:bookId?',
  name: 'UserPublish',
  component: () => import('@/views/front/user/UserPublish.vue')
},
      {
        path: 'user/my-books',
        name: 'MyBooks',
        component: () => import('@/views/front/user/MyBooks.vue')
      },
      {
        path: 'user/favorites',
        name: 'UserFavorites',
        component: () => import('@/views/front/user/UserFavorites.vue')
      },
      {
        path: 'message',
        name: 'MessageCenter',
        component: () => import('@/views/front/message/MessageCenter.vue')
      },
      {
        path: 'chat/:userId',
        name: 'ChatRoom',
        component: () => import('@/views/front/message/ChatRoom.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/front/user/Login.vue')
  },{
  path: '/reset-password',
  name: 'ResetPassword',
  component: () => import('@/views/front/user/ResetPassword.vue')
},
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/front/user/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/layout/AdminLayout.vue'),
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/DashboardIndex.vue')
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/user/UserManage.vue')
      },
      {
        path: 'book',
        name: 'AdminBook',
        component: () => import('@/views/admin/book/BookManage.vue')
      },
      {
        path: 'order',
        name: 'AdminOrder',
        component: () => import('@/views/admin/order/OrderManage.vue')
      },
      {
        path: 'review',
        name: 'AdminReview',
        component: () => import('@/views/admin/review/ReviewManage.vue')
      },
      {
        path: 'report',
        name: 'AdminReport',
        component: () => import('@/views/admin/report/ReportManage.vue')
      },
      {
        path: 'notice',
        name: 'AdminNotice',
        component: () => import('@/views/admin/notice/NoticeManage.vue')
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('@/views/admin/category/CategoryManage.vue')
      },
      {
        path: 'tag',
        name: 'AdminTag',
        component: () => import('@/views/admin/tag/TagManage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  // 访问管理后台需要 token 和 ADMIN 角色
  if (to.path.startsWith('/admin')) {
    if (!token) {
      next('/login')
    } else if (!userInfo || userInfo.role !== 'ADMIN') {
      next('/')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
