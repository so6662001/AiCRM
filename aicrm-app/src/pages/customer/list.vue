<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { customerApi } from '@/api/index'
import { formatDateTime, customerStageMap } from '@/utils/format'
import dayjs from 'dayjs'

const keyword = ref('')
const lifecycleStage = ref<number | ''>('')
const list = ref<any[]>([])
const pageNum = ref(1)
const pageSize = 20
const total = ref(0)
const loading = ref(false)
const finished = ref(false)

const stageTabs = [
  { label: '全部', value: '' },
  { label: '潜在', value: 1 },
  { label: '意向', value: 2 },
  { label: '成交', value: 3 },
  { label: '活跃', value: 4 },
  { label: 'VIP', value: 5 },
]

function getStageTagClass(stage: number) {
  const map: Record<number, string> = {
    1: 'tag-blue',
    2: 'tag-orange',
    3: 'tag-green',
    5: 'tag-purple',
    4: 'tag-blue',
    6: 'tag-gray',
    7: 'tag-gray',
    8: 'tag-red',
  }
  return map[stage] || 'tag-gray'
}

function getPurchaseCountdown(item: any) {
  const date = item.expectedPurchaseDate
  if (!date) return null
  const end = dayjs(date)
  const now = dayjs()
  const days = end.diff(now, 'day')
  if (days < 0) return { text: '已过期', class: 'countdown-expired' }
  if (days <= 7) return { text: `${days}天后到期`, class: 'countdown-warning' }
  return { text: `${days}天后到期`, class: 'countdown-normal' }
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
    const params: any = {
      pageNum: pageNum.value,
      pageSize,
      keyword: keyword.value || undefined,
    }
    if (lifecycleStage.value !== '') {
      params.lifecycleStage = lifecycleStage.value
    }
    const res = await customerApi.page(params) as any
    const records = res?.list ?? res?.records ?? []
    total.value = res?.total ?? 0

    if (isRefresh) {
      list.value = records
    } else {
      list.value = [...list.value, ...records]
    }
    if (records.length < pageSize) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch {
    if (isRefresh) list.value = []
  } finally {
    loading.value = false
  }
}

function onSearch() {
  loadList(true)
}

function onTabChange(stage: number | string) {
  lifecycleStage.value = stage === '' ? '' : Number(stage)
  loadList(true)
}

function goToDetail(id: number) {
  uni.navigateTo({ url: `/pages/customer/detail?id=${id}` })
}

function goToNewCustomer() {
  uni.showToast({ title: '新建客户页面开发中', icon: 'none' })
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
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索客户名称"
        type="text"
        confirm-type="search"
        @confirm="onSearch"
      />
      <view class="search-btn" @click="onSearch">搜索</view>
    </view>

    <!-- 筛选Tab -->
    <scroll-view class="scroll-tabs" scroll-x>
      <view class="tabs">
        <view
          v-for="tab in stageTabs"
          :key="String(tab.value)"
          :class="['tab-item', lifecycleStage === tab.value ? 'active' : '']"
          @click="onTabChange(tab.value as number | string)"
        >
          {{ tab.label }}
        </view>
      </view>
    </scroll-view>

    <!-- 客户列表 -->
    <view class="list-wrap">
      <view
        v-for="item in list"
        :key="item.id"
        class="customer-card"
        @click="goToDetail(item.id)"
      >
        <view class="card-header">
          <text class="customer-name">{{ item.name }}</text>
          <view :class="['tag', getStageTagClass(item.lifecycleStage)]">
            {{ customerStageMap[item.lifecycleStage] || '-' }}
          </view>
        </view>
        <view class="card-meta">
          <text class="meta-item">{{ item.industry || '-' }} · {{ item.region || '-' }}</text>
        </view>
        <view class="card-info">
          <text class="info-item">负责人：{{ item.ownerName || item.owner?.name || '-' }}</text>
          <text class="info-item">最后跟进：{{ formatDateTime(item.lastFollowTime) }}</text>
        </view>
        <view v-if="getPurchaseCountdown(item)" class="card-footer">
          <view :class="['countdown-tag', getPurchaseCountdown(item)!.class]">
            {{ getPurchaseCountdown(item)!.text }}
          </view>
        </view>
      </view>

      <view v-if="!loading && list.length === 0" class="empty-text">暂无客户数据</view>
      <view v-if="loading && list.length > 0" class="loading-text">加载中...</view>
      <view v-if="finished && list.length > 0" class="finished-text">— 已加载全部 —</view>
    </view>

    <!-- 浮动按钮 -->
    <view class="fab" @click="goToNewCustomer">
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

.customer-card {
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
    margin-bottom: 12rpx;
    .meta-item {
      font-size: 26rpx;
      color: #636e7b;
    }
  }
  .card-info {
    .info-item {
      display: block;
      font-size: 24rpx;
      color: #a0a8b4;
      margin-top: 4rpx;
    }
  }
  .card-footer {
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx solid #eef0f5;
  }
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-orange { background: #FFF3E0; color: #FF9500; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-purple { background: #F3E8FF; color: #8B5CF6; }
.tag-gray { background: #F2F3F5; color: #636e7b; }
.tag-red { background: #FFEBEE; color: #FF3B30; }

.countdown-tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.countdown-warning {
  background: #FFF3E0;
  color: #FF9500;
}
.countdown-expired {
  background: #FFEBEE;
  color: #FF3B30;
}
.countdown-normal {
  background: #E8EDFF;
  color: #4F6EF6;
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
