<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { visitApi } from '@/api/index'
import { formatDateTime, visitTypeMap, visitStatusMap } from '@/utils/format'

const id = ref(0)
const detail = ref<any>(null)
const loading = ref(false)
const showCompleteModal = ref(false)
const visitResult = ref('')
const completing = ref(false)

const isCompleted = computed(() => detail.value?.status === 2)
const canComplete = computed(() => detail.value?.status === 0 || detail.value?.status === 1)

async function loadDetail() {
  if (!id.value) return
  loading.value = true
  try {
    detail.value = await visitApi.get(id.value) as any
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function openCompleteModal() {
  visitResult.value = ''
  showCompleteModal.value = true
}

function closeCompleteModal() {
  showCompleteModal.value = false
}

async function doComplete() {
  if (!visitResult.value.trim()) {
    uni.showToast({ title: '请输入拜访结果', icon: 'none' })
    return
  }
  completing.value = true
  try {
    await visitApi.complete(id.value, { visitResult: visitResult.value.trim() })
    uni.showToast({ title: '完成成功' })
    closeCompleteModal()
    loadDetail()
  } finally {
    completing.value = false
  }
}

onLoad((opt: any) => {
  id.value = Number(opt?.id) || 0
  loadDetail()
})
</script>

<template>
  <view class="page">
    <view v-if="loading" class="loading-wrap">加载中...</view>
    <view v-else-if="detail" class="content">
      <view class="card">
        <view class="row">
          <text class="label">客户</text>
          <text class="value">{{ detail.customerName || detail.customer?.name || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">拜访方式</text>
          <view class="tag tag-type">{{ visitTypeMap[detail.visitType] ?? '-' }}</view>
        </view>
        <view class="row">
          <text class="label">拜访目的</text>
          <text class="value">{{ detail.purpose || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">拜访时间</text>
          <text class="value">{{ formatDateTime(detail.planTime || detail.visitTime) }}</text>
        </view>
        <view class="row">
          <text class="label">状态</text>
          <view :class="['tag', isCompleted ? 'tag-green' : 'tag-blue']">
            {{ visitStatusMap[detail.status] ?? '-' }}
          </view>
        </view>
        <view v-if="isCompleted && detail.visitResult" class="row full">
          <text class="label">拜访结果</text>
          <text class="value block">{{ detail.visitResult }}</text>
        </view>
      </view>

      <view v-if="canComplete" class="footer">
        <view class="btn-primary" @click="openCompleteModal">完成拜访</view>
      </view>
    </view>
    <view v-else class="empty-text">数据加载失败</view>

    <!-- 完成拜访弹窗 -->
    <view v-if="showCompleteModal" class="modal-mask" @click="closeCompleteModal">
      <view class="modal" @click.stop>
        <text class="modal-title">拜访结果</text>
        <textarea
          v-model="visitResult"
          class="modal-input"
          placeholder="请输入拜访结果"
          :maxlength="500"
        />
        <view class="modal-actions">
          <view class="btn-cancel" @click="closeCompleteModal">取消</view>
          <view class="btn-confirm" :class="{ disabled: completing }" @click="doComplete">
            {{ completing ? '提交中...' : '确定' }}
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
  padding-bottom: 140rpx;
}

.loading-wrap, .empty-text {
  text-align: center;
  padding: 80rpx 0;
  color: #a0a8b4;
  font-size: 28rpx;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.row {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eef0f5;
  &:last-child { border-bottom: none; }
  &.full { flex-direction: column; align-items: flex-start; }
  .label {
    width: 160rpx;
    font-size: 28rpx;
    color: #636e7b;
    flex-shrink: 0;
  }
  .value {
    flex: 1;
    font-size: 28rpx;
    color: #1a1a2e;
    &.block { margin-top: 12rpx; }
  }
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}
.tag-type { background: #E8EDFF; color: #4F6EF6; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-blue { background: #E8EDFF; color: #4F6EF6; }

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 30rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
}

.btn-primary {
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: #4F6EF6;
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 100;
}

.modal {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 30rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
}

.modal-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 24rpx;
}

.modal-input {
  width: 100%;
  height: 200rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  margin-bottom: 32rpx;
}

.modal-actions {
  display: flex;
  gap: 24rpx;
  .btn-cancel, .btn-confirm {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    border-radius: 16rpx;
    font-size: 32rpx;
  }
  .btn-cancel {
    background: #f5f6fa;
    color: #636e7b;
  }
  .btn-confirm {
    background: #4F6EF6;
    color: #fff;
    &.disabled { opacity: 0.6; }
  }
}
</style>
