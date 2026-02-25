<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { taskApi } from '@/api/index'
import { formatDateTime, priorityColorMap, taskStatusMap } from '@/utils/format'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

const tabList = [
  { key: 'all', label: '全部' },
  { key: 'today', label: '今日' },
  { key: 'week', label: '本周' },
  { key: 'overdue', label: '已逾期' },
]
const activeTab = ref('all')
const taskList = ref<any[]>([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)
const pageSize = 20

const statusFilter = computed(() => {
  if (activeTab.value === 'overdue') return 4
  return undefined
})

const dateRange = computed(() => {
  if (activeTab.value === 'today') {
    const today = dayjs().format('YYYY-MM-DD')
    return { start: today, end: today }
  }
  if (activeTab.value === 'week') {
    const start = dayjs().startOf('week').add(1, 'day').format('YYYY-MM-DD')
    const end = dayjs().endOf('week').add(1, 'day').format('YYYY-MM-DD')
    return { start, end }
  }
  return null
})

async function loadTasks(isRefresh = false) {
  if (isRefresh) pageNum.value = 1
  loading.value = true
  try {
    const params: any = {
      pageNum: pageNum.value,
      pageSize,
      assigneeUserId: userStore.userId,
    }
    if (statusFilter.value != null) params.status = statusFilter.value
    if (dateRange.value) {
      params.planStartTime = dateRange.value.start
      params.planEndTime = dateRange.value.end
    }
    const res = await taskApi.page(params).catch(() => ({ list: [], records: [], total: 0 }))
    const data = res as any
    const list = data?.list ?? data?.records ?? []
    total.value = data?.total ?? 0
    if (isRefresh) taskList.value = list
    else taskList.value = [...taskList.value, ...list]
  } catch {
    if (isRefresh) taskList.value = []
  } finally {
    loading.value = false
  }
}

function goTo(path: string) {
  uni.navigateTo({ url: `/pages/${path}` })
}

function goToDetail(id: number) {
  uni.navigateTo({ url: `/pages/task/detail?id=${id}` })
}

function goToCreate() {
  uni.navigateTo({ url: '/pages/task/create' })
}

function switchTab(key: string) {
  activeTab.value = key
  loadTasks(true)
}

onMounted(() => loadTasks(true))

onPullDownRefresh(() => {
  loadTasks(true).then(() => uni.stopPullDownRefresh())
})
</script>

<template>
  <view class="page">
    <!-- 功能入口区 -->
    <view class="entry-grid">
      <view class="entry-row">
        <view class="entry-item" @click="goTo('visit/list')">
          <view class="entry-icon icon-1">📋</view>
          <text class="entry-label">拜访管理</text>
        </view>
        <view class="entry-item" @click="goTo('checkin/index')">
          <view class="entry-icon icon-2">📍</view>
          <text class="entry-label">外勤打卡</text>
        </view>
        <view class="entry-item" @click="goTo('activity/list')">
          <view class="entry-icon icon-3">🎯</view>
          <text class="entry-label">活动管理</text>
        </view>
        <view class="entry-item" @click="goTo('friend/list')">
          <view class="entry-icon icon-4">👥</view>
          <text class="entry-label">好友管理</text>
        </view>
      </view>
      <view class="entry-row">
        <view class="entry-item" @click="goTo('lead/list')">
          <view class="entry-icon icon-5">💡</view>
          <text class="entry-label">线索管理</text>
        </view>
        <view class="entry-item" @click="goTo('opportunity/list')">
          <view class="entry-icon icon-6">💰</view>
          <text class="entry-label">商机管理</text>
        </view>
        <view class="entry-item" @click="goTo('followUp/create')">
          <view class="entry-icon icon-7">✏️</view>
          <text class="entry-label">新建跟进</text>
        </view>
        <view class="entry-item" @click="goTo('report/personal')">
          <view class="entry-icon icon-8">📊</view>
          <text class="entry-label">工作统计</text>
        </view>
      </view>
    </view>

    <view class="divider" />
    <text class="section-title">我的任务</text>

    <!-- 筛选Tab -->
    <view class="tab-bar">
      <view
        v-for="t in tabList"
        :key="t.key"
        class="tab-item"
        :class="{ active: activeTab === t.key }"
        @click="switchTab(t.key)"
      >
        {{ t.label }}
      </view>
    </view>

    <!-- 任务列表 -->
    <view class="task-list">
      <view
        v-for="item in taskList"
        :key="item.id"
        class="task-card"
        @click="goToDetail(item.id)"
      >
        <view class="task-header">
          <view
            class="priority-dot"
            :style="{ backgroundColor: priorityColorMap[item.priority] || '#8E8E93' }"
          />
          <text class="task-title">{{ item.title }}</text>
        </view>
        <view class="task-meta">
          <text class="tag" :class="'tag-' + (item.status === 2 ? 'green' : item.status === 4 ? 'red' : 'blue')">
            {{ taskStatusMap[item.status] ?? '未知' }}
          </text>
          <text class="task-time">{{ formatDateTime(item.planStartTime || item.planTime) }}</text>
        </view>
        <text class="task-source">{{ item.assignType === 1 ? '自主' : '上级安排' }}{{ item.assignerName ? ` · ${item.assignerName}` : '' }}</text>
      </view>
      <view v-if="!loading && taskList.length === 0" class="empty-text">暂无任务</view>
    </view>

    <view class="fab" @click="goToCreate">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 30rpx;
  padding-bottom: 120rpx;
}

.entry-grid {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.entry-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
  &:last-child {
    margin-bottom: 0;
  }
}

.entry-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 12rpx;
}

.entry-icon {
  width: 64rpx;
  height: 64rpx;
  line-height: 64rpx;
  margin: 0 auto 8rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
}

.icon-1 { background: #E8F4FF; }
.icon-2 { background: #E8EDFF; }
.icon-3 { background: #FFF3E0; }
.icon-4 { background: #E8F8EE; }
.icon-5 { background: #FFF8E1; }
.icon-6 { background: #E8F5E9; }
.icon-7 { background: #F3E5F5; }
.icon-8 { background: #E3F2FD; }

.entry-label {
  font-size: 24rpx;
  color: #636e7b;
}

.divider {
  height: 1rpx;
  background: #eef0f5;
  margin: 24rpx 0;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 20rpx;
  display: block;
}

.tab-bar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.tab-item {
  padding: 12rpx 28rpx;
  font-size: 26rpx;
  color: #636e7b;
  background: #fff;
  border-radius: 24rpx;
  &.active {
    background: #4F6EF6;
    color: #fff;
  }
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.task-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.task-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.priority-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.task-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a2e;
  flex: 1;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-red { background: #FFEBEE; color: #FF3B30; }

.task-time {
  font-size: 24rpx;
  color: #636e7b;
}

.task-source {
  font-size: 24rpx;
  color: #a0a8b4;
}

.empty-text {
  text-align: center;
  color: #a0a8b4;
  padding: 80rpx 0;
  font-size: 28rpx;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: calc(env(safe-area-inset-bottom) + 120rpx);
  width: 100rpx;
  height: 100rpx;
  background: #4F6EF6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(79, 110, 246, 0.4);
}

.fab-icon {
  font-size: 48rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}
</style>
