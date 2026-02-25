<template>
  <div class="page opportunity-detail" v-loading="loading">
    <div class="page-header">
      <div class="title-row">
        <h1 class="page-title">{{ detail?.name || '商机详情' }}</h1>
        <el-tag v-if="detail?.status === 'ongoing'" type="primary">进行中</el-tag>
        <el-tag v-else-if="detail?.status === 'won'" type="success">赢单</el-tag>
        <el-tag v-else-if="detail?.status === 'lost'" type="danger">输单</el-tag>
      </div>
    </div>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="客户">{{ detail?.customerName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="阶段">{{ detail?.stageName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="预计金额">{{ formatAmount(detail?.expectedAmount) }}</el-descriptions-item>
      <el-descriptions-item label="实际金额">{{ formatAmount(detail?.actualAmount) }}</el-descriptions-item>
      <el-descriptions-item label="预计成交日期">{{ detail?.expectedCloseDate || '-' }}</el-descriptions-item>
      <el-descriptions-item label="赢率">
        <el-progress :percentage="detail?.winRate ?? 0" :color="'#4F6EF6'" :stroke-width="8" style="width: 120px; display: inline-block" />
      </el-descriptions-item>
      <el-descriptions-item label="竞争对手">{{ detail?.competitor || '-' }}</el-descriptions-item>
      <el-descriptions-item label="负责人">{{ detail?.ownerName || '-' }}</el-descriptions-item>
    </el-descriptions>

    <div class="actions" v-if="detail?.status === 'ongoing'">
      <el-button @click="showStageDialog = true">变更阶段</el-button>
      <el-button type="success" @click="showWinDialog = true">标记赢单</el-button>
      <el-button type="danger" @click="showLoseDialog = true">标记输单</el-button>
    </div>
    <el-button class="back-btn" @click="$router.push('/opportunities')">返回列表</el-button>

    <el-dialog v-model="showStageDialog" title="变更阶段" width="400px">
      <el-form :model="stageForm" label-width="80px">
        <el-form-item label="阶段">
          <el-select v-model="stageForm.stageId" placeholder="选择阶段" style="width: 100%">
            <el-option label="初步接触" :value="1" />
            <el-option label="需求确认" :value="2" />
            <el-option label="方案提案" :value="3" />
            <el-option label="报价" :value="4" />
            <el-option label="谈判" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stageForm.remark" type="textarea" :rows="3" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStageDialog = false">取消</el-button>
        <el-button type="primary" :loading="stageLoading" @click="handleChangeStage">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showWinDialog" title="标记赢单" width="400px">
      <el-form :model="winForm" label-width="100px">
        <el-form-item label="实际成交金额">
          <el-input v-model.number="winForm.actualAmount" placeholder="请输入实际成交金额" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWinDialog = false">取消</el-button>
        <el-button type="primary" :loading="winLoading" @click="handleWin">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showLoseDialog" title="标记输单" width="400px">
      <el-form :model="loseForm" label-width="80px">
        <el-form-item label="输单原因">
          <el-input v-model="loseForm.reason" type="textarea" :rows="3" placeholder="请输入输单原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoseDialog = false">取消</el-button>
        <el-button type="primary" :loading="loseLoading" @click="handleLose">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { opportunityApi } from '@/api/index'

const route = useRoute()
const id = computed(() => Number(route.params.id))
const loading = ref(false)
const detail = ref<any>(null)
const showStageDialog = ref(false)
const showWinDialog = ref(false)
const showLoseDialog = ref(false)
const stageLoading = ref(false)
const winLoading = ref(false)
const loseLoading = ref(false)
const stageForm = reactive({ stageId: undefined as number | undefined, remark: '' })
const winForm = reactive({ actualAmount: undefined as number | undefined })
const loseForm = reactive({ reason: '' })

const formatAmount = (v: number | undefined) => (v != null ? `¥${Number(v).toLocaleString()}` : '-')

const loadDetail = async () => {
  loading.value = true
  try {
    detail.value = await opportunityApi.get(id.value)
  } finally {
    loading.value = false
  }
}

const handleChangeStage = async () => {
  if (!stageForm.stageId) {
    ElMessage.warning('请选择阶段')
    return
  }
  stageLoading.value = true
  try {
    await opportunityApi.changeStage(id.value, { stageId: stageForm.stageId, remark: stageForm.remark })
    ElMessage.success('变更成功')
    showStageDialog.value = false
    stageForm.stageId = undefined
    stageForm.remark = ''
    loadDetail()
  } finally {
    stageLoading.value = false
  }
}

const handleWin = async () => {
  winLoading.value = true
  try {
    await opportunityApi.win(id.value, { actualAmount: winForm.actualAmount })
    ElMessage.success('已标记赢单')
    showWinDialog.value = false
    winForm.actualAmount = undefined
    loadDetail()
  } finally {
    winLoading.value = false
  }
}

const handleLose = async () => {
  loseLoading.value = true
  try {
    await opportunityApi.lose(id.value, { reason: loseForm.reason })
    ElMessage.success('已标记输单')
    showLoseDialog.value = false
    loseForm.reason = ''
    loadDetail()
  } finally {
    loseLoading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.page { background: #fff; border-radius: 8px; padding: 20px; }
.page-header { margin-bottom: 20px; }
.title-row { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 24px; font-weight: 600; color: #1a1a2e; margin: 0; }
.actions { margin-top: 20px; display: flex; gap: 12px; }
.back-btn { margin-top: 20px; }
:deep(.el-button--primary) { background: #4F6EF6; border-color: #4F6EF6; }
:deep(.el-button--primary:hover) { background: #3d5ce8; border-color: #3d5ce8; }
</style>
