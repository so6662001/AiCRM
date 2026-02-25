<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { taskApi, visitApi } from '@/api/index'
import { formatDateTime, priorityColorMap, visitTypeMap } from '@/utils/format'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayDate = computed(() => dayjs().format('MM月DD日'))

// 数据概览
const summary = ref({ pending: 0, completed: 0, overdue: 0 })
const visitList = ref<any[]>([])
const taskList = ref<any[]>([])
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const [summaryRes, visitRes] = await Promise.all([
      taskApi.todaySummary().catch(() => ({ pending: 5, completed: 12, overdue: 2 })),
      visitApi.page({ pageNum: 1, pageSize: 3, status: 1 }).catch(() => ({ list: [], total: 0 })),
    ])
    summary.value = {
      pending: (summaryRes as any)?.pending ?? 5,
      completed: (summaryRes as any)?.completed ?? 12,
      overdue: (summaryRes as any)?.overdue ?? 2,
    }
    const visitData = visitRes as any
    visitList.value = visitData?.list ?? visitData?.records ?? []
    // 获取今日任务
    const taskRes = await taskApi.page({ pageNum: 1, pageSize: 3, status: 0 }).catch(() => ({ list: [], records: [] }))
    const taskData = taskRes as any
    taskList.value = taskData?.list ?? taskData?.records ?? []
  } catch {
    summary.value = { pending: 5, completed: 12, overdue: 2 }
    visitList.value = []
    taskList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())

onPullDownRefresh(() => {
  loadData().then(() => uni.stopPullDownRefresh())
})

function goTo(path: string) {
  uni.navigateTo({ url: `/pages/${path}` })
}

function goToNotification() {
  uni.navigateTo({ url: '/pages/notification/list' })
}

function goToTaskDetail(id: number) {
  uni.navigateTo({ url: `/pages/task/detail?id=${id}` })
}

function goToNewCustomer() {
  uni.showToast({ title: '新建客户页面开发中', icon: 'none' })
}
</script>

<template>
  <view class="page">
    <!-- 自定义头部 -->
    <view class="header">
      <text class="logo">AiCRM</text>
      <view class="header-right" @click="goToNotification">
        <text class="icon-msg">📩</text>
      </view>
    </view>

    <view class="content">
      <!-- 问候语 -->
      <view class="greeting">
        <text class="greeting-text">{{ greeting }}，{{ userStore.userName }}</text>
        <text class="greeting-date">{{ todayDate }}</text>
      </view>

      <!-- 数据概览卡片 -->
      <view class="summary-row">
        <view class="summary-card card-pending">
          <text class="summary-num">{{ summary.pending }}</text>
          <text class="summary-label">待完成</text>
        </view>
        <view class="summary-card card-completed">
          <text class="summary-num">{{ summary.completed }}</text>
          <text class="summary-label">已完成</text>
        </view>
        <view class="summary-card card-overdue">
          <text class="summary-num">{{ summary.overdue }}</text>
          <text class="summary-label">已逾期</text>
        </view>
      </view>

      <!-- 今日拜访计划 -->
      <view class="section">
        <text class="section-title">今日拜访计划</text>
        <view class="card">
          <view v-if="visitList.length" class="list">
            <view
              v-for="item in visitList"
              :key="item.id"
              class="list-item"
            >
              <view class="item-main">
                <text class="item-time">{{ formatDateTime(item.planTime || item.visitTime) }}</text>
                <text class="item-name">{{ item.customerName || item.customer?.name || '-' }}</text>
                <text class="item-type">{{ item.visitTypeName || visitTypeMap[item.visitType] || '拜访' }}</text>
              </view>
              <view class="item-actions">
                <text class="btn-link" @click="goTo(`visit/detail?id=${item.id}`)">详情</text>
              </view>
            </view>
          </view>
          <view v-else class="empty-text">暂无拜访计划</view>
        </view>
      </view>

      <!-- 今日任务 -->
      <view class="section">
        <text class="section-title">今日任务</text>
        <view class="card">
          <view v-if="taskList.length" class="list">
            <view
              v-for="item in taskList"
              :key="item.id"
              class="list-item task-item"
              @click="goToTaskDetail(item.id)"
            >
              <view class="item-main">
                <view class="task-title-row">
                  <view
                    class="priority-dot"
                    :style="{ backgroundColor: priorityColorMap[item.priority] || '#8E8E93' }"
                  />
                  <text class="item-title">{{ item.title }}</text>
                </view>
                <text class="item-source">{{ item.sourceName || '任务' }}</text>
              </view>
              <text class="icon-arrow">›</text>
            </view>
          </view>
          <view v-else class="empty-text">暂无任务</view>
        </view>
      </view>

      <!-- 快捷操作 -->
      <view class="section">
        <text class="section-title">快捷操作</text>
        <view class="quick-actions">
          <view class="quick-btn" @click="goTo('checkin/index')">
            <view class="quick-icon icon-checkin">📍</view>
            <text class="quick-label">外出打卡</text>
          </view>
          <view class="quick-btn" @click="goTo('visit/create')">
            <view class="quick-icon icon-visit">📋</view>
            <text class="quick-label">新建拜访</text>
          </view>
          <view class="quick-btn" @click="goTo('followUp/create')">
            <view class="quick-icon icon-follow">📝</view>
            <text class="quick-label">新建跟进</text>
          </view>
          <view class="quick-btn" @click="goTo('task/create')">
            <view class="quick-icon icon-task">✓</view>
            <text class="quick-label">新建任务</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 30rpx;
  padding-top: calc(env(safe-area-inset-top) + 60rpx);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0 30rpx;
  .logo {
    font-size: 40rpx;
    font-weight: 700;
    color: #1a1a2e;
  }
  .header-right {
    padding: 12rpx;
    .icon-msg {
      font-size: 44rpx;
    }
  }
}

.content {
  padding-bottom: 40rpx;
}

.greeting {
  margin-bottom: 30rpx;
  .greeting-text {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #1a1a2e;
  }
  .greeting-date {
    font-size: 26rpx;
    color: #636e7b;
    margin-top: 8rpx;
  }
}

.summary-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.summary-card {
  flex: 1;
  border-radius: 16rpx;
  padding: 28rpx;
  text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  .summary-num {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
  }
  .summary-label {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.9);
    margin-top: 8rpx;
  }
}

.card-pending {
  background: linear-gradient(135deg, #4F6EF6 0%, #6B8AFF 100%);
}

.card-completed {
  background: linear-gradient(135deg, #34C759 0%, #5DD879 100%);
}

.card-overdue {
  background: linear-gradient(135deg, #FF9500 0%, #FFB340 100%);
}

.section {
  margin-bottom: 30rpx;
  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1a1a2e;
    margin-bottom: 16rpx;
    display: block;
  }
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eef0f5;
  &:last-child {
    border-bottom: none;
  }
}

.item-main {
  flex: 1;
  .item-time {
    display: block;
    font-size: 24rpx;
    color: #636e7b;
    margin-bottom: 4rpx;
  }
  .item-name {
    font-size: 30rpx;
    font-weight: 500;
    color: #1a1a2e;
  }
  .item-type {
    font-size: 24rpx;
    color: #a0a8b4;
    margin-top: 4rpx;
  }
}

.task-item {
  cursor: pointer;
  .task-title-row {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }
  .priority-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
  }
  .item-title {
    font-size: 30rpx;
    font-weight: 500;
    color: #1a1a2e;
  }
  .item-source {
    font-size: 24rpx;
    color: #a0a8b4;
    margin-top: 4rpx;
  }
}

.btn-link {
  font-size: 28rpx;
  color: #4F6EF6;
  padding: 8rpx 20rpx;
}

.empty-text {
  text-align: center;
  color: #a0a8b4;
  padding: 40rpx 0;
  font-size: 28rpx;
}

.quick-actions {
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}

.quick-btn {
  flex: 1;
  min-width: 150rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  .quick-icon {
    width: 72rpx;
    height: 72rpx;
    line-height: 72rpx;
    margin: 0 auto 12rpx;
    border-radius: 16rpx;
    font-size: 36rpx;
  }
  .icon-checkin {
    background: #E8EDFF;
    color: #4F6EF6;
  }
  .icon-visit {
    background: #E8F8EE;
    color: #34C759;
  }
  .icon-follow {
    background: #FFF3E0;
    color: #FF9500;
  }
  .icon-task {
    background: #E8EDFF;
    color: #4F6EF6;
  }
  .quick-label {
    font-size: 26rpx;
    color: #636e7b;
  }
}

.icon-arrow {
  font-size: 40rpx;
  color: #a0a8b4;
  font-weight: 300;
}
</style>
