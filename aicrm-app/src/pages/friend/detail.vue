<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { friendApi } from '@/api/index'
import { formatDateTime, friendTypeMap } from '@/utils/format'

const id = ref(0)
const detail = ref<any>(null)
const loading = ref(true)
const customerIdInput = ref('')
const showLinkModal = ref(false)

const linkStatus = computed(() => {
  const d = detail.value
  if (!d) return 'unknown'
  if (d.customerId) return 'linked'
  if (d.leadId) return 'lead'
  return 'none'
})

const linkStatusText = computed(() => {
  if (linkStatus.value === 'linked') return '已关联客户'
  if (linkStatus.value === 'lead') return '已转线索'
  return '未关联'
})

onLoad((opt: any) => {
  id.value = Number(opt?.id) || 0
})

async function loadData() {
  if (!id.value) return
  loading.value = true
  try {
    const res = await friendApi.get(id.value)
    detail.value = res as any
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function callPhone() {
  const phone = detail.value?.phone || detail.value?.mobile
  if (!phone) {
    uni.showToast({ title: '暂无手机号', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: phone })
}

function openLinkModal() {
  customerIdInput.value = ''
  showLinkModal.value = true
}

function closeLinkModal() {
  showLinkModal.value = false
}

async function confirmLink() {
  const cid = customerIdInput.value?.trim()
  if (!cid) {
    uni.showToast({ title: '请输入客户ID', icon: 'none' })
    return
  }
  try {
    await friendApi.linkCustomer(id.value, { customerId: Number(cid) })
    uni.showToast({ title: '关联成功', icon: 'success' })
    closeLinkModal()
    loadData()
  } catch (e: any) {
    uni.showToast({ title: e?.message || '关联失败', icon: 'none' })
  }
}

async function convertToLead() {
  try {
    await friendApi.convertToLead(id.value)
    uni.showToast({ title: '已转为线索', icon: 'success' })
    loadData()
  } catch (e: any) {
    uni.showToast({ title: e?.message || '操作失败', icon: 'none' })
  }
}

function goToCustomer() {
  const cid = detail.value?.customerId
  if (!cid) return
  uni.navigateTo({ url: `/pages/customer/detail?id=${cid}` })
}

async function deleteFriend() {
  const res = await new Promise<boolean>((resolve) => {
    uni.showModal({
      title: '确认删除',
      content: '确定要删除该好友吗？',
      success: (r) => resolve(r.confirm),
    })
  })
  if (!res) return
  try {
    await friendApi.delete(id.value)
    uni.showToast({ title: '已删除', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '删除失败', icon: 'none' })
  }
}

function getTypeClass(type: number) {
  const map: Record<number, string> = {
    1: 'tag-blue',
    2: 'tag-green',
    3: 'tag-purple',
  }
  return map[type] ?? 'tag-gray'
}

onMounted(() => loadData())
</script>

<template>
  <view class="page">
    <view v-if="loading" class="loading-wrap">
      <text class="loading-text">加载中...</text>
    </view>

    <template v-else-if="detail">
      <!-- 信息卡片 -->
      <view class="info-card">
        <text class="friend-name">{{ detail.name || detail.nickname || '未命名' }}</text>
        <view v-if="detail.phone || detail.mobile" class="phone-row" @click="callPhone">
          <text class="phone-text">{{ detail.phone || detail.mobile }}</text>
          <text class="phone-hint">点击拨打</text>
        </view>
        <view class="info-row">
          <text class="info-label">公司</text>
          <text class="info-value">{{ detail.company || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">职位</text>
          <text class="info-value">{{ detail.position || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">好友类型</text>
          <view :class="['tag', getTypeClass(detail.friendType)]">
            {{ friendTypeMap[detail.friendType] ?? '-' }}
          </view>
        </view>
        <view class="info-row">
          <text class="info-label">来源</text>
          <text class="info-value">{{ detail.source || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">添加时间</text>
          <text class="info-value">{{ formatDateTime(detail.createTime) }}</text>
        </view>

        <!-- 关联状态 -->
        <view class="link-status">
          <text class="link-label">关联状态</text>
          <view v-if="linkStatus === 'linked'" class="link-value linked" @click="goToCustomer">
            {{ linkStatusText }} · {{ detail.customerName || '客户#' + detail.customerId }}
          </view>
          <view v-else-if="linkStatus === 'lead'" class="link-value lead">
            {{ linkStatusText }}
          </view>
          <view v-else class="link-value none">
            {{ linkStatusText }}
          </view>
        </view>

        <!-- 标签 -->
        <view v-if="detail.tags && detail.tags.length" class="tags-wrap">
          <text class="tags-label">标签</text>
          <view class="tags-list">
            <view v-for="(t, i) in (detail.tags || [])" :key="i" class="tag tag-gray">
              {{ typeof t === 'string' ? t : t?.name }}
            </view>
          </view>
        </view>
      </view>

      <!-- 底部操作按钮 -->
      <view class="bottom-bar">
        <view class="btn-row">
          <view v-if="linkStatus === 'none'" class="btn btn-primary" @click="openLinkModal">
            关联客户
          </view>
          <view v-if="linkStatus === 'none'" class="btn btn-primary" @click="convertToLead">
            转为线索
          </view>
          <view v-if="linkStatus === 'linked'" class="btn btn-primary" @click="goToCustomer">
            查看客户
          </view>
          <view class="btn btn-outline" @click="callPhone">拨打电话</view>
          <view class="btn btn-danger" @click="deleteFriend">删除好友</view>
        </view>
      </view>
    </template>

    <view v-else class="empty-wrap">
      <text class="empty-text">好友不存在</text>
    </view>

    <!-- 关联客户弹窗 -->
    <view v-if="showLinkModal" class="modal-mask" @click="closeLinkModal">
      <view class="modal-content" @click.stop>
        <text class="modal-title">关联客户</text>
        <input
          v-model="customerIdInput"
          class="modal-input"
          type="number"
          placeholder="请输入客户ID"
        />
        <view class="modal-actions">
          <view class="btn btn-outline" @click="closeLinkModal">取消</view>
          <view class="btn btn-primary" @click="confirmLink">确定</view>
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
  padding-bottom: 180rpx;
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

.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.friend-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #1a1a2e;
  display: block;
  margin-bottom: 24rpx;
}

.phone-row {
  margin-bottom: 20rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #eef0f5;
}

.phone-text {
  font-size: 32rpx;
  color: #4F6EF6;
  margin-right: 16rpx;
}

.phone-hint {
  font-size: 24rpx;
  color: #a0a8b4;
}

.info-row {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
  .info-label {
    width: 140rpx;
    font-size: 26rpx;
    color: #a0a8b4;
    flex-shrink: 0;
  }
  .info-value {
    font-size: 28rpx;
    color: #1a1a2e;
  }
}

.tag {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-purple { background: #F3E8FF; color: #8B5CF6; }
.tag-gray { background: #F2F3F5; color: #636e7b; }

.link-status {
  margin-top: 32rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #eef0f5;
}

.link-label {
  font-size: 26rpx;
  color: #a0a8b4;
  display: block;
  margin-bottom: 12rpx;
}

.link-value {
  font-size: 28rpx;
  &.linked {
    color: #4F6EF6;
    text-decoration: underline;
  }
  &.lead {
    color: #34C759;
  }
  &.none {
    color: #636e7b;
  }
}

.tags-wrap {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #eef0f5;
}

.tags-label {
  font-size: 26rpx;
  color: #a0a8b4;
  display: block;
  margin-bottom: 12rpx;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 30rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.btn-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  justify-content: center;
}

.btn {
  padding: 24rpx 40rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  text-align: center;
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
.btn-danger {
  background: #fff;
  color: #FF3B30;
  border: 1rpx solid #FF3B30;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  width: 600rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a2e;
  display: block;
  margin-bottom: 24rpx;
}

.modal-input {
  width: 100%;
  height: 80rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  margin-bottom: 32rpx;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  gap: 24rpx;
  justify-content: flex-end;
}
</style>
