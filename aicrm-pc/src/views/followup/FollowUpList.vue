<template>
  <div class="page followup-list">
    <div class="page-header">
      <h1 class="page-title">跟进记录</h1>
      <el-button type="primary" @click="showCreate = true">新建跟进</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="filters.bizType" placeholder="业务类型" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="线索" value="lead" />
        <el-option label="客户" value="customer" />
        <el-option label="商机" value="opportunity" />
      </el-select>
      <el-select v-model="filters.followMethod" placeholder="跟进方式" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="现场" value="onsite" />
        <el-option label="电话" value="phone" />
        <el-option label="微信" value="wechat" />
        <el-option label="邮件" value="email" />
        <el-option label="企微" value="wework" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="bizType" label="业务类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ bizTypeMap[row.bizType] || row.bizType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="followMethod" label="跟进方式" width="100">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ followMethodMap[row.followMethod] || row.followMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="跟进内容" min-width="200">
        <template #default="{ row }">{{ truncate(row.content, 100) }}</template>
      </el-table-column>
      <el-table-column prop="followUserName" label="跟进人" width="100" />
      <el-table-column prop="nextFollowTime" label="下次跟进时间" width="160">
        <template #default="{ row }">{{ formatDate(row.nextFollowTime) }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
    </div>

    <el-dialog v-model="showCreate" title="新建跟进" width="500px" @close="resetCreateForm">
      <el-form :model="createForm" label-width="110px">
        <el-form-item label="业务类型">
          <el-radio-group v-model="createForm.bizType">
            <el-radio value="lead">线索</el-radio>
            <el-radio value="customer">客户</el-radio>
            <el-radio value="opportunity">商机</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="业务ID">
          <el-input v-model="createForm.bizId" placeholder="请输入业务ID" />
        </el-form-item>
        <el-form-item label="跟进方式">
          <el-select v-model="createForm.followMethod" placeholder="选择跟进方式" style="width: 100%">
            <el-option label="现场" value="onsite" />
            <el-option label="电话" value="phone" />
            <el-option label="微信" value="wechat" />
            <el-option label="邮件" value="email" />
            <el-option label="企微" value="wework" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容" required>
          <el-input v-model="createForm.content" type="textarea" :rows="4" placeholder="请输入跟进内容" />
        </el-form-item>
        <el-form-item label="下次跟进时间">
          <el-date-picker v-model="createForm.nextFollowTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="下次跟进备注">
          <el-input v-model="createForm.nextFollowNote" placeholder="请输入备注" />
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
import { followUpApi } from '@/api/index'

const loading = ref(false)
const list = ref<any[]>([])
const filters = reactive({ bizType: '', followMethod: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showCreate = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  bizType: 'lead',
  bizId: '',
  followMethod: '',
  content: '',
  nextFollowTime: '',
  nextFollowNote: '',
})

const bizTypeMap: Record<string, string> = { lead: '线索', customer: '客户', opportunity: '商机' }
const followMethodMap: Record<string, string> = { onsite: '现场', phone: '电话', wechat: '微信', email: '邮件', wework: '企微' }

const truncate = (s: string | undefined, len: number) => {
  if (!s) return '-'
  return s.length > len ? s.slice(0, len) + '...' : s
}
const formatDate = (v: string | undefined) => v || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await followUpApi.page({
      page: pagination.page,
      size: pagination.size,
      bizType: filters.bizType || undefined,
      followMethod: filters.followMethod || undefined,
    })
    list.value = res?.records ?? res?.list ?? []
    pagination.total = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

const resetCreateForm = () => {
  createForm.bizType = 'lead'
  createForm.bizId = ''
  createForm.followMethod = ''
  createForm.content = ''
  createForm.nextFollowTime = ''
  createForm.nextFollowNote = ''
}

const handleCreate = async () => {
  if (!createForm.content?.trim()) {
    ElMessage.warning('请输入跟进内容')
    return
  }
  createLoading.value = true
  try {
    await followUpApi.create({
      bizType: createForm.bizType,
      bizId: createForm.bizId ? Number(createForm.bizId) : undefined,
      followMethod: createForm.followMethod || undefined,
      content: createForm.content.trim(),
      nextFollowTime: createForm.nextFollowTime || undefined,
      nextFollowNote: createForm.nextFollowNote || undefined,
    })
    ElMessage.success('创建成功')
    showCreate.value = false
    loadData()
  } finally {
    createLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await followUpApi.delete(id)
    ElMessage.success('删除成功')
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
