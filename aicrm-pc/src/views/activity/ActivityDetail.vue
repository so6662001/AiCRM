<template>
  <div class="page" v-loading="loading">
    <el-button link type="primary" class="back-btn" @click="$router.push('/activities')">← 返回列表</el-button>

    <div v-if="activity" class="detail-header">
      <h1 class="title">{{ activity.name }}</h1>
      <div class="tags">
        <el-tag :type="activity.category === 'need_registration' ? 'primary' : 'success'" size="large">
          {{ activity.category === 'need_registration' ? '需要报名' : '不需要报名' }}
        </el-tag>
        <el-tag :type="statusTagMap[activity.status] || 'info'" size="large">{{ statusTextMap[activity.status] }}</el-tag>
      </div>
    </div>

    <el-descriptions v-if="activity" :column="2" border class="card">
      <el-descriptions-item label="活动类型">{{ activity.type }}</el-descriptions-item>
      <el-descriptions-item label="活动时间">{{ activity.startTime }} ~ {{ activity.endTime }}</el-descriptions-item>
      <el-descriptions-item label="活动地点">{{ activity.location || '-' }}</el-descriptions-item>
      <el-descriptions-item label="负责人">{{ activity.ownerName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="预算">{{ activity.budget ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="企微绑定">{{ activity.wecomBind ? '是' : '否' }}</el-descriptions-item>
    </el-descriptions>

    <div class="stat-row">
      <div v-for="(v, k) in statCards" :key="k" class="stat-card">
        <div class="value">{{ stats[k] ?? 0 }}</div>
        <div class="label">{{ v }}</div>
      </div>
    </div>

    <div class="card qr-card">
      <div class="qr-placeholder">
        <span>活动二维码</span>
      </div>
      <div class="qr-link">{{ qrLink }}</div>
      <div class="qr-actions">
        <el-button size="small" @click="copyLink">复制链接</el-button>
        <el-button size="small" @click="downloadQr">下载二维码</el-button>
      </div>
    </div>

    <div v-if="activity?.category === 'need_registration'" class="card">
      <h3 class="section-title">参与人管理</h3>
      <el-table :data="participants" v-loading="participantsLoading" stripe>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="company" label="公司" min-width="120" />
        <el-table-column prop="source" label="来源" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.source || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registrationStatus" label="报名状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.registrationStatus === 'approved' ? 'success' : 'info'" size="small">
              {{ row.registrationStatus === 'approved' ? '已通过' : row.registrationStatus === 'pending' ? '待审核' : row.registrationStatus || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkinStatus" label="签到状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.checkinStatus === 'checked' ? 'success' : 'info'" size="small">
              {{ row.checkinStatus === 'checked' ? '已签到' : '未签到' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.registrationStatus === 'pending'" link type="primary" @click="approve(row)">审核</el-button>
            <el-button v-if="row.registrationStatus === 'approved' && row.checkinStatus !== 'checked'" link type="primary" @click="checkin(row)">签到</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { activityApi } from '@/api'

const route = useRoute()
const id = computed(() => Number(route.params.id))
const loading = ref(false)
const activity = ref<any>(null)
const stats = ref<Record<string, number>>({})
const participants = ref<any[]>([])
const participantsLoading = ref(false)

const statusTagMap: Record<string, string> = {
  draft: 'info',
  not_started: 'primary',
  registration: 'success',
  ongoing: 'warning',
  ended: 'info',
  cancelled: 'danger',
}
const statusTextMap: Record<string, string> = {
  draft: '草稿',
  not_started: '未开始',
  registration: '报名中',
  ongoing: '进行中',
  ended: '已结束',
  cancelled: '已取消',
}

const statCards = computed(() => {
  if (!activity.value) return {}
  const needReg = activity.value.category === 'need_registration'
  return needReg
    ? { scanCount: '扫码量', registrationCount: '报名数', approvedCount: '审核通过', checkinCount: '签到数', friendCount: '加好友数' }
    : { scanCount: '扫码量', uvCount: '独立访客', friendCount: '加好友数', leadCount: '转线索数' }
})

const qrLink = computed(() => {
  return activity.value?.qrLink || `https://example.com/activity/${id.value}`
})

async function load() {
  if (!id.value) return
  loading.value = true
  try {
    const [act, stat] = await Promise.all([activityApi.get(id.value), activityApi.statistics(id.value)])
    activity.value = act as any
    stats.value = ((stat as unknown) as Record<string, number>) || {}
  } finally {
    loading.value = false
  }
}

async function loadParticipants() {
  if (!id.value || activity.value?.category !== 'need_registration') return
  participantsLoading.value = true
  try {
    const res = (await activityApi.participants(id.value)) as any
    participants.value = res?.records ?? res?.list ?? res ?? []
  } finally {
    participantsLoading.value = false
  }
}

async function approve(row: any) {
  try {
    await activityApi.approveParticipant(id.value!, row.id)
    ElMessage.success('审核通过')
    loadParticipants()
    load()
  } catch (e) {
    // error handled by interceptor
  }
}

async function checkin(row: any) {
  try {
    await activityApi.checkinParticipant(id.value!, row.id)
    ElMessage.success('签到成功')
    loadParticipants()
    load()
  } catch (e) {
    // error handled by interceptor
  }
}

async function copyLink() {
  try {
    await navigator.clipboard.writeText(qrLink.value)
    ElMessage.success('链接已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

function downloadQr() {
  ElMessage.info('下载二维码')
}

onMounted(() => {
  load().then(() => loadParticipants())
})
</script>

<style scoped>
.back-btn { margin-bottom: 16px; font-size: 14px; }
.detail-header { margin-bottom: 20px; }
.detail-header .title { font-size: 24px; font-weight: 600; margin-bottom: 12px; }
.detail-header .tags { display: flex; gap: 8px; }
.card { margin-bottom: 20px; }
.qr-card { text-align: center; padding: 24px; }
.qr-placeholder { width: 200px; height: 200px; margin: 0 auto 16px; border: 1px solid var(--border); display: flex; align-items: center; justify-content: center; color: var(--text-hint); font-size: 14px; }
.qr-link { font-size: 12px; color: var(--text-secondary); margin-bottom: 12px; word-break: break-all; }
.qr-actions { display: flex; gap: 8px; justify-content: center; }
.section-title { font-size: 16px; margin-bottom: 16px; }
</style>
