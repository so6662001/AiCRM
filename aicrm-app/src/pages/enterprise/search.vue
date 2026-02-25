<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { enterpriseApi } from '@/api/index'

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const resultList = ref<any[]>([])
const quota = ref<any>(null)
const expandedCreditCode = ref<string | null>(null)
const contactsMap = ref<Record<string, any[]>>({})
const contactsLoadingMap = ref<Record<string, boolean>>({})

function reliabilityLabel(r: number) {
  const map: Record<number, string> = { 1: '高', 2: '中', 3: '低' }
  return map[r] ?? '-'
}

function reliabilityTagClass(r: number) {
  const map: Record<number, string> = { 1: 'tag-green', 2: 'tag-orange', 3: 'tag-gray' }
  return map[r] ?? 'tag-gray'
}

async function loadQuota() {
  try {
    quota.value = await enterpriseApi.getQuota()
  } catch {
    quota.value = null
  }
}

async function onSearch() {
  if (!keyword.value?.trim()) return
  loading.value = true
  searched.value = true
  try {
    resultList.value = await enterpriseApi.search(keyword.value.trim())
    expandedCreditCode.value = null
    contactsMap.value = {}
    loadQuota()
  } catch {
    resultList.value = []
  } finally {
    loading.value = false
  }
}

async function toggleContacts(ent: any) {
  const code = ent.creditCode
  if (!code) return
  if (expandedCreditCode.value === code) {
    expandedCreditCode.value = null
    return
  }
  expandedCreditCode.value = code
  if (contactsMap.value[code]) return
  contactsLoadingMap.value[code] = true
  try {
    const list = await enterpriseApi.queryContacts(code)
    contactsMap.value = { ...contactsMap.value, [code]: list }
  } catch {
    contactsMap.value = { ...contactsMap.value, [code]: [] }
  } finally {
    contactsLoadingMap.value[code] = false
  }
}

function getContacts(code: string) {
  return contactsMap.value[code] ?? []
}

function isContactsLoading(code: string) {
  return contactsLoadingMap.value[code] ?? false
}

function onCall(phone: string) {
  if (phone) {
    uni.makePhoneCall({ phoneNumber: phone })
  }
}

onMounted(() => loadQuota())
</script>

<template>
  <view class="page">
    <!-- 顶部搜索栏 + 配额 -->
    <view class="header">
      <view class="search-row">
        <input
          v-model="keyword"
          class="search-input"
          placeholder="输入企业名称或信用代码"
          type="text"
          confirm-type="search"
          @confirm="onSearch"
        />
        <view class="search-btn" @click="onSearch">搜索</view>
      </view>
      <view v-if="quota" class="quota">
        今日剩余 {{ quota.dailyRemaining ?? '-' }} · 本月剩余 {{ quota.monthlyRemaining ?? '-' }}
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading-wrap">搜索中...</view>

    <!-- 空状态 -->
    <view v-else-if="searched && resultList.length === 0" class="empty-wrap">暂无搜索结果</view>

    <!-- 搜索结果卡片 -->
    <view v-else class="list-wrap">
      <view
        v-for="ent in resultList"
        :key="ent.creditCode || ent.companyName"
        class="enterprise-card"
      >
        <view class="card-header">
          <text class="company-name">{{ ent.companyName || '-' }}</text>
          <view :class="['tag', ent.isPlatformCustomer ? 'tag-green' : 'tag-gray']">
            {{ ent.isPlatformCustomer ? '已是平台客户' : '非平台客户' }}
          </view>
        </view>
        <view class="card-meta">
          <text class="credit-code">{{ ent.creditCode || '-' }}</text>
        </view>
        <view class="card-info">
          <text class="info-line">法人 {{ ent.legalPerson || '-' }} · 注册资本 {{ ent.registeredCapital || '-' }}</text>
        </view>
        <view class="card-tags">
          <view v-if="ent.companyStatus" class="tag tag-status">{{ ent.companyStatus }}</view>
          <text class="contact-count">联系人数 {{ ent.contactCacheCount ?? 0 }}</text>
        </view>
        <view class="card-footer">
          <view class="btn-query" @click="toggleContacts(ent)">查询联系方式</view>
        </view>

        <!-- 展开的联系方式列表 -->
        <view v-if="expandedCreditCode === ent.creditCode" class="contacts-panel">
          <view v-if="isContactsLoading(ent.creditCode)" class="contacts-loading">加载中...</view>
          <view v-else class="contacts-list">
            <view
              v-for="c in getContacts(ent.creditCode)"
              :key="c.id"
              class="contact-item"
            >
              <view class="contact-main">
                <text class="contact-name">{{ c.contactName || '-' }}</text>
                <text class="contact-position">{{ c.position || '-' }}</text>
                <text class="contact-phone">{{ c.phone || '-' }}</text>
              </view>
              <view class="contact-actions">
                <view
                  v-if="c.phoneFull || c.phone"
                  class="btn-call"
                  @click="onCall(c.phoneFull || c.phone)"
                >
                  拨打
                </view>
                <view :class="['tag', reliabilityTagClass(c.reliability)]">
                  {{ c.reliabilityLabel || reliabilityLabel(c.reliability) }}
                </view>
              </view>
            </view>
            <view v-if="getContacts(ent.creditCode).length === 0 && !isContactsLoading(ent.creditCode)" class="contacts-empty">
              暂无联系方式
            </view>
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

.header {
  margin-bottom: 24rpx;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 16rpx;

  .search-input {
    flex: 1;
    height: 72rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
  }

  .search-btn {
    width: 120rpx;
    height: 72rpx;
    line-height: 72rpx;
    text-align: center;
    background: #4F6EF6;
    color: #fff;
    border-radius: 16rpx;
    font-size: 28rpx;
  }
}

.quota {
  font-size: 24rpx;
  color: #636e7b;
  text-align: right;
}

.loading-wrap,
.empty-wrap {
  text-align: center;
  padding: 80rpx 0;
  color: #636e7b;
  font-size: 28rpx;
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.enterprise-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;

  .company-name {
    font-size: 36rpx;
    font-weight: 600;
    color: #1a1a2e;
    flex: 1;
  }
}

.card-meta {
  margin-bottom: 12rpx;

  .credit-code {
    font-size: 26rpx;
    color: #636e7b;
  }
}

.card-info {
  margin-bottom: 12rpx;

  .info-line {
    font-size: 26rpx;
    color: #636e7b;
  }
}

.card-tags {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;

  .contact-count {
    font-size: 24rpx;
    color: #a0a8b4;
  }
}

.card-footer {
  padding-top: 20rpx;
  border-top: 1rpx solid #eef0f5;
}

.btn-query {
  width: 100%;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  background: #4F6EF6;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}

.tag-green {
  background: #E8F8EE;
  color: #34C759;
}

.tag-gray {
  background: #F2F3F5;
  color: #636e7b;
}

.tag-orange {
  background: #FFF3E0;
  color: #FF9500;
}

.tag-status {
  background: #E8EDFF;
  color: #4F6EF6;
}

.contacts-panel {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #eef0f5;
}

.contacts-loading {
  text-align: center;
  padding: 40rpx 0;
  color: #636e7b;
  font-size: 26rpx;
}

.contacts-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.contact-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.contact-main {
  flex: 1;

  .contact-name {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: #1a1a2e;
    margin-bottom: 8rpx;
  }

  .contact-position,
  .contact-phone {
    display: block;
    font-size: 24rpx;
    color: #636e7b;
    margin-top: 4rpx;
  }
}

.contact-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.btn-call {
  padding: 12rpx 24rpx;
  background: #4F6EF6;
  color: #fff;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.contacts-empty {
  text-align: center;
  padding: 40rpx 0;
  color: #a0a8b4;
  font-size: 26rpx;
}
</style>
