<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { visitApi } from '@/api/index'
import { formatDateTime, visitTypeMap, visitStatusMap } from '@/utils/format'

const statusTabs = [
  { label: '全部', value: '' },
  { label: '计划中', value: 0 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
]

const statusFilter = ref<number | ''>('')
const list = ref<any[]>([])
const pageNum = ref(1)
const pageSize = 20
const loading = ref(false)
const finished = ref(false)

function getStatusClass(status: number) {
  const map: Record<number, string> = {
    0: 'tag-blue',
    1: 'tag-orange',
    2: 'tag-green',
    3: 'tag-gray',
  }
  return map[status] ?? 'tag-gray'
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
    const res = (await visitApi.page(params)) as any
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
  uni.navigateTo({ url: `/pages/visit/detail?id=${id}` })
}

function goToCreate() {
  uni.navigateTo({ url: '/pages/visit/create' })
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
          <text class="customer-name">{{ item.customerName || item.customer?.name || '-' }}</text>
          <view :class="['tag', getStatusClass(item.status)]">
            {{ visitStatusMap[item.status] ?? '-' }}
          </view>
        </view>
        <view class="card-meta">
          <view class="tag tag-type">{{ visitTypeMap[item.visitType] ?? '-' }}</view>
          <text class="meta-time">{{ formatDateTime(item.planTime || item.visitTime) }}</text>
        </view>
      </view>

      <view v-if="!loading && list.length === 0" class="empty-text">暂无拜访记录</view>
      <view v-if="loading && list.length > 0" class="loading-text">加载中...</view>
      <view v-if="finished && list.length > 0" class="finished-text">— 已加载全部 —</view>
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
    .customer-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #1a1a2e;
    }
  }
  .card-meta {
    display: flex;
    align-items: center;
    gap: 16rpx;
    .meta-time {
      font-size: 26rpx;
      color: #636e7b;
    }
  }
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.tag-type {
  background: #E8EDFF;
  color: #4F6EF6;
}
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-orange { background: #FFF3E0; color: #FF9500; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-gray { background: #F2F3F5; color: #636e7b; }

.empty-text, .loading-text, .finished-text {
  text-align: center;
  color: #a0a8b4;
  padding: 40rpx 0;
  font-size: 28rpx;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 120rpx;
  width: 100rpx;
  height: 100rpx;
  background: #4F6EF6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(79, 110, 246, 0.4);
  .fab-icon {
    font-size: 56rpx;
    color: #fff;
    line-height: 1;
    font-weight: 300;
  }
}
</style>
