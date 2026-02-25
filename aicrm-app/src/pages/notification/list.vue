<script setup lang="ts">
import { ref } from 'vue'
import { formatDateTime } from '@/utils/format'

const typeMap: Record<number, string> = {
  1: '任务提醒',
  2: '线索分配',
  3: '审核通知',
  4: '违规警报',
  5: '系统通知',
}

const list = ref([
  { id: 1, title: '您有新的任务待处理', time: new Date().toISOString(), type: 1, read: false },
  { id: 2, title: '线索「张先生」已分配给您', time: new Date(Date.now() - 3600000).toISOString(), type: 2, read: false },
  { id: 3, title: '拜访计划已通过审核', time: new Date(Date.now() - 7200000).toISOString(), type: 3, read: true },
  { id: 4, title: '请及时完成外勤打卡', time: new Date(Date.now() - 86400000).toISOString(), type: 4, read: false },
  { id: 5, title: '系统将于今晚22:00进行维护', time: new Date(Date.now() - 172800000).toISOString(), type: 5, read: true },
])
</script>

<template>
  <view class="page">
    <view class="list">
      <view
        v-for="item in list"
        :key="item.id"
        class="list-item"
      >
        <view class="dot-area">
          <view v-if="!item.read" class="unread-dot" />
        </view>
        <view class="item-content">
          <text class="item-title">{{ item.title }}</text>
          <view class="item-meta">
            <text class="item-type">{{ typeMap[item.type] || '通知' }}</text>
            <text class="item-time">{{ formatDateTime(item.time) }}</text>
          </view>
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

.list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.list-item {
  display: flex;
  align-items: flex-start;
  padding: 28rpx;
  border-bottom: 1rpx solid #eef0f5;
  position: relative;
  &:last-child {
    border-bottom: none;
  }
}

.dot-area {
  width: 36rpx;
  flex-shrink: 0;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #4F6EF6;
  margin-top: 12rpx;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #1a1a2e;
  display: block;
  margin-bottom: 12rpx;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.item-type {
  font-size: 24rpx;
  color: #4F6EF6;
  background: #E8EDFF;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.item-time {
  font-size: 24rpx;
  color: #a0a8b4;
}
</style>
