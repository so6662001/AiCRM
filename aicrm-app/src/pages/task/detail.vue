<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { taskApi } from '@/api/index'
import { formatDateTime, priorityMap, priorityColorMap, taskStatusMap } from '@/utils/format'

const id = ref(0)
const task = ref<any>({})
const loading = ref(true)

onLoad((opt: any) => {
  id.value = Number(opt?.id || 0)
})

async function loadDetail() {
  if (!id.value) return
  loading.value = true
  try {
    const res = await taskApi.get(id.value).catch(() => null)
    task.value = res || {}
  } catch {
    task.value = {}
  } finally {
    loading.value = false
  }
}

function updateProgress() {
  uni.showModal({
    title: '更新进度',
    editable: true,
    placeholderText: '请输入完成进度(0-100)',
    success: (res) => {
      if (res.confirm && res.content) {
        const progress = parseInt(res.content, 10)
        if (progress >= 0 && progress <= 100) {
          taskApi.update(id.value, { progress }).then(() => {
            uni.showToast({ title: '更新成功' })
            loadDetail()
          }).catch(() => uni.showToast({ title: '更新失败', icon: 'none' }))
        } else {
          uni.showToast({ title: '请输入0-100的数字', icon: 'none' })
        }
      }
    },
  })
}

function markComplete() {
  uni.showModal({
    title: '标记完成',
    editable: true,
    placeholderText: '请输入完成说明（选填）',
    success: (res) => {
      if (res.confirm) {
        taskApi.complete(id.value, { completionNote: res.content || '' }).then(() => {
          uni.showToast({ title: '已完成' })
          loadDetail()
        }).catch(() => uni.showToast({ title: '操作失败', icon: 'none' }))
      }
    },
  })
}

onMounted(() => loadDetail())
</script>

<template>
  <view class="page">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else class="content">
      <text class="task-title">{{ task.title || '-' }}</text>
      <view class="tags-row">
        <view
          class="priority-tag"
          :style="{ backgroundColor: priorityColorMap[task.priority] || '#8E8E93', color: '#fff' }"
        >
          {{ priorityMap[task.priority] ?? '未知' }}
        </view>
        <text class="status-tag" :class="task.status === 2 ? 'done' : task.status === 4 ? 'overdue' : 'pending'">
          {{ taskStatusMap[task.status] ?? '未知' }}
        </text>
      </view>
      <view class="section">
        <text class="label">任务内容</text>
        <text class="value">{{ task.content || '无' }}</text>
      </view>
      <view class="section">
        <text class="label">计划时间</text>
        <text class="value">{{ formatDateTime(task.planStartTime) }} ~ {{ formatDateTime(task.planEndTime) }}</text>
      </view>
      <view class="section">
        <text class="label">来源</text>
        <text class="value">{{ task.assignType === 1 ? '自主' : '上级安排' }}{{ task.assignerName ? ` · ${task.assignerName}` : '' }}</text>
      </view>
      <view class="section">
        <text class="label">完成进度</text>
        <view class="progress-wrap">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: (task.progress ?? 0) + '%' }" />
          </view>
          <text class="progress-text">{{ task.progress ?? 0 }}%</text>
        </view>
      </view>
      <view v-if="task.completionNote" class="section">
        <text class="label">完成说明</text>
        <text class="value">{{ task.completionNote }}</text>
      </view>
    </view>

    <view v-if="!loading && task.status !== 2" class="footer-actions">
      <view class="btn btn-outline" @click="updateProgress">更新进度</view>
      <view class="btn btn-primary" @click="markComplete">标记完成</view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 30rpx;
  padding-bottom: 140rpx;
}

.loading {
  text-align: center;
  padding: 80rpx 0;
  color: #636e7b;
}

.content {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.task-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #1a1a2e;
  display: block;
  margin-bottom: 20rpx;
}

.tags-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.priority-tag {
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.status-tag {
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  &.done { background: #E8F8EE; color: #34C759; }
  &.overdue { background: #FFEBEE; color: #FF3B30; }
  &.pending { background: #E8EDFF; color: #4F6EF6; }
}

.section {
  margin-bottom: 28rpx;
  .label {
    display: block;
    font-size: 26rpx;
    color: #636e7b;
    margin-bottom: 8rpx;
  }
  .value {
    font-size: 30rpx;
    color: #1a1a2e;
    line-height: 1.6;
  }
}

.progress-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 16rpx;
  background: #eef0f5;
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #4F6EF6;
  border-radius: 8rpx;
  transition: width 0.3s;
}

.progress-text {
  font-size: 26rpx;
  color: #636e7b;
  min-width: 60rpx;
}

.footer-actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 30rpx;
  padding-bottom: calc(env(safe-area-inset-bottom) + 24rpx);
  background: #fff;
  display: flex;
  gap: 24rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.btn {
  flex: 1;
  padding: 24rpx 0;
  text-align: center;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 16rpx;
}

.btn-primary {
  background: #4F6EF6;
  color: #fff;
}

.btn-outline {
  background: #fff;
  color: #4F6EF6;
  border: 2rpx solid #4F6EF6;
}
</style>
