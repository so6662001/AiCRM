<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { activityApi } from '@/api/index'
import { formatDateTime, activityStatusMap } from '@/utils/format'

const tabList = [
  { key: 'all', label: '全部' },
  { key: 'need', label: '需要报名' },
  { key: 'no_need', label: '不需要报名' },
]
const statusList = [
  { key: '', label: '全部' },
  { key: '2', label: '进行中' },
  { key: '1', label: '报名中' },
  { key: '0', label: '未开始' },
  { key: '3', label: '已结束' },
]

const activeTab = ref('all')
const activeStatus = ref('')
const list = ref<any[]>([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)
const pageSize = 20
const finished = ref(false)

const params = computed(() => {
  const p: any = { pageNum: pageNum.value, pageSize }
  if (activeTab.value === 'need') p.needSignup = 1
  else if (activeTab.value === 'no_need') p.needSignup = 0
  if (activeStatus.value) p.status = Number(activeStatus.value)
  return p
})

async function loadList(isRefresh = false) {
  if (loading.value) return
  if (isRefresh) {
    pageNum.value = 1
    finished.value = false
  }
  if (finished.value && !isRefresh) return

  loading.value = true
  try {
    const res = await activityApi.page(params.value) as any
    const records = res?.list ?? res?.records ?? []
    total.value = res?.total ?? 0

    if (isRefresh) list.value = records
    else list.value = [...list.value, ...records]

    if (records.length < pageSize) finished.value = true
    else pageNum.value++
  } catch {
    if (isRefresh) list.value = []
  } finally {
    loading.value = false
  }
}

function switchTab(key: string) {
  activeTab.value = key
  loadList(true)
}

function switchStatus(key: string) {
  activeStatus.value = key
  loadList(true)
}

function goToDetail(id: number) {
  uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
}

function onFabClick() {
  uni.showToast({ title: '新建活动请在PC端操作', icon: 'none' })
}

function getCategoryClass(needSignup: number) {
  return needSignup === 1 ? 'tag-blue' : 'tag-green'
}

function getStatusClass(status: number) {
  const map: Record<number, string> = {
    0: 'tag-gray',
    1: 'tag-blue',
    2: 'tag-green',
    3: 'tag-gray',
  }
  return map[status] ?? 'tag-gray'
}

function getParticipantText(item: any) {
  const need = item.needSignup === 1
  if (need && item.signupCount != null && item.maxParticipants != null) {
    return `${item.signupCount}/${item.maxParticipants}`
  }
  if (need && item.signupCount != null) return `${item.signupCount}人`
  if (!need && item.scanCount != null) return `${item.scanCount}次扫码`
  return ''
}

onMounted(() => loadList(true))

onPullDownRefresh(() => {
  loadList(true).then(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  loadList(false)
})
</script>

<template>
  <view class="page">
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

    <!-- 状态筛选 -->
    <scroll-view class="status-scroll" scroll-x>
      <view class="status-tabs">
        <view
          v-for="s in statusList"
          :key="s.key"
          class="status-item"
          :class="{ active: activeStatus === s.key }"
          @click="switchStatus(s.key)"
        >
          {{ s.label }}
        </view>
      </view>
    </scroll-view>

    <!-- 活动列表 -->
    <view class="list-wrap">
      <view
        v-for="item in list"
        :key="item.id"
        class="activity-card"
        @click="goToDetail(item.id)"
      >
        <text class="activity-name">{{ item.name }}</text>
        <view class="card-tags">
          <view :class="['tag', getCategoryClass(item.needSignup)]">
            {{ item.needSignup === 1 ? '需要报名' : '不需要报名' }}
          </view>
          <view :class="['tag', getStatusClass(item.status)]">
            {{ activityStatusMap[item.status] ?? '-' }}
          </view>
        </view>
        <view class="card-meta">
          <text class="meta-item">时间：{{ formatDateTime(item.startTime) }}</text>
          <text class="meta-item">地点：{{ item.location || '-' }}</text>
        </view>
        <view v-if="getParticipantText(item)" class="card-footer">
          <text class="participant-text">参与人数 {{ getParticipantText(item) }}</text>
        </view>
      </view>

      <view v-if="!loading && list.length === 0" class="empty-text">暂无活动</view>
      <view v-if="loading && list.length > 0" class="loading-text">加载中...</view>
      <view v-if="finished && list.length > 0" class="finished-text">— 已加载全部 —</view>
    </view>

    <view class="fab" @click="onFabClick">
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

.tab-bar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.tab-item {
  flex: 1;
  padding: 16rpx;
  font-size: 28rpx;
  text-align: center;
  color: #636e7b;
  background: #fff;
  border-radius: 12rpx;
  &.active {
    background: #4F6EF6;
    color: #fff;
  }
}

.status-scroll {
  margin-bottom: 24rpx;
  white-space: nowrap;
}

.status-tabs {
  display: inline-flex;
  gap: 16rpx;
  padding: 4rpx 0;
}

.status-item {
  display: inline-block;
  padding: 12rpx 28rpx;
  font-size: 26rpx;
  color: #636e7b;
  background: #fff;
  border-radius: 24rpx;
  white-space: nowrap;
  &.active {
    background: #4F6EF6;
    color: #fff;
  }
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.activity-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.activity-name {
  font-size: 34rpx;
  font-weight: 600;
  color: #1a1a2e;
  display: block;
  margin-bottom: 16rpx;
}

.card-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.tag {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-gray { background: #F2F3F5; color: #636e7b; }

.card-meta {
  .meta-item {
    display: block;
    font-size: 26rpx;
    color: #636e7b;
    margin-top: 8rpx;
  }
}

.card-footer {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #eef0f5;
}

.participant-text {
  font-size: 24rpx;
  color: #a0a8b4;
}

.empty-text,
.loading-text,
.finished-text {
  text-align: center;
  color: #a0a8b4;
  padding: 40rpx 0;
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
