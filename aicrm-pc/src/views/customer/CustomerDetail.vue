<template>
  <div class="customer-detail" v-loading="loading">
    <el-card v-if="customer" shadow="hover">
      <!-- 顶部 -->
      <div class="info-header">
        <h1 class="customer-name">{{ customer.name }}</h1>
        <div class="tags">
          <el-tag :type="lifecycleTagType(customer.lifecycle)">{{ lifecycleText(customer.lifecycle) }}</el-tag>
          <el-tag :type="levelTagType(customer.level)" style="margin-left: 8px">{{ customer.level || '-' }}</el-tag>
        </div>
      </div>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="客户编号">{{ customer.customerNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ customer.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ customer.industry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="规模">{{ customer.scale || '-' }}</el-descriptions-item>
        <el-descriptions-item label="省/市/区">{{ [customer.province, customer.city, customer.district].filter(Boolean).join(' / ') || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ customer.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ customer.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ customer.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ customer.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 采购倒计时卡片 -->
      <el-card v-if="customer.purchaseCountdown" class="countdown-card" shadow="never">
        <div :class="['countdown-text', customer.purchaseCountdown.expired ? 'expired' : 'ok']">
          {{ countdownText(customer.purchaseCountdown) }}
        </div>
      </el-card>

      <!-- 数据概览 -->
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-value">{{ customer.followCount ?? 0 }}</span>
          <span class="stat-label">跟进次数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ formatMoney(customer.dealAmount) }}</span>
          <span class="stat-label">成交金额</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ customer.dealCount ?? 0 }}</span>
          <span class="stat-label">成交次数</span>
        </div>
      </div>

      <!-- Tabs -->
      <el-tabs v-model="activeTab">
        <el-tab-pane label="跟进记录" name="follow">
          <el-timeline>
            <el-timeline-item v-for="(f, i) in followList" :key="i" :timestamp="f.time" placement="top">
              <div class="follow-content">{{ f.content }}</div>
              <div class="follow-meta">{{ f.creator }} · {{ f.method }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
        <el-tab-pane label="商机" name="opportunity">
          <el-table :data="opportunityList" stripe size="small">
            <el-table-column prop="name" label="商机名" />
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="stage" label="阶段" width="100" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="拜访记录" name="visit">
          <el-table :data="visitList" stripe size="small">
            <el-table-column prop="visitTime" label="拜访时间" width="160" />
            <el-table-column prop="type" label="类型" width="90" />
            <el-table-column prop="purpose" label="目的" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <div class="action-bar">
        <el-button type="primary" @click="$router.push(`/follow-ups?customerId=${customer.id}`)">新建跟进</el-button>
        <el-button type="primary" @click="$router.push(`/visits?customerId=${customer.id}`)">新建拜访</el-button>
        <el-button @click="$router.push('/customers')">返回列表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { customerApi } from '@/api/index'

const route = useRoute()
const id = computed(() => Number(route.params.id))
const customer = ref<any>(null)
const loading = ref(false)
const activeTab = ref('follow')

const followList = ref([
  { content: '电话沟通产品需求，客户表示下周安排会议', creator: '李明', time: '2025-02-24 10:30', method: '电话' },
  { content: '发送方案文档，等待客户反馈', creator: '李明', time: '2025-02-23 14:00', method: '邮件' },
  { content: '现场拜访，了解客户采购计划', creator: '李明', time: '2025-02-22 09:00', method: '拜访' },
])

const opportunityList = ref([
  { name: '年度采购项目', amount: 580000, stage: '方案报价', status: '进行中' },
  { name: '设备升级项目', amount: 120000, stage: '商务谈判', status: '进行中' },
])

const visitList = ref([
  { visitTime: '2025-02-24 10:00', type: '上门', purpose: '方案演示', status: '已完成' },
  { visitTime: '2025-02-20 14:00', type: '上门', purpose: '需求调研', status: '已完成' },
])

function lifecycleTagType(lc: string) {
  const map: Record<string, string> = {
    potential: 'info', intent: 'warning', deal: 'success', active: 'primary', vip: 'danger', churned: 'info',
  }
  return map[lc] || 'info'
}

function lifecycleText(lc: string) {
  const map: Record<string, string> = {
    potential: '潜在', intent: '意向', deal: '成交', active: '活跃', vip: 'VIP', churned: '流失',
  }
  return map[lc] || lc || '-'
}

function levelTagType(level: string) {
  const map: Record<string, string> = { A: 'success', B: 'primary', C: 'warning', D: 'info' }
  return map[level] || 'info'
}

function countdownText(cd: { days?: number; expired?: boolean }): string {
  if (cd.expired && cd.days != null) return `已过期${Math.abs(cd.days)}天`
  if (cd.days != null && cd.days >= 0) return `还剩${cd.days}天`
  return '-'
}

function formatMoney(val: number | string | null | undefined): string {
  if (val == null || val === '') return '-'
  const n = Number(val)
  if (isNaN(n)) return '-'
  return '¥' + n.toLocaleString()
}

async function loadData() {
  if (!id.value) return
  loading.value = true
  try {
    const res: any = await customerApi.get(id.value)
    customer.value = res?.data ?? res ?? null
  } catch (e) {
    customer.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.customer-detail { padding: 0; }
.info-header { margin-bottom: 24px; padding-bottom: 20px; border-bottom: 1px solid #ebeef5; }
.customer-name { font-size: 24px; font-weight: 600; color: #1a1a2e; margin: 0 0 12px 0; }
.tags { margin-top: 8px; }
.countdown-card { margin: 20px 0; background: #f5f7fa; }
.countdown-text.ok { color: #67c23a; font-size: 16px; font-weight: 600; }
.countdown-text.expired { color: #f56c6c; font-size: 16px; font-weight: 600; }
.stat-row {
  display: flex;
  gap: 40px;
  margin: 24px 0;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}
.stat-item { display: flex; flex-direction: column; gap: 4px; }
.stat-value { font-size: 22px; font-weight: 700; color: #4F6EF6; }
.stat-label { font-size: 13px; color: #636e7b; }
.follow-content { font-size: 14px; color: #1a1a2e; margin-bottom: 4px; }
.follow-meta { font-size: 12px; color: #636e7b; }
.action-bar { margin-top: 24px; display: flex; gap: 12px; }
</style>
