<script setup lang="ts">
import { ref, computed } from 'vue'
import { visitApi } from '@/api/index'
import { visitTypeMap } from '@/utils/format'

const visitTypes = [
  { value: 1, label: '现场', icon: '📍' },
  { value: 2, label: '电话', icon: '📞' },
  { value: 3, label: '微信', icon: '💬' },
  { value: 4, label: '企微', icon: '🏢' },
  { value: 5, label: '视频', icon: '🖥️' },
]

const selectedType = ref<number | null>(null)
const customerName = ref('')
const purpose = ref('')
const visitTime = ref('')
const remark = ref('')
const phone = ref('')
const submitting = ref(false)

const showPhoneField = computed(() => selectedType.value === 2)

function selectType(val: number) {
  selectedType.value = val
}

async function submit() {
  if (!selectedType.value) {
    uni.showToast({ title: '请选择拜访方式', icon: 'none' })
    return
  }
  if (!customerName.value.trim()) {
    uni.showToast({ title: '请输入客户名称', icon: 'none' })
    return
  }
  if (!purpose.value.trim()) {
    uni.showToast({ title: '请输入拜访目的', icon: 'none' })
    return
  }
  if (!visitTime) {
    uni.showToast({ title: '请选择拜访时间', icon: 'none' })
    return
  }
  if (selectedType.value === 2 && !phone.value.trim()) {
    uni.showToast({ title: '请输入联系电话', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const data: any = {
      visitType: selectedType.value,
      customerName: customerName.value.trim(),
      purpose: purpose.value.trim(),
      planTime: visitTime,
      remark: remark.value.trim() || undefined,
    }
    if (selectedType.value === 2) data.phone = phone.value.trim()
    await visitApi.create(data)
    uni.showToast({ title: '创建成功' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch {
    // toast handled by api
  } finally {
    submitting.value = false
  }
}

function onDateChange(e: any) {
  visitTime.value = e.detail.value as string
}
</script>

<template>
  <view class="page">
    <view class="section">
      <text class="label">拜访方式 *</text>
      <view class="type-grid">
        <view
          v-for="t in visitTypes"
          :key="t.value"
          :class="['type-btn', selectedType === t.value ? 'active' : '']"
          @click="selectType(t.value)"
        >
          <text class="type-icon">{{ t.icon }}</text>
          <text class="type-label">{{ t.label }}</text>
        </view>
      </view>
    </view>

    <view v-if="selectedType" class="form">
      <view class="form-item">
        <text class="label">客户名称 *</text>
        <input v-model="customerName" class="input" placeholder="输入客户名称" />
      </view>
      <view class="form-item">
        <text class="label">拜访目的 *</text>
        <input v-model="purpose" class="input" placeholder="请输入拜访目的" />
      </view>
      <view class="form-item">
        <text class="label">拜访时间 *</text>
        <picker mode="datetime" :value="visitTime" @change="onDateChange">
          <view class="picker">{{ visitTime || '请选择时间' }}</view>
        </picker>
      </view>
      <view v-if="showPhoneField" class="form-item">
        <text class="label">联系电话 *</text>
        <input v-model="phone" class="input" type="number" placeholder="请输入联系电话" />
      </view>
      <view class="form-item">
        <text class="label">备注</text>
        <textarea v-model="remark" class="textarea" placeholder="选填" />
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

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  .label {
    display: block;
    font-size: 28rpx;
    color: #1a1a2e;
    margin-bottom: 20rpx;
  }
}

.type-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.type-btn {
  width: calc(20% - 16rpx);
  min-width: 100rpx;
  aspect-ratio: 1;
  background: #f5f6fa;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .type-icon {
    font-size: 40rpx;
    margin-bottom: 8rpx;
  }
  .type-label {
    font-size: 24rpx;
    color: #636e7b;
  }
}
.type-btn.active {
  background: #E8EDFF;
  border: 2rpx solid #4F6EF6;
  .type-label { color: #4F6EF6; }
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
  }
  .picker {
    line-height: 72rpx;
    color: #636e7b;
  }
  .textarea {
    height: 160rpx;
    padding: 20rpx;
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
