<template>
  <div class="sales-report">
    <div class="page-header">
      <h2 class="page-title">销售数据报表</h2>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="个人报表" name="personal">
        <div class="period-bar">
          <span>选择月份：</span>
          <el-date-picker
            v-model="period"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            @change="loadPersonal"
          />
        </div>
        <div v-if="personalData" class="stat-cards">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">成交额</div>
            <div class="stat-value">{{ formatMoney(personalData.dealAmount) }}</div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">完成率</div>
            <div class="stat-value">{{ (personalData.completionRate ?? 0).toFixed(1) }}%</div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">新增客户</div>
            <div class="stat-value">{{ personalData.newCustomers ?? 0 }}</div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">跟进次数</div>
            <div class="stat-value">{{ personalData.totalFollowUps ?? 0 }}</div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">拜访次数</div>
            <div class="stat-value">{{ personalData.totalVisits ?? 0 }}</div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">通话时长(分钟)</div>
            <div class="stat-value">{{ personalData.totalCallDuration ?? 0 }}</div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">活跃商机</div>
            <div class="stat-value">{{ personalData.activeOpportunities ?? 0 }}</div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">赢单数</div>
            <div class="stat-value">{{ personalData.wonCount ?? 0 }}</div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">违规数</div>
            <div class="stat-value">{{ personalData.violationCount ?? 0 }}</div>
          </el-card>
        </div>
        <div v-else-if="personalLoading" class="loading-wrap">加载中...</div>
        <div v-else class="empty-wrap">请选择月份查看数据</div>
      </el-tab-pane>

      <el-tab-pane label="团队报表" name="team">
        <div class="period-bar">
          <span>选择月份：</span>
          <el-date-picker
            v-model="teamPeriod"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            @change="loadTeam"
          />
        </div>
        <el-card shadow="hover">
          <el-table v-loading="teamLoading" :data="teamList" stripe>
            <el-table-column prop="userName" label="姓名" width="120" />
            <el-table-column prop="followUps" label="跟进数" width="100" />
            <el-table-column prop="visits" label="拜访数" width="100" />
            <el-table-column prop="callDuration" label="通话时长(分钟)" width="140" />
            <el-table-column prop="newCustomers" label="新增客户" width="100" />
            <el-table-column prop="dealAmount" label="成交额" min-width="120">
              <template #default="{ row }">{{ formatMoney(row.dealAmount) }}</template>
            </el-table-column>
            <el-table-column prop="overdueLeads" label="逾期线索" width="100" />
          </el-table>
          <div v-if="!teamLoading && teamList.length === 0" class="empty-table">暂无数据</div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { reportApi } from '@/api/index'
import dayjs from 'dayjs'

const activeTab = ref('personal')
const period = ref(dayjs().format('YYYY-MM'))
const teamPeriod = ref(dayjs().format('YYYY-MM'))
const personalData = ref<any>(null)
const personalLoading = ref(false)
const teamList = ref<any[]>([])
const teamLoading = ref(false)

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return '¥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

async function loadPersonal() {
  if (!period.value) return
  personalLoading.value = true
  personalData.value = null
  try {
    personalData.value = await reportApi.personal(period.value)
  } catch {
    personalData.value = null
  } finally {
    personalLoading.value = false
  }
}

async function loadTeam() {
  if (!teamPeriod.value) return
  teamLoading.value = true
  teamList.value = []
  try {
    teamList.value = (await reportApi.team(teamPeriod.value)) as unknown as any[]
  } catch {
    teamList.value = []
  } finally {
    teamLoading.value = false
  }
}

onMounted(() => {
  loadPersonal()
  loadTeam()
})
</script>

<style scoped>
.sales-report { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; margin: 0; }
.period-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.stat-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
.stat-card { text-align: center; }
.stat-label { font-size: 14px; color: #636e7b; margin-bottom: 8px; }
.stat-value { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.loading-wrap, .empty-wrap { text-align: center; padding: 40px 0; color: #636e7b; }
.empty-table { text-align: center; padding: 40px 0; color: #a0a8b4; }
</style>
