<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { customerApi, followUpApi, opportunityApi, visitApi } from '@/api/index'
import { formatMoney, formatDate, customerStageMap } from '@/utils/format'
import dayjs from 'dayjs'

const id = ref(0)
const detail = ref<any>({})
const activeTab = ref<'follow' | 'opp' | 'visit'>('follow')
const followList = ref<any[]>([])
const oppList = ref<any[]>([])
const visitList = ref<any[]>([])
const loading = ref(true)

const summary = computed(() => ({
  followCount: detail.value.followCount ?? detail.value.followUpCount ?? 0,
  dealAmount: detail.value.dealAmount ?? detail.value.totalAmount ?? 0,
  oppCount: detail.value.oppCount ?? detail.value.opportunityCount ?? 0,
}))

const purchaseCountdown = computed(() => {
  const date = detail.value.expectedPurchaseDate
  if (!date) return null
  const end = dayjs(date)
  const now = dayjs()
  const days = end.diff(now, 'day')
  if (days < 0) return { text: '已过期', class: 'countdown-expired' }
  if (days <= 7) return { text: `${days}天后到期`, class: 'countdown-warning' }
  return { text: `${days}天后到期`, class: 'countdown-normal' }
})

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

onLoad((query: any) => {
  id.value = Number(query?.id || 0)
})

async function loadDetail() {
  if (!id.value) return
  loading.value = true
  try {
    const res = await customerApi.get(id.value) as any
    detail.value = res
  } catch {
    detail.value = {}
  } finally {
    loading.value = false
  }
}

async function loadTabData() {
  if (!id.value) return
  try {
    const [followRes, oppRes, visitRes] = await Promise.all([
      followUpApi.page({ pageNum: 1, pageSize: 5, customerId: id.value }).catch(() => ({ list: [], records: [] })),
      opportunityApi.page({ pageNum: 1, pageSize: 5, customerId: id.value }).catch(() => ({ list: [], records: [] })),
      visitApi.page({ pageNum: 1, pageSize: 5, customerId: id.value }).catch(() => ({ list: [], records: [] })),
    ])
    followList.value = (followRes as any)?.list ?? (followRes as any)?.records ?? []
    oppList.value = (oppRes as any)?.list ?? (oppRes as any)?.records ?? []
    visitList.value = (visitRes as any)?.list ?? (visitRes as any)?.records ?? []
  } catch {
    followList.value = []
    oppList.value = []
    visitList.value = []
  }
}

async function init() {
  await loadDetail()
  await loadTabData()
}

onMounted(() => init())

function callPhone() {
  const tel = detail.value.phone || detail.value.contactPhone
  if (tel) {
    uni.makePhoneCall({ phoneNumber: tel })
  } else {
    uni.showToast({ title: '暂无联系方式', icon: 'none' })
  }
}

function goTo(path: string) {
  uni.navigateTo({ url: `/pages/${path}` })
}

function goToFollowList() {
  uni.navigateTo({ url: `/pages/followUp/create?customerId=${id.value}` })
}

function goToVisitList() {
  uni.navigateTo({ url: `/pages/visit/list?customerId=${id.value}` })
}

function goToOppList() {
  uni.navigateTo({ url: `/pages/opportunity/list?customerId=${id.value}` })
}
</script>

<template>
  <view class="page">
    <view v-if="loading" class="loading-wrap">
      <text class="loading-text">加载中...</text>
    </view>

    <view v-else class="content">
      <!-- 顶部客户名称+生命周期 -->
      <view class="header-card">
        <view class="header-row">
          <text class="customer-name">{{ detail.name }}</text>
          <view :class="['tag', getStageTagClass(detail.lifecycleStage)]">
            {{ customerStageMap[detail.lifecycleStage] || '-' }}
          </view>
        </view>
      </view>

      <!-- 信息卡片 -->
      <view class="card">
        <view class="info-row">
          <text class="info-label">行业</text>
          <text class="info-value">{{ detail.industry || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">地区</text>
          <text class="info-value">{{ detail.region || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">地址</text>
          <text class="info-value">{{ detail.address || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">负责人</text>
          <text class="info-value">{{ detail.ownerName || detail.owner?.name || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">来源</text>
          <text class="info-value">{{ detail.sourceName || detail.source || '-' }}</text>
        </view>
      </view>

      <!-- 数据概览 -->
      <view class="summary-row">
        <view class="summary-item">
          <text class="summary-num">{{ summary.followCount }}</text>
          <text class="summary-label">跟进次数</text>
        </view>
        <view class="summary-item">
          <text class="summary-num">{{ formatMoney(summary.dealAmount) }}</text>
          <text class="summary-label">成交金额</text>
        </view>
        <view class="summary-item">
          <text class="summary-num">{{ summary.oppCount }}</text>
          <text class="summary-label">商机数</text>
        </view>
      </view>

      <!-- 快捷操作 -->
      <view class="quick-actions">
        <view class="quick-btn" @click="callPhone">
          <text class="quick-icon">📞</text>
          <text class="quick-label">拨打电话</text>
        </view>
        <view class="quick-btn" @click="goTo(`followUp/create?customerId=${id}`)">
          <text class="quick-icon">📝</text>
          <text class="quick-label">新建跟进</text>
        </view>
        <view class="quick-btn" @click="goTo(`visit/create?customerId=${id}`)">
          <text class="quick-icon">📋</text>
          <text class="quick-label">新建拜访</text>
        </view>
      </view>

      <!-- 采购倒计时 -->
      <view v-if="purchaseCountdown" class="card countdown-card">
        <view :class="['countdown-tag', purchaseCountdown.class]">
          {{ purchaseCountdown.text }}
        </view>
        <text class="countdown-desc">预计采购日期：{{ formatDate(detail.expectedPurchaseDate) }}</text>
      </view>

      <!-- Tab切换 -->
      <view class="tab-bar">
        <view
          :class="['tab-item', activeTab === 'follow' ? 'active' : '']"
          @click="activeTab = 'follow'"
        >
          跟进记录
        </view>
        <view
          :class="['tab-item', activeTab === 'opp' ? 'active' : '']"
          @click="activeTab = 'opp'"
        >
          商机
        </view>
        <view
          :class="['tab-item', activeTab === 'visit' ? 'active' : '']"
          @click="activeTab = 'visit'"
        >
          拜访记录
        </view>
      </view>

      <view class="tab-content">
        <!-- 跟进记录 -->
        <view v-show="activeTab === 'follow'" class="tab-panel">
          <view
            v-for="item in followList"
            :key="item.id"
            class="list-item"
            @click="goToFollowList"
          >
            <text class="item-title">{{ item.content || item.remark || '-' }}</text>
            <text class="item-meta">{{ item.createTime }}</text>
          </view>
          <view v-if="followList.length === 0" class="empty-text">暂无跟进记录</view>
          <view v-else class="more-link" @click="goToFollowList">查看全部 →</view>
        </view>

        <!-- 商机 -->
        <view v-show="activeTab === 'opp'" class="tab-panel">
          <view
            v-for="item in oppList"
            :key="item.id"
            class="list-item"
            @click="goTo(`opportunity/detail?id=${item.id}`)"
          >
            <text class="item-title">{{ item.name || '-' }}</text>
            <text class="item-meta">{{ formatMoney(item.amount) }} · {{ item.stageName }}</text>
          </view>
          <view v-if="oppList.length === 0" class="empty-text">暂无商机</view>
          <view v-else class="more-link" @click="goToOppList">查看全部 →</view>
        </view>

        <!-- 拜访记录 -->
        <view v-show="activeTab === 'visit'" class="tab-panel">
          <view
            v-for="item in visitList"
            :key="item.id"
            class="list-item"
            @click="goTo(`visit/detail?id=${item.id}`)"
          >
            <text class="item-title">{{ item.visitTypeName || '拜访' }}</text>
            <text class="item-meta">{{ item.planTime || item.visitTime }}</text>
          </view>
          <view v-if="visitList.length === 0" class="empty-text">暂无拜访记录</view>
          <view v-else class="more-link" @click="goToVisitList">查看全部 →</view>
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
  padding-bottom: 40rpx;
}

.loading-wrap {
  padding: 80rpx 0;
  text-align: center;
  .loading-text {
    color: #a0a8b4;
    font-size: 28rpx;
  }
}

.content {
  padding-bottom: 40rpx;
}

.header-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  .header-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
    .customer-name {
      font-size: 36rpx;
      font-weight: 600;
      color: #1a1a2e;
    }
  }
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.info-row {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #eef0f5;
  &:last-child {
    border-bottom: none;
  }
  .info-label {
    width: 140rpx;
    font-size: 28rpx;
    color: #636e7b;
  }
  .info-value {
    flex: 1;
    font-size: 28rpx;
    color: #1a1a2e;
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

.summary-row {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  .summary-item {
    flex: 1;
    text-align: center;
    .summary-num {
      display: block;
      font-size: 36rpx;
      font-weight: 600;
      color: #4F6EF6;
    }
    .summary-label {
      font-size: 24rpx;
      color: #636e7b;
      margin-top: 8rpx;
    }
  }
}

.quick-actions {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
  .quick-btn {
    flex: 1;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    text-align: center;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    .quick-icon {
      display: block;
      font-size: 40rpx;
      margin-bottom: 8rpx;
    }
    .quick-label {
      font-size: 24rpx;
      color: #636e7b;
    }
  }
}

.countdown-card {
  .countdown-tag {
    display: inline-block;
    padding: 8rpx 20rpx;
    border-radius: 8rpx;
    font-size: 26rpx;
    font-weight: 500;
    margin-bottom: 12rpx;
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
  .countdown-desc {
    display: block;
    font-size: 26rpx;
    color: #636e7b;
  }
}

.tab-bar {
  display: flex;
  background: #fff;
  border-radius: 16rpx 16rpx 0 0;
  padding: 0 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 20rpx 0;
    font-size: 28rpx;
    color: #636e7b;
  }
  .tab-item.active {
    color: #4F6EF6;
    font-weight: 600;
    border-bottom: 4rpx solid #4F6EF6;
  }
}

.tab-content {
  background: #fff;
  border-radius: 0 0 16rpx 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.tab-panel {
  min-height: 100rpx;
}

.list-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eef0f5;
  &:last-of-type {
    border-bottom: none;
  }
  .item-title {
    display: block;
    font-size: 28rpx;
    color: #1a1a2e;
  }
  .item-meta {
    font-size: 24rpx;
    color: #a0a8b4;
    margin-top: 4rpx;
  }
}

.empty-text {
  text-align: center;
  color: #a0a8b4;
  padding: 40rpx 0;
  font-size: 28rpx;
}

.more-link {
  text-align: center;
  color: #4F6EF6;
  font-size: 28rpx;
  padding: 20rpx 0 0;
}
</style>
