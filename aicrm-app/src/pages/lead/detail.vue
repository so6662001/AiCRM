<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { leadApi } from '@/api/index'
import { formatDateTime, leadStatusMap, leadIntentionMap } from '@/utils/format'

const id = ref(0)
const detail = ref<any>(null)
const loading = ref(false)
const showReturnModal = ref(false)
const returnReason = ref('')
const returning = ref(false)

function getIntentionClass(intention: string) {
  const map: Record<string, string> = {
    A: 'tag-green',
    B: 'tag-blue',
    C: 'tag-orange',
    D: 'tag-gray',
  }
  return map[intention] ?? 'tag-gray'
}

async function loadDetail() {
  if (!id.value) return
  loading.value = true
  try {
    detail.value = (await leadApi.get(id.value)) as any
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function callPhone() {
  const phone = detail.value?.phone || detail.value?.mobile
  if (!phone) {
    uni.showToast({ title: '暂无电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: phone })
}

function goToFollowUp() {
  uni.navigateTo({ url: `/pages/followUp/create?relType=lead&relId=${id.value}` })
}

function openReturnModal() {
  returnReason.value = ''
  showReturnModal.value = true
}

function closeReturnModal() {
  showReturnModal.value = false
}

async function doReturn() {
  if (!returnReason.value.trim()) {
    uni.showToast({ title: '请输入退回原因', icon: 'none' })
    return
  }
  returning.value = true
  try {
    await leadApi.returnToPool(id.value, { reason: returnReason.value.trim() })
    uni.showToast({ title: '已退回公海池' })
    closeReturnModal()
    loadDetail()
  } finally {
    returning.value = false
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
          <text class="label">联系人</text>
          <text class="value">{{ detail.contactName || detail.name || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">电话</text>
          <text class="value link" @click="callPhone">{{ detail.phone || detail.mobile || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">邮箱</text>
          <text class="value">{{ detail.email || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">公司</text>
          <text class="value">{{ detail.companyName || detail.company || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">职位</text>
          <text class="value">{{ detail.position || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">来源</text>
          <text class="value">{{ detail.sourceName || detail.source || '-' }}</text>
        </view>
        <view class="row">
          <text class="label">意向等级</text>
          <view :class="['tag', getIntentionClass(detail.intentionLevel)]">
            {{ leadIntentionMap[detail.intentionLevel] || detail.intentionLevel || '-' }}
          </view>
        </view>
        <view class="row">
          <text class="label">状态</text>
          <view class="tag tag-status">{{ leadStatusMap[detail.status] ?? '-' }}</view>
        </view>
        <view class="row">
          <text class="label">跟进次数</text>
          <text class="value">{{ detail.followCount ?? 0 }} 次</text>
        </view>
        <view class="row">
          <text class="label">最后跟进</text>
          <text class="value">{{ formatDateTime(detail.lastFollowTime) }}</text>
        </view>
      </view>

      <view class="footer">
        <view class="btn-outline" @click="callPhone">拨打电话</view>
        <view class="btn-outline" @click="goToFollowUp">新建跟进</view>
        <view class="btn-primary" @click="openReturnModal">退回公海池</view>
      </view>
    </view>
    <view v-else class="empty-text">数据加载失败</view>

    <view v-if="showReturnModal" class="modal-mask" @click="closeReturnModal">
      <view class="modal" @click.stop>
        <text class="modal-title">退回原因</text>
        <textarea
          v-model="returnReason"
          class="modal-input"
          placeholder="请输入退回原因"
          :maxlength="200"
        />
        <view class="modal-actions">
          <view class="btn-cancel" @click="closeReturnModal">取消</view>
          <view class="btn-confirm" :class="{ disabled: returning }" @click="doReturn">
            {{ returning ? '提交中...' : '确定' }}
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
    &.link { color: #4F6EF6; }
  }
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}
.tag-status { background: #F2F3F5; color: #636e7b; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-orange { background: #FFF3E0; color: #FF9500; }
.tag-gray { background: #F2F3F5; color: #636e7b; }

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
