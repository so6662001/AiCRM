<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { friendApi } from '@/api/index'
import { friendTypeMap } from '@/utils/format'

const tabList = [
  { key: 'all', label: '全部' },
  { key: '1', label: '平台好友' },
  { key: '2', label: '企微好友' },
  { key: '3', label: '双渠道' },
]

const keyword = ref('')
const activeTab = ref('all')
const list = ref<any[]>([])
const stats = ref<any>(null)
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)
const pageSize = 20
const finished = ref(false)

const params = computed(() => {
  const p: any = { pageNum: pageNum.value, pageSize, keyword: keyword.value || undefined }
  if (activeTab.value !== 'all') p.friendType = Number(activeTab.value)
  return p
})

async function loadStats() {
  try {
    const res = await friendApi.statistics() as any
    stats.value = res
  } catch {
    stats.value = null
  }
}

async function loadList(isRefresh = false) {
  if (loading.value) return
  if (isRefresh) {
    pageNum.value = 1
    finished.value = false
  }
  if (finished.value && !isRefresh) return

  loading.value = true
  try {
    const res = await friendApi.page(params.value) as any
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

function onSearch() {
  loadList(true)
}

function goToDetail(id: number) {
  uni.navigateTo({ url: `/pages/friend/detail?id=${id}` })
}

function getTypeClass(type: number) {
  const map: Record<number, string> = {
    1: 'tag-blue',
    2: 'tag-green',
    3: 'tag-purple',
  }
  return map[type] ?? 'tag-gray'
}

onMounted(() => {
  loadStats()
  loadList(true)
})

onPullDownRefresh(() => {
  Promise.all([loadStats(), loadList(true)]).then(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  loadList(false)
})
</script>

<template>
  <view class="page">
    <!-- 搜索框 -->
    <view class="search-bar">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索好友"
        type="text"
        confirm-type="search"
        @confirm="onSearch"
      />
      <view class="search-btn" @click="onSearch">搜索</view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-row">
      <view class="stat-card">
        <text class="stat-value">{{ stats?.totalCount ?? 0 }}</text>
        <text class="stat-label">总好友数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats?.monthNewCount ?? 0 }}</text>
        <text class="stat-label">本月新增</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats?.convertedCount ?? 0 }}</text>
        <text class="stat-label">已转客户</text>
      </view>
    </view>

    <!-- 筛选Tab -->
    <scroll-view class="scroll-tabs" scroll-x>
      <view class="tabs">
        <view
          v-for="tab in tabList"
          :key="tab.key"
          :class="['tab-item', activeTab === tab.key ? 'active' : '']"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </view>
      </view>
    </scroll-view>

    <!-- 好友列表 -->
    <view class="list-wrap">
      <view
        v-for="item in list"
        :key="item.id"
        class="friend-card"
        @click="goToDetail(item.id)"
      >
        <view class="card-header">
          <text class="friend-name">{{ item.name || item.nickname || '未命名' }}</text>
          <view v-if="item.customerId" class="linked-badge">✓</view>
        </view>
        <view class="card-meta">
          <text class="meta-item">{{ item.company || '-' }} · {{ item.position || '-' }}</text>
        </view>
        <view class="card-tags">
          <view :class="['tag', getTypeClass(item.friendType)]">
            {{ friendTypeMap[item.friendType] ?? '-' }}
          </view>
          <view v-if="item.source" class="tag tag-source">{{ item.source }}</view>
        </view>
        <view v-if="item.customerId" class="linked-text">已关联客户</view>
      </view>

      <view v-if="!loading && list.length === 0" class="empty-text">暂无好友</view>
      <view v-if="loading && list.length > 0" class="loading-text">加载中...</view>
      <view v-if="finished && list.length > 0" class="finished-text">— 已加载全部 —</view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 30rpx;
  padding-bottom: 60rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;
  .search-input {
    flex: 1;
    height: 72rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
  }
  .search-btn {
    width: 120rpx;
    height: 72rpx;
    line-height: 72rpx;
    text-align: center;
    background: #4F6EF6;
    color: #fff;
    border-radius: 16rpx;
    font-size: 28rpx;
  }
}

.stats-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.stat-value {
  font-size: 36rpx;
  font-weight: 600;
  color: #4F6EF6;
  display: block;
}

.stat-label {
  font-size: 24rpx;
  color: #636e7b;
  margin-top: 8rpx;
  display: block;
}

.scroll-tabs {
  white-space: nowrap;
  margin-bottom: 24rpx;
  .tabs {
    display: inline-flex;
    gap: 16rpx;
    padding: 4rpx 0;
  }
  .tab-item {
    display: inline-block;
    padding: 12rpx 28rpx;
    font-size: 26rpx;
    color: #636e7b;
    background: #fff;
    border-radius: 32rpx;
    white-space: nowrap;
  }
  .tab-item.active {
    background: #4F6EF6;
    color: #fff;
  }
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.friend-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.friend-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a2e;
}

.linked-badge {
  width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  background: #E8F8EE;
  color: #34C759;
  border-radius: 50%;
  font-size: 24rpx;
  font-weight: 600;
}

.card-meta {
  margin-bottom: 12rpx;
  .meta-item {
    font-size: 26rpx;
    color: #636e7b;
  }
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.tag {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-purple { background: #F3E8FF; color: #8B5CF6; }
.tag-gray { background: #F2F3F5; color: #636e7b; }
.tag-source { background: #FFF3E0; color: #FF9500; }

.linked-text {
  font-size: 24rpx;
  color: #34C759;
  margin-top: 8rpx;
}

.empty-text,
.loading-text,
.finished-text {
  text-align: center;
  color: #a0a8b4;
  padding: 40rpx 0;
  font-size: 28rpx;
}
</style>
