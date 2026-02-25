<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { opportunityApi } from '@/api/index'
import { formatMoney, oppStatusMap, oppStageMap } from '@/utils/format'

const statusTabs = [
  { label: '全部', value: '' },
  { label: '进行中', value: 1 },
  { label: '赢单', value: 2 },
  { label: '输单', value: 3 },
]

const statusFilter = ref<number | ''>('')
const list = ref<any[]>([])
const pageNum = ref(1)
const pageSize = 20
const loading = ref(false)
const finished = ref(false)

function getStageClass(status: number) {
  if (status === 5) return 'tag-green'
  if (status === 6) return 'tag-red'
  return 'tag-blue'
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
    const params: any = { pageNum: pageNum.value, pageSize }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = (await opportunityApi.page(params)) as any
    const records = res?.list ?? res?.records ?? []

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

function onTabChange(val: number | string) {
  statusFilter.value = val === '' ? '' : Number(val)
  loadList(true)
}

function goToDetail(id: number) {
  uni.navigateTo({ url: `/pages/opportunity/detail?id=${id}` })
}

onMounted(() => loadList(true))

onPullDownRefresh(() => loadList(true).then(() => uni.stopPullDownRefresh()))

onReachBottom(() => loadList(false))
</script>

<template>
  <view class="page">
    <scroll-view class="scroll-tabs" scroll-x>
      <view class="tabs">
        <view
          v-for="tab in statusTabs"
          :key="String(tab.value)"
          :class="['tab-item', statusFilter === tab.value ? 'active' : '']"
          @click="onTabChange(tab.value as number | string)"
        >
          {{ tab.label }}
        </view>
      </view>
    </scroll-view>

    <view class="list-wrap">
      <view
        v-for="item in list"
        :key="item.id"
        class="card"
        @click="goToDetail(item.id)"
      >
        <view class="card-header">
          <text class="opp-name">{{ item.name || item.opportunityName || '-' }}</text>
          <view :class="['tag', getStageClass(item.stage)]">
            {{ oppStageMap[item.stage] || oppStatusMap[item.status] || '-' }}
          </view>
        </view>
        <view class="card-meta">
          <text class="customer">{{ item.customerName || item.customer?.name || '-' }}</text>
        </view>
        <view class="card-amount">
          {{ formatMoney(item.expectedAmount || item.amount) }}
        </view>
        <view v-if="item.winRate != null" class="card-winrate">
          赢率 {{ item.winRate }}%
        </view>
      </view>

      <view v-if="!loading && list.length === 0" class="empty-text">暂无商机</view>
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

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12rpx;
    .opp-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #1a1a2e;
      flex: 1;
      margin-right: 16rpx;
    }
  }
  .card-meta {
    margin-bottom: 12rpx;
    .customer {
      font-size: 26rpx;
      color: #636e7b;
    }
  }
  .card-amount {
    font-size: 40rpx;
    font-weight: 700;
    color: #4F6EF6;
    margin-bottom: 8rpx;
  }
  .card-winrate {
    font-size: 24rpx;
    color: #a0a8b4;
  }
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  flex-shrink: 0;
}
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-red { background: #FFEBEE; color: #FF3B30; }

.empty-text, .loading-text, .finished-text {
  text-align: center;
  color: #a0a8b4;
  padding: 40rpx 0;
  font-size: 28rpx;
}
</style>
