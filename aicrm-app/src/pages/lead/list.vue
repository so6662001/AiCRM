<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { leadApi } from '@/api/index'
import { leadStatusMap, leadIntentionMap } from '@/utils/format'

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待分配', value: 0 },
  { label: '已分配', value: 1 },
  { label: '跟进中', value: 2 },
  { label: '公海池', value: 4 },
]

const keyword = ref('')
const statusFilter = ref<number | ''>('')
const list = ref<any[]>([])
const pageNum = ref(1)
const pageSize = 20
const loading = ref(false)
const finished = ref(false)

function getIntentionClass(intention: string) {
  const map: Record<string, string> = {
    A: 'tag-green',
    B: 'tag-blue',
    C: 'tag-orange',
    D: 'tag-gray',
  }
  return map[intention] ?? 'tag-gray'
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
    if (keyword.value) params.keyword = keyword.value
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = (await leadApi.page(params)) as any
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

function onSearch() {
  loadList(true)
}

function onTabChange(val: number | string) {
  statusFilter.value = val === '' ? '' : Number(val)
  loadList(true)
}

function goToDetail(id: number) {
  uni.navigateTo({ url: `/pages/lead/detail?id=${id}` })
}

onMounted(() => loadList(true))

onPullDownRefresh(() => loadList(true).then(() => uni.stopPullDownRefresh()))

onReachBottom(() => loadList(false))
</script>

<template>
  <view class="page">
    <view class="search-bar">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索线索"
        type="text"
        confirm-type="search"
        @confirm="onSearch"
      />
      <view class="search-btn" @click="onSearch">搜索</view>
    </view>

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
          <text class="contact">{{ item.contactName || item.name || '-' }}</text>
          <view :class="['tag', getIntentionClass(item.intentionLevel)]">
            {{ leadIntentionMap[item.intentionLevel] || item.intentionLevel || '-' }}
          </view>
        </view>
        <view class="card-meta">
          <text class="company">{{ item.companyName || item.company || '-' }}</text>
        </view>
        <view class="card-footer">
          <view class="tag tag-source">{{ item.sourceName || item.source || '-' }}</view>
          <view class="tag tag-status">{{ leadStatusMap[item.status] ?? '-' }}</view>
        </view>
      </view>

      <view v-if="!loading && list.length === 0" class="empty-text">暂无线索</view>
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
    .contact {
      font-size: 32rpx;
      font-weight: 600;
      color: #1a1a2e;
    }
  }
  .card-meta {
    margin-bottom: 12rpx;
    .company {
      font-size: 26rpx;
      color: #636e7b;
    }
  }
  .card-footer {
    display: flex;
    gap: 12rpx;
  }
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.tag-source { background: #E8EDFF; color: #4F6EF6; }
.tag-status { background: #F2F3F5; color: #636e7b; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-orange { background: #FFF3E0; color: #FF9500; }
.tag-gray { background: #F2F3F5; color: #636e7b; }

.empty-text, .loading-text, .finished-text {
  text-align: center;
  color: #a0a8b4;
  padding: 40rpx 0;
  font-size: 28rpx;
}
</style>
