<script setup lang="ts">
import { formatMoney } from '@/utils/format'

// 模拟数据
const performance = {
  amount: 128000,
  target: 200000,
  rate: 64,
}

const actions = {
  followCount: 28,
  visitCount: 15,
  callDuration: 120, // 分钟
}

const customers = {
  newCount: 12,
  followCount: 45,
  lostCount: 3,
}
</script>

<template>
  <view class="page">
    <!-- 业绩卡片 -->
    <view class="card performance-card">
      <text class="card-title">本月业绩</text>
      <view class="amount-row">
        <text class="amount">{{ formatMoney(performance.amount) }}</text>
        <text class="target">/ 目标 {{ formatMoney(performance.target) }}</text>
      </view>
      <view class="progress-wrap">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: performance.rate + '%' }" />
        </view>
        <text class="progress-text">完成率 {{ performance.rate }}%</text>
      </view>
    </view>

    <!-- 行动力统计 -->
    <view class="card">
      <text class="card-title">行动力统计</text>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-num">{{ actions.followCount }}</text>
          <text class="stat-label">跟进次数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ actions.visitCount }}</text>
          <text class="stat-label">拜访次数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ actions.callDuration }}</text>
          <text class="stat-label">通话时长(分钟)</text>
        </view>
      </view>
    </view>

    <!-- 客户统计 -->
    <view class="card">
      <text class="card-title">客户统计</text>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-num highlight-green">{{ customers.newCount }}</text>
          <text class="stat-label">新增客户</text>
        </view>
        <view class="stat-item">
          <text class="stat-num highlight-blue">{{ customers.followCount }}</text>
          <text class="stat-label">跟进客户</text>
        </view>
        <view class="stat-item">
          <text class="stat-num highlight-red">{{ customers.lostCount }}</text>
          <text class="stat-label">流失客户</text>
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
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.performance-card {
  background: linear-gradient(135deg, #4F6EF6 0%, #6B8AFF 100%);
  color: #fff;
}

.card-title {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 20rpx;
  display: block;
}

.card:not(.performance-card) .card-title {
  color: #636e7b;
}

.amount-row {
  margin-bottom: 24rpx;
}

.amount {
  font-size: 48rpx;
  font-weight: 700;
}

.target {
  font-size: 26rpx;
  opacity: 0.9;
  margin-left: 8rpx;
}

.progress-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 16rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #fff;
  border-radius: 8rpx;
  transition: width 0.3s;
}

.progress-text {
  font-size: 26rpx;
  min-width: 120rpx;
}

.stats-grid {
  display: flex;
  gap: 24rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
}

.stat-num {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-label {
  font-size: 24rpx;
  color: #636e7b;
  margin-top: 8rpx;
}

.highlight-green { color: #34C759 !important; }
.highlight-blue { color: #4F6EF6 !important; }
.highlight-red { color: #FF3B30 !important; }
</style>
