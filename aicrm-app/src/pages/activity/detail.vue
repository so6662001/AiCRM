<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { activityApi } from '@/api/index'
import { formatDateTime, activityStatusMap } from '@/utils/format'

const id = ref(0)
const detail = ref<any>(null)
const statistics = ref<any>(null)
const loading = ref(true)

const needSignup = computed(() => detail.value?.needSignup === 1)

const statCards = computed(() => {
  const stats = statistics.value
  if (!stats) return []
  if (needSignup.value) {
    return [
      { label: '报名数', value: stats.signupCount ?? 0 },
      { label: '签到数', value: stats.checkinCount ?? 0 },
      { label: '加好友数', value: stats.friendCount ?? 0 },
    ]
  }
  return [
    { label: '扫码量', value: stats.scanCount ?? 0 },
    { label: '独立访客', value: stats.uvCount ?? 0 },
    { label: '加好友数', value: stats.friendCount ?? 0 },
  ]
})

const qrLink = computed(() => {
  if (!detail.value?.qrCodeUrl) return ''
  return detail.value.qrCodeUrl
})

onLoad((opt: any) => {
  id.value = Number(opt?.id) || 0
})

async function loadData() {
  if (!id.value) return
  loading.value = true
  try {
    const [resDetail, resStat] = await Promise.all([
      activityApi.get(id.value),
      activityApi.statistics(id.value),
    ])
    detail.value = resDetail as any
    statistics.value = resStat as any
  } catch {
    detail.value = null
    statistics.value = null
  } finally {
    loading.value = false
  }
}

function saveToAlbum() {
  uni.showToast({ title: '已保存到相册', icon: 'success' })
}

function onShare() {
  uni.showToast({ title: '已分享', icon: 'success' })
}

function onParticipantClick() {
  uni.showToast({ title: '参与人列表暂未实现', icon: 'none' })
}

function getCategoryClass() {
  return needSignup.value ? 'tag-blue' : 'tag-green'
}

function getStatusClass() {
  const status = detail.value?.status ?? 0
  const map: Record<number, string> = { 0: 'tag-gray', 1: 'tag-blue', 2: 'tag-green', 3: 'tag-gray' }
  return map[status] ?? 'tag-gray'
}

onMounted(() => loadData())
</script>

<template>
  <view class="page">
    <view v-if="loading" class="loading-wrap">
      <text class="loading-text">加载中...</text>
    </view>

    <template v-else-if="detail">
      <!-- 上半部分 -->
      <view class="header">
        <text class="activity-name">{{ detail.name }}</text>
        <view class="tags">
          <view :class="['tag', getCategoryClass()]">
            {{ needSignup ? '需要报名' : '不需要报名' }}
          </view>
          <view :class="['tag', getStatusClass()]">
            {{ activityStatusMap[detail.status] ?? '-' }}
          </view>
        </view>
        <view class="info-row">
          <text class="info-label">活动时间</text>
          <text class="info-value">{{ formatDateTime(detail.startTime) }} - {{ formatDateTime(detail.endTime) }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">地点</text>
          <text class="info-value">{{ detail.location || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">负责人</text>
          <text class="info-value">{{ detail.ownerName || detail.owner?.name || '-' }}</text>
        </view>
      </view>

      <!-- 数据概览 -->
      <view class="section">
        <text class="section-title">数据概览</text>
        <view class="stat-cards">
          <view v-for="(s, i) in statCards" :key="i" class="stat-card">
            <text class="stat-value">{{ s.value }}</text>
            <text class="stat-label">{{ s.label }}</text>
          </view>
        </view>
      </view>

      <!-- 二维码区域 -->
      <view class="section">
        <text class="section-title">活动二维码</text>
        <view class="qr-wrap">
          <view class="qr-placeholder">
            <text class="qr-placeholder-text">活动二维码</text>
          </view>
          <text v-if="qrLink" class="qr-link">{{ qrLink }}</text>
          <view class="qr-actions">
            <view class="btn btn-primary" @click="saveToAlbum">保存到相册</view>
            <view class="btn btn-outline" @click="onShare">分享</view>
          </view>
        </view>
      </view>

      <!-- 参与人列表入口 -->
      <view v-if="needSignup" class="section">
        <view class="participant-entry" @click="onParticipantClick">
          <text class="participant-label">参与人列表</text>
          <text class="participant-count">共 {{ statistics?.signupCount ?? 0 }} 人</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </template>

    <view v-else class="empty-wrap">
      <text class="empty-text">活动不存在</text>
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

.loading-wrap,
.empty-wrap {
  padding: 80rpx 0;
  text-align: center;
}

.loading-text,
.empty-text {
  font-size: 28rpx;
  color: #a0a8b4;
}

.header {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.activity-name {
  font-size: 38rpx;
  font-weight: 700;
  color: #1a1a2e;
  display: block;
  margin-bottom: 20rpx;
}

.tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.tag {
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-gray { background: #F2F3F5; color: #636e7b; }

.info-row {
  margin-top: 16rpx;
  .info-label {
    font-size: 26rpx;
    color: #a0a8b4;
    margin-right: 16rpx;
  }
  .info-value {
    font-size: 28rpx;
    color: #1a1a2e;
  }
}

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a2e;
  display: block;
  margin-bottom: 24rpx;
}

.stat-cards {
  display: flex;
  gap: 20rpx;
}

.stat-card {
  flex: 1;
  background: #f5f6fa;
  border-radius: 12rpx;
  padding: 24rpx;
  text-align: center;
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

.qr-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-placeholder {
  width: 320rpx;
  height: 320rpx;
  background: #e8e8ed;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.qr-placeholder-text {
  font-size: 28rpx;
  color: #8e8e93;
}

.qr-link {
  font-size: 24rpx;
  color: #636e7b;
  margin-bottom: 24rpx;
  word-break: break-all;
  text-align: center;
}

.qr-actions {
  display: flex;
  gap: 24rpx;
}

.btn {
  padding: 20rpx 48rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
}
.btn-primary {
  background: #4F6EF6;
  color: #fff;
}
.btn-outline {
  background: #fff;
  color: #4F6EF6;
  border: 1rpx solid #4F6EF6;
}

.participant-entry {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-top: 1rpx solid #eef0f5;
  margin-top: -16rpx;
}

.participant-label {
  font-size: 28rpx;
  color: #1a1a2e;
  flex: 1;
}

.participant-count {
  font-size: 26rpx;
  color: #636e7b;
  margin-right: 8rpx;
}

.arrow {
  font-size: 32rpx;
  color: #a0a8b4;
}
</style>
