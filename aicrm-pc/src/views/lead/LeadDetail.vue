<template>
  <div class="lead-detail" v-loading="loading">
    <el-card v-if="lead" shadow="hover">
      <!-- 上方信息区 -->
      <div class="info-header">
        <div class="info-left">
          <h1 class="contact-name">{{ lead.contact }}</h1>
          <div class="company-name">{{ lead.company || '-' }}</div>
        </div>
        <div class="info-right">
          <el-tag :type="statusTagType(lead.status)">{{ statusText(lead.status) }}</el-tag>
          <el-tag :type="intentTagType(lead.intentLevel)" style="margin-left: 8px">{{ lead.intentLevel || '-' }}</el-tag>
        </div>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="手机">{{ lead.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ lead.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ lead.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ lead.industry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="省份">{{ lead.province || '-' }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ lead.city || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ lead.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分配时间">{{ lead.assignedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="跟进次数">{{ lead.followCount ?? '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="action-bar">
        <el-button type="primary" @click="showAssignDialog = true">分配</el-button>
        <el-button type="primary" @click="$router.push(`/follow-ups?leadId=${lead.id}`)">新建跟进</el-button>
        <el-button type="warning" @click="showReturnDialog = true">退回公海</el-button>
        <el-button @click="$router.push('/leads')">返回列表</el-button>
      </div>
    </el-card>

    <!-- 分配弹窗 -->
    <el-dialog v-model="showAssignDialog" title="分配线索" width="400px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="目标用户">
          <el-input v-model="assignForm.targetUserId" placeholder="请输入用户ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 退回公海弹窗 -->
    <el-dialog v-model="showReturnDialog" title="退回公海" width="400px">
      <el-form :model="returnForm" label-width="100px">
        <el-form-item label="退回原因">
          <el-input v-model="returnForm.reason" type="textarea" :rows="3" placeholder="请输入退回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReturnDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReturn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { leadApi } from '@/api/index'

const route = useRoute()
const id = computed(() => Number(route.params.id))
const lead = ref<any>(null)
const loading = ref(false)
const showAssignDialog = ref(false)
const showReturnDialog = ref(false)

const assignForm = reactive({ targetUserId: '' })
const returnForm = reactive({ reason: '' })

function intentTagType(level: string) {
  const map: Record<string, string> = { A: 'success', B: 'primary', C: 'warning', D: 'info' }
  return map[level] || 'info'
}

function statusTagType(status: string) {
  const map: Record<string, string> = { pending: 'info', assigned: 'warning', following: 'primary', pool: 'danger' }
  return map[status] || 'info'
}

function statusText(status: string) {
  const map: Record<string, string> = { pending: '待分配', assigned: '已分配', following: '跟进中', pool: '公海池' }
  return map[status] || status || '-'
}

async function loadData() {
  if (!id.value) return
  loading.value = true
  try {
    const res: any = await leadApi.get(id.value)
    lead.value = res?.data ?? res ?? null
  } catch (e) {
    lead.value = null
  } finally {
    loading.value = false
  }
}

async function handleAssign() {
  try {
    await leadApi.assign({ leadId: id.value, targetUserId: assignForm.targetUserId })
    showAssignDialog.value = false
    assignForm.targetUserId = ''
    loadData()
  } catch (e) {}
}

async function handleReturn() {
  try {
    await leadApi.returnToPool(id.value, { reason: returnForm.reason })
    showReturnDialog.value = false
    returnForm.reason = ''
    loadData()
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.lead-detail { padding: 0; }
.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}
.contact-name { font-size: 24px; font-weight: 600; color: #1a1a2e; margin: 0 0 8px 0; }
.company-name { font-size: 14px; color: #636e7b; }
.info-right { display: flex; align-items: center; }
.action-bar { margin-top: 24px; display: flex; gap: 12px; }
</style>
