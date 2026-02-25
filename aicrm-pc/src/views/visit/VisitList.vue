<template>
  <div class="page visit-list">
    <div class="page-header">
      <h1 class="page-title">拜访管理</h1>
      <el-button type="primary" @click="showCreate = true">新建拜访</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="filters.visitMethod" placeholder="拜访方式" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="现场" value="onsite" />
        <el-option label="电话" value="phone" />
        <el-option label="微信" value="wechat" />
        <el-option label="企微" value="wework" />
        <el-option label="视频" value="video" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="计划中" value="planned" />
        <el-option label="进行中" value="ongoing" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="code" label="拜访编号" width="120" />
      <el-table-column prop="customerId" label="客户ID" width="100" />
      <el-table-column prop="visitMethod" label="拜访方式" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.visitMethod === 'onsite'" type="success" size="small">现场</el-tag>
          <el-tag v-else-if="row.visitMethod === 'phone'" type="primary" size="small">电话</el-tag>
          <el-tag v-else-if="row.visitMethod === 'wechat'" size="small" style="background:#9c27b0;color:#fff">微信</el-tag>
          <el-tag v-else-if="row.visitMethod === 'wework'" size="small" style="background:#e6a23c;color:#fff">企微</el-tag>
          <el-tag v-else-if="row.visitMethod === 'video'" size="small">视频</el-tag>
          <el-tag v-else size="small">{{ row.visitMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="purpose" label="拜访目的" min-width="140" />
      <el-table-column prop="visitTime" label="拜访时间" width="160">
        <template #default="{ row }">{{ formatDate(row.visitTime) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <template v-if="row.status !== 'completed' && row.status !== 'cancelled'">
            <el-popconfirm title="确定完成？" @confirm="openCompleteDialog(row)">
              <template #reference>
                <el-button link type="success">完成</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确定取消？" @confirm="handleCancel(row.id)">
              <template #reference>
                <el-button link type="danger">取消</el-button>
              </template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
    </div>

    <el-dialog v-model="showCreate" title="新建拜访" width="500px" @close="resetCreateForm">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="客户ID">
          <el-input v-model.number="createForm.customerId" placeholder="请输入客户ID" type="number" />
        </el-form-item>
        <el-form-item label="拜访方式">
          <el-select v-model="createForm.visitMethod" placeholder="选择拜访方式" style="width: 100%">
            <el-option label="现场" value="onsite" />
            <el-option label="电话" value="phone" />
            <el-option label="微信" value="wechat" />
            <el-option label="企微" value="wework" />
            <el-option label="视频" value="video" />
          </el-select>
        </el-form-item>
        <el-form-item label="拜访目的">
          <el-input v-model="createForm.purpose" placeholder="请输入拜访目的" />
        </el-form-item>
        <el-form-item label="拜访时间">
          <el-date-picker v-model="createForm.visitTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="createForm.visitMethod === 'phone'" label="联系电话">
          <el-input v-model="createForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCompleteDialog" title="完成拜访" width="400px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="拜访结果">
          <el-input v-model="completeForm.visitResult" type="textarea" :rows="4" placeholder="请输入拜访结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCompleteDialog = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="handleComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { visitApi } from '@/api/index'

const loading = ref(false)
const list = ref<any[]>([])
const filters = reactive({ visitMethod: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showCreate = ref(false)
const createLoading = ref(false)
const showCompleteDialog = ref(false)
const completeLoading = ref(false)
const completeTargetId = ref<number | null>(null)
const createForm = reactive({
  customerId: undefined as number | undefined,
  visitMethod: '',
  purpose: '',
  visitTime: '',
  phone: '',
  remark: '',
})
const completeForm = reactive({ visitResult: '' })

const statusMap: Record<string, string> = { planned: '计划中', ongoing: '进行中', completed: '已完成', cancelled: '已取消' }

const formatDate = (v: string | undefined) => v || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await visitApi.page({
      page: pagination.page,
      size: pagination.size,
      visitMethod: filters.visitMethod || undefined,
      status: filters.status || undefined,
    })
    list.value = res?.records ?? res?.list ?? []
    pagination.total = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

const resetCreateForm = () => {
  createForm.customerId = undefined
  createForm.visitMethod = ''
  createForm.purpose = ''
  createForm.visitTime = ''
  createForm.phone = ''
  createForm.remark = ''
}

const handleCreate = async () => {
  createLoading.value = true
  try {
    await visitApi.create({
      customerId: createForm.customerId,
      visitMethod: createForm.visitMethod || undefined,
      purpose: createForm.purpose || undefined,
      visitTime: createForm.visitTime || undefined,
      phone: createForm.visitMethod === 'phone' ? createForm.phone : undefined,
      remark: createForm.remark || undefined,
    })
    ElMessage.success('创建成功')
    showCreate.value = false
    loadData()
  } finally {
    createLoading.value = false
  }
}

const handleView = (row: any) => {
  ElMessage.info('查看详情功能可扩展')
}

const openCompleteDialog = (row: any) => {
  completeTargetId.value = row.id
  completeForm.visitResult = ''
  showCompleteDialog.value = true
}

const handleComplete = async () => {
  if (!completeTargetId.value) return
  completeLoading.value = true
  try {
    await visitApi.complete(completeTargetId.value, { visitResult: completeForm.visitResult })
    ElMessage.success('已完成')
    showCompleteDialog.value = false
    completeTargetId.value = null
    loadData()
  } finally {
    completeLoading.value = false
  }
}

const handleCancel = async (id: number) => {
  try {
    await visitApi.cancel(id)
    ElMessage.success('已取消')
    loadData()
  } catch {}
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
