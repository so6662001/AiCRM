<script setup lang="ts">
import { ref } from 'vue'

const pushNotify = ref(true)
const autoRecord = ref(false)
const bgLocation = ref(false)

function clearCache() {
  uni.showToast({ title: '已清除', icon: 'success' })
}
</script>

<template>
  <view class="page">
    <view class="settings-list">
      <view class="setting-item">
        <text class="setting-label">消息推送通知</text>
        <switch :checked="pushNotify" color="#4F6EF6" @change="(e: any) => pushNotify = e.detail.value" />
      </view>
      <view class="setting-item">
        <text class="setting-label">自动录音</text>
        <switch :checked="autoRecord" color="#4F6EF6" @change="(e: any) => autoRecord = e.detail.value" />
      </view>
      <view class="setting-item">
        <text class="setting-label">后台定位</text>
        <switch :checked="bgLocation" color="#4F6EF6" @change="(e: any) => bgLocation = e.detail.value" />
      </view>
      <view class="setting-item clickable" @click="clearCache">
        <text class="setting-label">清除缓存</text>
        <text class="setting-action">点击清除</text>
      </view>
      <view class="setting-item">
        <text class="setting-label">当前版本</text>
        <text class="setting-value">v1.0.0</text>
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

.settings-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 28rpx;
  border-bottom: 1rpx solid #eef0f5;
  &:last-child {
    border-bottom: none;
  }
  &.clickable {
    cursor: pointer;
  }
}

.setting-label {
  font-size: 30rpx;
  color: #1a1a2e;
}

.setting-value, .setting-action {
  font-size: 28rpx;
  color: #636e7b;
}

.setting-action {
  color: #4F6EF6;
}
</style>
