<script setup lang="ts">
import { useUserStore } from '@/store/user'
import { computed } from 'vue'

const userStore = useUserStore()

const initial = computed(() => {
  const name = userStore.userName || '用'
  return name.charAt(0)
})

// 模拟数据
const stats = {
  followCount: 28,
  visitCount: 15,
  dealAmount: 128000,
}

function goTo(path: string) {
  uni.navigateTo({ url: `/pages/${path}` })
}

function about() {
  uni.showToast({ title: 'AiCRM v1.0.0', icon: 'none' })
}

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.clearStorageSync()
        userStore.setUser({ userId: 0, userName: '', tenantId: 0 })
        uni.reLaunch({ url: '/pages/home/index' })
      }
    },
  })
}
</script>

<template>
  <view class="page">
    <!-- 用户卡片 -->
    <view class="user-card">
      <view class="avatar-wrap">
        <view class="avatar">{{ initial }}</view>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userStore.userName }}</text>
        <text class="user-dept">{{ userStore.orgName }}</text>
        <text class="user-role">{{ userStore.roleName }}</text>
      </view>
    </view>

    <!-- 数据概览 -->
    <view class="stats-row">
      <view class="stat-card">
        <text class="stat-num">{{ stats.followCount }}</text>
        <text class="stat-label">本月跟进数</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ stats.visitCount }}</text>
        <text class="stat-label">本月拜访数</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ (stats.dealAmount / 10000).toFixed(1) }}万</text>
        <text class="stat-label">本月成交额</text>
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="goTo('report/personal')">
        <text class="menu-text">工作统计</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goTo('notification/list')">
        <text class="menu-text">消息中心</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goTo('mine/settings')">
        <text class="menu-text">设置</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="about">
        <text class="menu-text">关于</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item logout" @click="logout">
        <text class="menu-text">退出登录</text>
        <text class="menu-arrow">›</text>
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

.user-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.avatar-wrap {
  margin-right: 28rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #e0e4eb;
  color: #4F6EF6;
  font-size: 48rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  flex: 1;
}

.user-name {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8rpx;
}

.user-dept, .user-role {
  display: block;
  font-size: 26rpx;
  color: #636e7b;
  margin-top: 4rpx;
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
  padding: 28rpx;
  text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.stat-num {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #4F6EF6;
}

.stat-label {
  font-size: 24rpx;
  color: #636e7b;
  margin-top: 8rpx;
}

.menu-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 28rpx;
  border-bottom: 1rpx solid #eef0f5;
  &:last-child {
    border-bottom: none;
  }
  &.logout .menu-text {
    color: #FF3B30;
  }
}

.menu-text {
  font-size: 30rpx;
  color: #1a1a2e;
}

.menu-arrow {
  font-size: 36rpx;
  color: #a0a8b4;
  font-weight: 300;
}
</style>
