<template>
  <div class="lead-list">
    <div class="page-header">
      <h2 class="page-title">线索管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建线索
      </el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索联系人/公司"
        clearable
        style="width: 220px"
        @keyup.enter="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
        <el-option label="全部" value="" />
        <el-option label="待分配" value="pending" />
        <el-option label="已分配" value="assigned" />
        <el-option label="跟进中" value="following" />
        <el-option label="公海池" value="pool" />
      </el-select>
      <el-select v-model="intentFilter" placeholder="意向等级" clearable style="width: 120px">
        <el-option label="全部" value="" />
        <el-option label="A" value="A" />
        <el-option label="B" value="B" />
        <el-option label="C" value="C" />
        <el-option label="D" value="D" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-card shadow="hover">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="leadNo" label="线索编号" width="120" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="company" label="公司名称" min-width="140" />
        <el-table-column prop="source" label="来源" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.source || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="intentLevel" label="意向等级" width="90">
          <template #default="{ row }">
            <el-tag :type="intentTagType(row.intentLevel)" size="small">{{ row.intentLevel || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="$router.push(`/leads/${row.id}`)">查看</el-link>
            <el-divider direction="vertical" />
            <el-link type="primary" :underline="false" @click="handleAssign(row)">分配</el-link>
            <el-divider direction="vertical" />
            <el-popconfirm title="确定删除该线索？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-link type="danger" :underline="false">删除</el-link>
              </template>
            </el-popconfirm>
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

    <!-- 新建线索弹窗 -->
    <el-dialog v-model="showCreateDialog" title="新建线索" width="500px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="公司" prop="company">
          <el-input v-model="form.company" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-select v-model="form.source" placeholder="请选择来源" style="width: 100%">
            <el-option label="官网" value="website" />
            <el-option label="展会" value="exhibition" />
            <el-option label="转介绍" value="referral" />
            <el-option label="广告" value="ad" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="意向等级" prop="intentLevel">
          <el-select v-model="form.intentLevel" placeholder="请选择意向等级" style="width: 100%">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
            <el-option label="D" value="D" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { leadApi } from '@/api/index'
import type { FormInstance, FormRules } from 'element-plus'

const keyword = ref('')
const statusFilter = ref('')
const intentFilter = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const loading = ref(false)
const showCreateDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  contact: '',
  phone: '',
  email: '',
  company: '',
  source: '',
  intentLevel: '',
  remark: '',
})

const rules: FormRules = {
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

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
  loading.value = true
  try {
    const res: any = await leadApi.page({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      intentLevel: intentFilter.value || undefined,
    })
    list.value = res?.data?.list ?? res?.list ?? []
    total.value = res?.data?.total ?? res?.total ?? 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.contact = ''
  form.phone = ''
  form.email = ''
  form.company = ''
  form.source = ''
  form.intentLevel = ''
  form.remark = ''
  formRef.value?.resetFields()
}

function handleAssign(row: any) {
  // TODO: 分配弹窗
}

async function handleDelete(id: number) {
  try {
    await leadApi.delete(id)
    loadData()
  } catch (e) {
    // error
  }
}

async function handleCreate() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await leadApi.create(form)
    showCreateDialog.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.lead-list { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
