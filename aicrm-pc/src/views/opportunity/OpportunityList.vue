<template>
  <div class="page opportunity-list">
    <div class="page-header">
      <h1 class="page-title">商机管理</h1>
      <el-button type="primary" @click="showCreate = true">新建商机</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="filters.keyword" placeholder="搜索商机名称" clearable style="width: 200px" />
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="进行中" value="ongoing" />
        <el-option label="赢单" value="won" />
        <el-option label="输单" value="lost" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="code" label="商机编号" width="120" />
      <el-table-column prop="name" label="商机名称" min-width="160" />
      <el-table-column prop="customerName" label="客户名称" min-width="120" />
      <el-table-column prop="expectedAmount" label="预计金额" width="120">
        <template #default="{ row }">{{ formatAmount(row.expectedAmount) }}</template>
      </el-table-column>
      <el-table-column prop="stageName" label="阶段" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ row.stageName || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="winRate" label="赢率" width="100">
        <template #default="{ row }">
          <el-progress :percentage="row.winRate || 0" :stroke-width="6" />
        </template>
      </el-table-column>
      <el-table-column prop="expectedCloseDate" label="预计成交日期" width="120">
        <template #default="{ row }">{{ formatDate(row.expectedCloseDate) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'ongoing'" type="primary" size="small">进行中</el-tag>
          <el-tag v-else-if="row.status === 'won'" type="success" size="small">赢单</el-tag>
          <el-tag v-else-if="row.status === 'lost'" type="danger" size="small">输单</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/opportunities/${row.id}`)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
    </div>

    <el-dialog v-model="showCreate" title="新建商机" width="500px" @close="resetCreateForm">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="商机名称" required>
          <el-input v-model="createForm.name" placeholder="请输入商机名称" />
        </el-form-item>
        <el-form-item label="客户ID">
          <el-input v-model.number="createForm.customerId" placeholder="请输入客户ID" type="number" />
        </el-form-item>
        <el-form-item label="阶段ID">
          <el-input v-model.number="createForm.stageId" placeholder="请输入阶段ID" type="number" />
        </el-form-item>
        <el-form-item label="预计金额">
          <el-input v-model.number="createForm.expectedAmount" placeholder="请输入预计金额" type="number" />
        </el-form-item>
        <el-form-item label="预计成交日期">
          <el-date-picker v-model="createForm.expectedCloseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="竞争对手">
          <el-input v-model="createForm.competitor" placeholder="请输入竞争对手" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" placeholder="请输入备注" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { opportunityApi } from '@/api/index'

const loading = ref(false)
const list = ref<any[]>([])
const filters = reactive({ keyword: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showCreate = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  name: '',
  customerId: undefined as number | undefined,
  stageId: undefined as number | undefined,
  expectedAmount: undefined as number | undefined,
  expectedCloseDate: '',
  competitor: '',
  remark: '',
})

const formatAmount = (v: number | undefined) => (v != null ? `¥${Number(v).toLocaleString()}` : '-')
const formatDate = (v: string | undefined) => v || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await opportunityApi.page({
      page: pagination.page,
      size: pagination.size,
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
    })
    list.value = res?.records ?? res?.list ?? []
    pagination.total = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

const resetCreateForm = () => {
  createForm.name = ''
  createForm.customerId = undefined
  createForm.stageId = undefined
  createForm.expectedAmount = undefined
  createForm.expectedCloseDate = ''
  createForm.competitor = ''
  createForm.remark = ''
}

const handleCreate = async () => {
  if (!createForm.name?.trim()) {
    ElMessage.warning('请输入商机名称')
    return
  }
  createLoading.value = true
  try {
    await opportunityApi.create({
      name: createForm.name.trim(),
      customerId: createForm.customerId,
      stageId: createForm.stageId,
      expectedAmount: createForm.expectedAmount,
      expectedCloseDate: createForm.expectedCloseDate || undefined,
      competitor: createForm.competitor || undefined,
      remark: createForm.remark || undefined,
    })
    ElMessage.success('创建成功')
    showCreate.value = false
    loadData()
  } finally {
    createLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page { background: #fff; border-radius: 8px; padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
:deep(.el-button--primary) { background: #4F6EF6; border-color: #4F6EF6; }
:deep(.el-button--primary:hover) { background: #3d5ce8; border-color: #3d5ce8; }
</style>
