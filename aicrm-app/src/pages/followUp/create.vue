<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { followUpApi } from '@/api/index'

const relTypes = [
  { label: '线索', value: 'lead' },
  { label: '客户', value: 'customer' },
  { label: '商机', value: 'opportunity' },
]

const relTypeIndex = ref(0)
const relId = ref('')

onLoad((opt: any) => {
  const relType = opt?.relType
  const relIdParam = opt?.relId
  if (relType) {
    const idx = relTypes.findIndex((t) => t.value === relType)
    if (idx >= 0) relTypeIndex.value = idx
  }
  if (relIdParam) relId.value = String(relIdParam)
})
const followTypeIndex = ref(0)
const content = ref('')
const nextFollowTime = ref('')
const nextRemark = ref('')
const submitting = ref(false)

const followTypes = [
  { value: 1, label: '现场' },
  { value: 2, label: '电话' },
  { value: 3, label: '微信' },
  { value: 4, label: '邮件' },
  { value: 5, label: '企微' },
  { value: 6, label: '其他' },
]

function onRelTypeChange(e: any) {
  relTypeIndex.value = Number(e.detail.value)
}

function onFollowTypeChange(e: any) {
  followTypeIndex.value = Number(e.detail.value)
}

function onDateChange(e: any) {
  nextFollowTime.value = e.detail.value as string
}

async function submit() {
  const relType = relTypes[relTypeIndex.value]?.value
  const followType = followTypes[followTypeIndex.value]?.value

  if (!relType || !relId.value.trim()) {
    uni.showToast({ title: '请填写关联类型和关联ID', icon: 'none' })
    return
  }
  if (!content.value.trim()) {
    uni.showToast({ title: '请输入跟进内容', icon: 'none' })
    return
  }
  if (content.value.trim().length < 10) {
    uni.showToast({ title: '跟进内容至少10字', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const data: any = {
      relType,
      relId: relId.value.trim(),
      followType,
      content: content.value.trim(),
    }
    if (nextFollowTime.value) data.nextFollowTime = nextFollowTime.value
    if (nextRemark.value.trim()) data.nextRemark = nextRemark.value.trim()
    await followUpApi.create(data)
    uni.showToast({ title: '创建成功' })
    setTimeout(() => uni.navigateBack(), 1500)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="page">
    <view class="form">
      <view class="form-item">
        <text class="label">关联类型</text>
        <picker
          :range="relTypes"
          range-key="label"
          :value="relTypeIndex"
          @change="onRelTypeChange"
        >
          <view class="picker">{{ relTypes[relTypeIndex]?.label || '请选择' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">关联ID</text>
        <input v-model="relId" class="input" placeholder="输入关联ID" />
      </view>
      <view class="form-item">
        <text class="label">跟进方式</text>
        <picker
          :range="followTypes"
          range-key="label"
          :value="followTypeIndex"
          @change="onFollowTypeChange"
        >
          <view class="picker">{{ followTypes[followTypeIndex]?.label || '请选择' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">跟进内容 *</text>
        <textarea
          v-model="content"
          class="textarea"
          placeholder="至少10字"
          :maxlength="500"
        />
      </view>
      <view class="form-item">
        <text class="label">下次跟进时间</text>
        <picker mode="date" :value="nextFollowTime" @change="onDateChange">
          <view class="picker">{{ nextFollowTime || '选填' }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">下次跟进备注</text>
        <input v-model="nextRemark" class="input" placeholder="选填" />
      </view>

      <view class="submit-btn" :class="{ disabled: submitting }" @click="submit">
        {{ submitting ? '提交中...' : '提交' }}
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

.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
}

.form-item {
  margin-bottom: 28rpx;
  .label {
    display: block;
    font-size: 28rpx;
    color: #1a1a2e;
    margin-bottom: 12rpx;
  }
  .input, .picker, .textarea {
    width: 100%;
    height: 72rpx;
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    box-sizing: border-box;
    line-height: 72rpx;
  }
  .picker {
    color: #636e7b;
  }
  .textarea {
    height: 200rpx;
    padding: 20rpx;
    line-height: 1.5;
  }
}

.submit-btn {
  margin-top: 40rpx;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: #4F6EF6;
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
}
.submit-btn.disabled {
  opacity: 0.6;
}
</style>
