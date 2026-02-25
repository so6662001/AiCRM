<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { checkinApi } from '@/api/index'
import { formatDateTime } from '@/utils/format'

const currentTime = ref('')
const currentAddress = ref('深圳市南山区科技园')
const checkType = ref(1) // 1: 外出打卡 2: 客户签到
const remark = ref('')
const todayList = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)

let timer: ReturnType<typeof setInterval> | null = null

function updateTime() {
  const now = new Date()
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${h}:${m}:${s}`
}

async function loadToday() {
  loading.value = true
  try {
    const res = (await checkinApi.today()) as any
    todayList.value = res?.list ?? res ?? []
    if (!Array.isArray(todayList.value)) todayList.value = []
  } catch {
    todayList.value = []
  } finally {
    loading.value = false
  }
}

function getTypeName(type: number) {
  return type === 2 ? '客户签到' : '外出打卡'
}

async function doCheckin() {
  submitting.value = true
  try {
    await checkinApi.create({
      type: checkType.value,
      address: currentAddress.value,
      remark: remark.value.trim() || undefined,
    })
    uni.showToast({ title: '打卡成功' })
    remark.value = ''
    loadToday()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadToday()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

onPullDownRefresh(() => loadToday().then(() => uni.stopPullDownRefresh()))
</script>

<template>
  <view class="page">
    <view class="map-placeholder">
      <text class="map-icon">📍</text>
      <text class="map-text">地图加载中</text>
    </view>

    <view class="info-row">
      <text class="info-label">当前位置</text>
      <text class="info-value">{{ currentAddress }}</text>
    </view>

    <view class="info-row">
      <text class="info-label">当前时间</text>
      <text class="info-value time">{{ currentTime }}</text>
    </view>

    <view class="section">
      <text class="label">打卡类型</text>
      <view class="type-tabs">
        <view
          :class="['type-tab', checkType === 1 ? 'active' : '']"
          @click="checkType = 1"
        >
          外出打卡
        </view>
        <view
          :class="['type-tab', checkType === 2 ? 'active' : '']"
          @click="checkType = 2"
        >
          客户签到
        </view>
      </view>
    </view>

    <view class="section">
      <text class="label">备注</text>
      <input v-model="remark" class="input" placeholder="选填" />
    </view>

    <view
      class="checkin-btn"
      :class="{ disabled: submitting }"
      @click="doCheckin"
    >
      {{ submitting ? '打卡中...' : '签到打卡' }}
    </view>

    <view class="section list-section">
      <text class="section-title">今日打卡记录</text>
      <view v-if="todayList.length" class="record-list">
        <view
          v-for="item in todayList"
          :key="item.id"
          class="record-item"
        >
          <view class="record-main">
            <text class="record-time">{{ formatDateTime(item.createTime || item.checkinTime) }}</text>
            <text class="record-type">{{ getTypeName(item.type) }}</text>
          </view>
          <text class="record-addr">{{ item.address || '-' }}</text>
        </view>
      </view>
      <view v-else class="empty-text">暂无打卡记录</view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 30rpx;
}

.map-placeholder {
  height: 320rpx;
  background: #e8e9ed;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  .map-icon {
    font-size: 64rpx;
    margin-bottom: 12rpx;
  }
  .map-text {
    font-size: 28rpx;
    color: #636e7b;
  }
}

.info-row {
  background: #fff;
  padding: 24rpx 28rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  .info-label {
    font-size: 24rpx;
    color: #636e7b;
    display: block;
    margin-bottom: 8rpx;
  }
  .info-value {
    font-size: 28rpx;
    color: #1a1a2e;
    &.time {
      font-size: 36rpx;
      font-weight: 600;
      font-variant-numeric: tabular-nums;
    }
  }
}

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  .label {
    display: block;
    font-size: 28rpx;
    color: #1a1a2e;
    margin-bottom: 16rpx;
  }
  .input {
    width: 100%;
    height: 72rpx;
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    box-sizing: border-box;
  }
}

.type-tabs {
  display: flex;
  gap: 20rpx;
}

.type-tab {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: #f5f6fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #636e7b;
}
.type-tab.active {
  background: #E8EDFF;
  color: #4F6EF6;
}

.checkin-btn {
  height: 120rpx;
  line-height: 120rpx;
  text-align: center;
  background: linear-gradient(135deg, #4F6EF6 0%, #6B8AFF 100%);
  color: #fff;
  border-radius: 60rpx;
  font-size: 36rpx;
  font-weight: 600;
  margin: 40rpx 0;
  box-shadow: 0 8rpx 24rpx rgba(79, 110, 246, 0.35);
}
.checkin-btn.disabled {
  opacity: 0.7;
}

.list-section {
  .section-title {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: #1a1a2e;
    margin-bottom: 20rpx;
  }
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eef0f5;
  &:last-child { border-bottom: none; }
  .record-main {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 8rpx;
  }
  .record-time {
    font-size: 28rpx;
    color: #1a1a2e;
  }
  .record-type {
    font-size: 24rpx;
    padding: 4rpx 12rpx;
    background: #E8EDFF;
    color: #4F6EF6;
    border-radius: 8rpx;
  }
  .record-addr {
    font-size: 24rpx;
    color: #636e7b;
  }
}

.empty-text {
  text-align: center;
  color: #a0a8b4;
  padding: 40rpx 0;
  font-size: 28rpx;
}
</style>
