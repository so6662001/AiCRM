<template>
  <div class="customer-blacklist">
    <div class="page-header">
      <h2 class="page-title">客户黑名单</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加黑名单
      </el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
        <el-option label="全部" value="" />
        <el-option label="生效中" :value="1" />
        <el-option label="已解除" :value="2" />
      </el-select>
      <el-select v-model="typeFilter" placeholder="类型" clearable style="width: 120px">
        <el-option label="全部" value="" />
        <el-option label="欺诈" :value="1" />
        <el-option label="恶意投诉" :value="2" />
        <el-option label="空壳公司" :value="3" />
        <el-option label="同行竞对" :value="4" />
        <el-option label="其他" :value="5" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-card shadow="hover">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="companyName" label="公司名称" min-width="140" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.blacklistTypeLabel || typeLabel(row.blacklistType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'info'" size="small">
              {{ row.status === 1 ? '生效' : '已解除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatedBy" label="操作人" width="100" />
        <el-table-column prop="createdTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="primary"
              link
              size="small"
              @click="openReleaseDialog(row)"
            >
              解除
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 解除黑名单弹窗 -->
    <el-dialog v-model="showReleaseDialog" title="解除黑名单" width="450px" destroy-on-close>
      <el-form ref="releaseFormRef" :model="releaseForm" :rules="releaseRules" label-width="100px">
        <el-form-item label="解除原因" prop="releaseReason">
          <el-input v-model="releaseForm.releaseReason" type="textarea" :rows="3" placeholder="请输入解除原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReleaseDialog = false">取消</el-button>
        <el-button type="primary" :loading="releasing" @click="handleRelease">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加黑名单弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加黑名单" width="500px" destroy-on-close @close="resetAddForm">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="100px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="addForm.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="手机号" prop="contactPhone">
          <el-input v-model="addForm.contactPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="类型" prop="blacklistType">
          <el-select v-model="addForm.blacklistType" placeholder="请选择类型" style="width: 100%">
            <el-option label="欺诈" :value="1" />
            <el-option label="恶意投诉" :value="2" />
            <el-option label="空壳公司" :value="3" />
            <el-option label="同行竞对" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="addForm.reason" type="textarea" :rows="3" placeholder="请输入原因" />
        </el-form-item>
        <el-form-item label="关联客户ID" prop="customerId">
          <el-input-number v-model="addForm.customerId" :min="0" placeholder="选填" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { blacklistApi } from '@/api/index'
import type { FormInstance, FormRules } from 'element-plus'

const statusFilter = ref<number | ''>('')
const typeFilter = ref<number | ''>('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const loading = ref(false)
const showAddDialog = ref(false)
const showReleaseDialog = ref(false)
const adding = ref(false)
const releasing = ref(false)
const addFormRef = ref<FormInstance>()
const releaseFormRef = ref<FormInstance>()
const releaseTargetId = ref<number | null>(null)

const addForm = reactive({
  companyName: '',
  contactPhone: '',
  blacklistType: undefined as number | undefined,
  reason: '',
  customerId: undefined as number | undefined,
})

const releaseForm = reactive({
  releaseReason: '',
})

const addRules: FormRules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  blacklistType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  reason: [{ required: true, message: '请输入原因', trigger: 'blur' }],
}

const releaseRules: FormRules = {
  releaseReason: [{ required: true, message: '请输入解除原因', trigger: 'blur' }],
}

function typeLabel(type: number) {
  const map: Record<number, string> = {
    1: '欺诈', 2: '恶意投诉', 3: '空壳公司', 4: '同行竞对', 5: '其他',
  }
  return map[type] || '-'
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await blacklistApi.page({
      pageNum: page.value,
      pageSize: pageSize.value,
      status: statusFilter.value || undefined,
      blacklistType: typeFilter.value || undefined,
    })
    list.value = res?.records ?? res?.list ?? []
    total.value = res?.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function openReleaseDialog(row: any) {
  releaseTargetId.value = row.id
  releaseForm.releaseReason = ''
  showReleaseDialog.value = true
}

function resetAddForm() {
  addForm.companyName = ''
  addForm.contactPhone = ''
  addForm.blacklistType = undefined
  addForm.reason = ''
  addForm.customerId = undefined
  addFormRef.value?.resetFields()
}

async function handleRelease() {
  try {
    await releaseFormRef.value?.validate()
  } catch {
    return
  }
  if (releaseTargetId.value == null) return
  releasing.value = true
  try {
    await blacklistApi.release(releaseTargetId.value, { releaseReason: releaseForm.releaseReason })
    showReleaseDialog.value = false
    releaseTargetId.value = null
    loadData()
  } finally {
    releasing.value = false
  }
}

async function handleAdd() {
  try {
    await addFormRef.value?.validate()
  } catch {
    return
  }
  adding.value = true
  try {
    const payload: any = {
      companyName: addForm.companyName,
      contactPhone: addForm.contactPhone || undefined,
      blacklistType: addForm.blacklistType,
      reason: addForm.reason,
    }
    if (addForm.customerId) payload.customerId = addForm.customerId
    await blacklistApi.add(payload)
    showAddDialog.value = false
    loadData()
  } finally {
    adding.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.customer-blacklist { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
