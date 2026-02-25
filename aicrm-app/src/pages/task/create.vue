<script setup lang="ts">
import { ref } from 'vue'
import { taskApi } from '@/api/index'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

const form = ref({
  title: '',
  content: '',
  taskType: 1,
  priority: 2,
  planStartTime: '',
  planEndTime: '',
  remindTime: '',
})

const taskTypeOptions = [
  { value: 1, label: '客户拜访' },
  { value: 2, label: '电话跟进' },
  { value: 3, label: '商机推进' },
  { value: 4, label: '资料整理' },
  { value: 5, label: '其他' },
]

const priorityOptions = [
  { value: 1, label: '低' },
  { value: 2, label: '中' },
  { value: 3, label: '高' },
  { value: 4, label: '紧急' },
]

const taskTypeIndex = ref(0)
const priorityIndex = ref(1)
const showStartPicker = ref(false)
const showEndPicker = ref(false)
const showRemindPicker = ref(false)
const submitting = ref(false)

const defaultDate = dayjs().format('YYYY-MM-DD HH:mm')

function onTaskTypeChange(e: any) {
  const i = Number(e.detail?.value ?? 0)
  taskTypeIndex.value = i
  form.value.taskType = taskTypeOptions[i]?.value ?? 1
}

function onPriorityChange(e: any) {
  const i = Number(e.detail?.value ?? 0)
  priorityIndex.value = i
  form.value.priority = priorityOptions[i]?.value ?? 2
}

function onStartTimeChange(e: any) {
  form.value.planStartTime = e.detail?.value || ''
}

function onEndTimeChange(e: any) {
  form.value.planEndTime = e.detail?.value || ''
}

function onRemindTimeChange(e: any) {
  form.value.remindTime = e.detail?.value || ''
}

async function submit() {
  if (!form.value.title?.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await taskApi.create({
      title: form.value.title.trim(),
      content: form.value.content || '',
      taskType: form.value.taskType,
      priority: form.value.priority,
      planStartTime: form.value.planStartTime || undefined,
      planEndTime: form.value.planEndTime || undefined,
      remindTime: form.value.remindTime || undefined,
      assignType: 1,
      assigneeUserId: userStore.userId,
    })
    uni.showToast({ title: '创建成功' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch {
    uni.showToast({ title: '创建失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="page">
    <view class="form">
      <view class="form-item required">
        <text class="label">任务标题</text>
        <input
          v-model="form.title"
          class="input"
          placeholder="请输入任务标题"
          placeholder-class="placeholder"
        />
      </view>
      <view class="form-item">
        <text class="label">任务内容</text>
        <textarea
          v-model="form.content"
          class="textarea"
          placeholder="请输入任务内容（选填）"
          placeholder-class="placeholder"
        />
      </view>
      <view class="form-item">
        <text class="label">任务类型</text>
        <picker
          :value="taskTypeIndex"
          :range="taskTypeOptions"
          range-key="label"
          @change="onTaskTypeChange"
        >
          <view class="picker-value">
            {{ taskTypeOptions[taskTypeIndex]?.label ?? '请选择' }}
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">优先级</text>
        <picker
          :value="priorityIndex"
          :range="priorityOptions"
          range-key="label"
          @change="onPriorityChange"
        >
          <view class="picker-value">
            {{ priorityOptions[priorityIndex]?.label ?? '请选择' }}
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">计划开始时间</text>
        <picker
          mode="date"
          :value="form.planStartTime ? form.planStartTime.split(' ')[0] : ''"
          @change="(e: any) => form.planStartTime = (e.detail?.value || '') + ' 09:00'"
        >
          <view class="picker-value">
            {{ form.planStartTime ? form.planStartTime.split(' ')[0] : '请选择' }}
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">计划结束时间</text>
        <picker
          mode="date"
          :value="form.planEndTime ? form.planEndTime.split(' ')[0] : ''"
          @change="(e: any) => form.planEndTime = (e.detail?.value || '') + ' 18:00'"
        >
          <view class="picker-value">
            {{ form.planEndTime ? form.planEndTime.split(' ')[0] : '请选择' }}
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">提醒时间（可选）</text>
        <picker
          mode="date"
          :value="form.remindTime ? form.remindTime.split(' ')[0] : ''"
          @change="(e: any) => form.remindTime = (e.detail?.value || '') + ' 09:00'"
        >
          <view class="picker-value">
            {{ form.remindTime ? form.remindTime.split(' ')[0] : '不提醒' }}
          </view>
        </picker>
      </view>
    </view>

    <view class="submit-btn" :class="{ disabled: submitting }" @click="submit">
      {{ submitting ? '提交中...' : '创建任务' }}
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

.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 0 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.form-item {
  padding: 28rpx 0;
  border-bottom: 1rpx solid #eef0f5;
  &:last-child {
    border-bottom: none;
  }
  &.required .label::before {
    content: '* ';
    color: #FF3B30;
  }
}

.label {
  display: block;
  font-size: 28rpx;
  color: #636e7b;
  margin-bottom: 16rpx;
}

.input {
  font-size: 30rpx;
  color: #1a1a2e;
  padding: 0;
}

.textarea {
  font-size: 30rpx;
  color: #1a1a2e;
  min-height: 160rpx;
  padding: 0;
}

.placeholder {
  color: #a0a8b4;
}

.picker-value {
  font-size: 30rpx;
  color: #1a1a2e;
}

.submit-btn {
  margin-top: 40rpx;
  padding: 28rpx 0;
  background: #4F6EF6;
  color: #fff;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 16rpx;
  &.disabled {
    opacity: 0.6;
  }
}
</style>
