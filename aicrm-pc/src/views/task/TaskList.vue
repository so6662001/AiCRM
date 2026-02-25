<template>
  <div class="page task-list">
    <div class="page-header">
      <h1 class="page-title">任务管理</h1>
      <el-button type="primary" @click="showCreate = true">新建任务</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="待开始" value="pending" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="已完成" value="completed" />
        <el-option label="已逾期" value="overdue" />
      </el-select>
      <el-select v-model="filters.priority" placeholder="优先级" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="低" value="low" />
        <el-option label="中" value="medium" />
        <el-option label="高" value="high" />
        <el-option label="紧急" value="urgent" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="code" label="任务编号" width="120" />
      <el-table-column prop="title" label="任务标题" min-width="160" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ typeMap[row.type] || row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.priority === 'low'" size="small" style="background:#909399;color:#fff">低</el-tag>
          <el-tag v-else-if="row.priority === 'medium'" type="primary" size="small">中</el-tag>
          <el-tag v-else-if="row.priority === 'high'" size="small" style="background:#e6a23c;color:#fff">高</el-tag>
          <el-tag v-else-if="row.priority === 'urgent'" type="danger" size="small">紧急</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计划时间" width="200">
        <template #default="{ row }">{{ formatPlanTime(row) }}</template>
      </el-table-column>
      <el-table-column prop="completionRate" label="完成率" width="100">
        <template #default="{ row }">
          <el-progress :percentage="row.completionRate ?? 0" :stroke-width="6" />
        </template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="90">
        <template #default="{ row }">{{ row.source === 'self' ? '自主' : row.source === 'assigned' ? '上级安排' : row.source || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 'completed'" link type="success" @click="openCompleteDialog(row)">完成</el-button>
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
    </div>

    <el-dialog v-model="showCreate" title="新建任务" width="500px" @close="resetCreateForm">
      <el-form :model="createForm" label-width="110px">
        <el-form-item label="任务标题" required>
          <el-input v-model="createForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="createForm.content" type="textarea" :rows="4" placeholder="请输入任务内容" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="createForm.type" placeholder="选择类型" style="width: 100%">
            <el-option label="普通" value="normal" />
            <el-option label="跟进" value="followup" />
            <el-option label="拜访" value="visit" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="createForm.priority" placeholder="选择优先级" style="width: 100%">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始时间">
          <el-date-picker v-model="createForm.planStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划结束时间">
          <el-date-picker v-model="createForm.planEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="提醒时间">
          <el-date-picker v-model="createForm.remindTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEdit" title="编辑任务" width="500px">
      <el-form :model="editForm" label-width="110px">
        <el-form-item label="任务标题" required>
          <el-input v-model="editForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editForm.content" type="textarea" :rows="4" placeholder="请输入任务内容" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="editForm.type" placeholder="选择类型" style="width: 100%">
            <el-option label="普通" value="normal" />
            <el-option label="跟进" value="followup" />
            <el-option label="拜访" value="visit" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="editForm.priority" placeholder="选择优先级" style="width: 100%">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleUpdate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCompleteDialog" title="完成任务" width="400px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="完成备注">
          <el-input v-model="completeForm.completionNote" type="textarea" :rows="4" placeholder="请输入完成备注" />
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
import { taskApi } from '@/api/index'

const loading = ref(false)
const list = ref<any[]>([])
const filters = reactive({ status: '', priority: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showCreate = ref(false)
const createLoading = ref(false)
const showEdit = ref(false)
const editLoading = ref(false)
const showCompleteDialog = ref(false)
const completeLoading = ref(false)
const completeTargetId = ref<number | null>(null)
const createForm = reactive({
  title: '',
  content: '',
  type: '',
  priority: '',
  planStartTime: '',
  planEndTime: '',
  remindTime: '',
})
const editForm = reactive({ id: 0, title: '', content: '', type: '', priority: '' })
const completeForm = reactive({ completionNote: '' })

const typeMap: Record<string, string> = { normal: '普通', followup: '跟进', visit: '拜访', other: '其他' }
const statusMap: Record<string, string> = { pending: '待开始', in_progress: '进行中', completed: '已完成', overdue: '已逾期' }

const formatPlanTime = (row: any) => {
  const s = row.planStartTime || ''
  const e = row.planEndTime || ''
  if (!s && !e) return '-'
  return `${s || '-'} ~ ${e || '-'}`
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await taskApi.page({
      page: pagination.page,
      size: pagination.size,
      status: filters.status || undefined,
      priority: filters.priority || undefined,
    })
    list.value = res?.records ?? res?.list ?? []
    pagination.total = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

const resetCreateForm = () => {
  createForm.title = ''
  createForm.content = ''
  createForm.type = ''
  createForm.priority = ''
  createForm.planStartTime = ''
  createForm.planEndTime = ''
  createForm.remindTime = ''
}

const handleCreate = async () => {
  if (!createForm.title?.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  createLoading.value = true
  try {
    await taskApi.create({
      title: createForm.title.trim(),
      content: createForm.content || undefined,
      type: createForm.type || undefined,
      priority: createForm.priority || undefined,
      planStartTime: createForm.planStartTime || undefined,
      planEndTime: createForm.planEndTime || undefined,
      remindTime: createForm.remindTime || undefined,
    })
    ElMessage.success('创建成功')
    showCreate.value = false
    loadData()
  } finally {
    createLoading.value = false
  }
}

const openEditDialog = (row: any) => {
  editForm.id = row.id
  editForm.title = row.title || ''
  editForm.content = row.content || ''
  editForm.type = row.type || ''
  editForm.priority = row.priority || ''
  showEdit.value = true
}

const handleUpdate = async () => {
  if (!editForm.title?.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  editLoading.value = true
  try {
    await taskApi.update(editForm.id, {
      title: editForm.title.trim(),
      content: editForm.content || undefined,
      type: editForm.type || undefined,
      priority: editForm.priority || undefined,
    })
    ElMessage.success('更新成功')
    showEdit.value = false
    loadData()
  } finally {
    editLoading.value = false
  }
}

const openCompleteDialog = (row: any) => {
  completeTargetId.value = row.id
  completeForm.completionNote = ''
  showCompleteDialog.value = true
}

const handleComplete = async () => {
  if (!completeTargetId.value) return
  completeLoading.value = true
  try {
    await taskApi.complete(completeTargetId.value, { completionNote: completeForm.completionNote })
    ElMessage.success('已完成')
    showCompleteDialog.value = false
    completeTargetId.value = null
    loadData()
  } finally {
    completeLoading.value = false
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
