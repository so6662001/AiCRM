import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '@/components/Layout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '工作台', icon: 'Odometer' } },
      { path: 'leads', name: 'LeadList', component: () => import('@/views/lead/LeadList.vue'), meta: { title: '线索管理', icon: 'Magnet' } },
      { path: 'leads/:id', name: 'LeadDetail', component: () => import('@/views/lead/LeadDetail.vue'), meta: { title: '线索详情', hidden: true } },
      { path: 'customers', name: 'CustomerList', component: () => import('@/views/customer/CustomerList.vue'), meta: { title: '客户管理', icon: 'User' } },
      { path: 'customers/:id', name: 'CustomerDetail', component: () => import('@/views/customer/CustomerDetail.vue'), meta: { title: '客户详情', hidden: true } },
      { path: 'opportunities', name: 'OpportunityList', component: () => import('@/views/opportunity/OpportunityList.vue'), meta: { title: '商机管理', icon: 'TrendCharts' } },
      { path: 'opportunities/:id', name: 'OpportunityDetail', component: () => import('@/views/opportunity/OpportunityDetail.vue'), meta: { title: '商机详情', hidden: true } },
      { path: 'follow-ups', name: 'FollowUpList', component: () => import('@/views/followup/FollowUpList.vue'), meta: { title: '跟进记录', icon: 'ChatDotRound' } },
      { path: 'visits', name: 'VisitList', component: () => import('@/views/visit/VisitList.vue'), meta: { title: '拜访管理', icon: 'Location' } },
      { path: 'tasks', name: 'TaskList', component: () => import('@/views/task/TaskList.vue'), meta: { title: '任务管理', icon: 'List' } },
      { path: 'activities', name: 'ActivityList', component: () => import('@/views/activity/ActivityList.vue'), meta: { title: '活动管理', icon: 'Flag' } },
      { path: 'activities/:id', name: 'ActivityDetail', component: () => import('@/views/activity/ActivityDetail.vue'), meta: { title: '活动详情', hidden: true } },
      { path: 'friends', name: 'FriendList', component: () => import('@/views/friend/FriendList.vue'), meta: { title: '好友管理', icon: 'Connection' } },
      { path: 'settings', name: 'Settings', component: () => import('@/views/settings/OrgManage.vue'), meta: { title: '系统设置', icon: 'Setting' } },
    ],
  },
]

export default createRouter({ history: createWebHashHistory(), routes })
