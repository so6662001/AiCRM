<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { opportunityApi } from '@/api/index'
import { formatDate, formatMoney, oppStageMap, oppStatusMap } from '@/utils/format'

const id = ref(0)
const detail = ref<any>(null)
const loading = ref(false)
const showWinModal = ref(false)
const showLoseModal = ref(false)
const showStageModal = ref(false)
const actualAmount = ref('')
const lossReason = ref('')
const stageIndex = ref(0)
const submitting = ref(false)

const stages = [
  { value: 1, label: '初步接触' },
  { value: 2, label: '需求确认' },
  { value: 3, label: '方案报价' },
  { value: 4, label: '商务谈判' },
  { value: 5, label: '赢单' },
  { value: 6, label: '输单' },
]

const canChangeStage = ref(true)
const canWin = ref(true)
const canLose = ref(true)

async function loadDetail() {
  if (!id.value) return
  loading.value = true
  try {
    detail.value = (await opportunityApi.get(id.value)) as any
    if (detail.value) {
      const status = detail.value.status
      canChangeStage.value = status === 1
      canWin.value = status === 1
      canLose.value = status === 1
      const s = detail.value.stage
      stageIndex.value = stages.findIndex((x) => x.value === s)
      if (stageIndex.value < 0) stageIndex.value = 0
    }
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function openStageModal() {
  showStageModal.value = true
}

function onStageChange(e: any) {
  stageIndex.value = Number(e.detail.value)
}

async function doChangeStage() {
  const stage = stages[stageIndex.value]?.value
  if (!stage) return
  submitting.value = true
  try {
    await opportunityApi.changeStage(id.value, { stage })
    uni.showToast({ title: '变更成功' })
    showStageModal.value = false
    loadDetail()
  } finally {
    submitting.value = false
  }
}

function openWinModal() {
  actualAmount.value = String(detail.value?.expectedAmount ?? '')
  showWinModal.value = true
}

async function doWin() {
  const amount = parseFloat(actualAmount.value)
  if (isNaN(amount) || amount < 0) {
    uni.showToast({ title: '请输入有效金额', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await opportunityApi.win(id.value, { actualAmount: amount })
    uni.showToast({ title: '已标记赢单' })
    showWinModal.value = false
    loadDetail()
  } finally {
    submitting.value = false
  }
}

function openLoseModal() {
  lossReason.value = ''
  showLoseModal.value = true
}

async function doLose() {
  if (!lossReason.value.trim()) {
    uni.showToast({ title: '请输入输单原因', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await opportunityApi.lose(id.value, { lossReason: lossReason.value.trim() })
    uni.showToast({ title: '已标记输单' })
    showLoseModal.value = false
    loadDetail()
  } finally {
    submitting.value = false
  }
}

function closeModal() {
  showWinModal.value = false
  showLoseModal.value = false
  showStageModal.value = false
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
          <text class="label">商机名称</text>
          <text class="value">{{ detail.name || detail.opportunityName || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">客户</text>
          <text class="value">{{ detail.customerName || detail.customer?.name || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">阶段</text>
          <view class="tag tag-stage">{{ oppStageMap[detail.stage] ?? '-' }}</view>
        </view>
        <view class="row">
          <text class="label">预计金额</text>
          <text class="value amount">{{ formatMoney(detail.expectedAmount || detail.amount) }}</text>
        </view>
        <view class="row">
          <text class="label">预计成交日期</text>
          <text class="value">{{ formatDate(detail.expectedCloseDate) }}</text>
        </view>
        <view v-if="detail.winRate != null" class="row full">
          <text class="label">赢率</text>
          <view class="progress-wrap">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: detail.winRate + '%' }" />
            </view>
            <text class="progress-text">{{ detail.winRate }}%</text>
          </view>
        </view>
        <view class="row">
          <text class="label">竞争对手</text>
          <text class="value">{{ detail.competitor || '-' }}</text>
        </view>
        <view v-if="detail.remark" class="row full">
          <text class="label">备注</text>
          <text class="value block">{{ detail.remark }}</text>
        </view>
      </view>

      <view v-if="canChangeStage || canWin || canLose" class="footer">
        <view v-if="canChangeStage" class="btn-outline" @click="openStageModal">变更阶段</view>
        <view v-if="canWin" class="btn-outline" @click="openWinModal">标记赢单</view>
        <view v-if="canLose" class="btn-primary" @click="openLoseModal">标记输单</view>
      </view>
    </view>
    <view v-else class="empty-text">数据加载失败</view>

    <!-- 变更阶段 -->
    <view v-if="showStageModal" class="modal-mask" @click="closeModal">
      <view class="modal" @click.stop>
        <text class="modal-title">变更阶段</text>
        <picker
          :range="stages"
          range-key="label"
          :value="stageIndex"
          @change="onStageChange"
        >
          <view class="picker">{{ stages[stageIndex]?.label || '请选择' }}</view>
        </picker>
        <view class="modal-actions">
          <view class="btn-cancel" @click="closeModal">取消</view>
          <view class="btn-confirm" :class="{ disabled: submitting }" @click="doChangeStage">
            {{ submitting ? '提交中...' : '确定' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 标记赢单 -->
    <view v-if="showWinModal" class="modal-mask" @click="closeModal">
      <view class="modal" @click.stop>
        <text class="modal-title">实际成交金额</text>
        <input
          v-model="actualAmount"
          class="modal-input"
          type="digit"
          placeholder="请输入金额"
        />
        <view class="modal-actions">
          <view class="btn-cancel" @click="closeModal">取消</view>
          <view class="btn-confirm" :class="{ disabled: submitting }" @click="doWin">
            {{ submitting ? '提交中...' : '确定' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 标记输单 -->
    <view v-if="showLoseModal" class="modal-mask" @click="closeModal">
      <view class="modal" @click.stop>
        <text class="modal-title">输单原因</text>
        <textarea
          v-model="lossReason"
          class="modal-textarea"
          placeholder="请输入输单原因"
          :maxlength="200"
        />
        <view class="modal-actions">
          <view class="btn-cancel" @click="closeModal">取消</view>
          <view class="btn-confirm" :class="{ disabled: submitting }" @click="doLose">
            {{ submitting ? '提交中...' : '确定' }}
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
    width: 180rpx;
    font-size: 28rpx;
    color: #636e7b;
    flex-shrink: 0;
  }
  .value {
    flex: 1;
    font-size: 28rpx;
    color: #1a1a2e;
    &.amount { font-size: 32rpx; font-weight: 600; color: #4F6EF6; }
    &.block { margin-top: 12rpx; }
  }
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}
.tag-stage { background: #E8EDFF; color: #4F6EF6; }

.progress-wrap {
  flex: 1;
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
  background: linear-gradient(90deg, #4F6EF6, #6B8AFF);
  border-radius: 8rpx;
  transition: width 0.3s;
}
.progress-text {
  font-size: 26rpx;
  color: #636e7b;
  min-width: 60rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 30rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
  display: flex;
  gap: 20rpx;
}

.btn-outline {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border: 2rpx solid #4F6EF6;
  color: #4F6EF6;
  border-radius: 16rpx;
  font-size: 28rpx;
}

.btn-primary {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: #4F6EF6;
  color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
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

.picker {
  height: 72rpx;
  line-height: 72rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  margin-bottom: 32rpx;
}

.modal-input {
  width: 100%;
  height: 72rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  margin-bottom: 32rpx;
}

.modal-textarea {
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
