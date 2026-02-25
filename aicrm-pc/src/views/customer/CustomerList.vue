<template>
  <div class="customer-list">
    <div class="page-header">
      <h2 class="page-title">客户管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建客户
      </el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索客户名称"
        clearable
        style="width: 220px"
        @keyup.enter="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="lifecycleFilter" placeholder="生命周期" clearable style="width: 120px">
        <el-option label="全部" value="" />
        <el-option label="潜在" value="potential" />
        <el-option label="意向" value="intent" />
        <el-option label="成交" value="deal" />
        <el-option label="活跃" value="active" />
        <el-option label="VIP" value="vip" />
        <el-option label="流失" value="churned" />
      </el-select>
      <el-select v-model="levelFilter" placeholder="客户等级" clearable style="width: 120px">
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
        <el-table-column prop="customerNo" label="客户编号" width="120" />
        <el-table-column prop="name" label="客户名称" min-width="140" />
        <el-table-column prop="industry" label="行业" width="100" />
        <el-table-column label="地区" width="140">
          <template #default="{ row }">{{ [row.province, row.city].filter(Boolean).join(' ') || '-' }}</template>
        </el-table-column>
        <el-table-column prop="lifecycle" label="生命周期" width="90">
          <template #default="{ row }">
            <el-tag :type="lifecycleTagType(row.lifecycle)" size="small">{{ lifecycleText(row.lifecycle) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="客户等级" width="90">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">{{ row.level || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="采购倒计时" width="110">
          <template #default="{ row }">
            <span :class="countdownClass(row.purchaseCountdown)">{{ countdownText(row.purchaseCountdown) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="90" />
        <el-table-column prop="lastFollowAt" label="最后跟进" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="$router.push(`/customers/${row.id}`)">查看</el-link>
            <el-divider direction="vertical" />
            <el-popconfirm title="确定删除该客户？" @confirm="handleDelete(row.id)">
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

    <!-- 新建客户弹窗 -->
    <el-dialog v-model="showCreateDialog" title="新建客户" width="500px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="行业" prop="industry">
          <el-input v-model="form.industry" placeholder="请输入行业" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="生命周期" prop="lifecycle">
          <el-select v-model="form.lifecycle" placeholder="请选择" style="width: 100%">
            <el-option label="潜在" value="potential" />
            <el-option label="意向" value="intent" />
            <el-option label="成交" value="deal" />
            <el-option label="活跃" value="active" />
            <el-option label="VIP" value="vip" />
            <el-option label="流失" value="churned" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择" style="width: 100%">
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
import { customerApi } from '@/api/index'
import type { FormInstance, FormRules } from 'element-plus'

const keyword = ref('')
const lifecycleFilter = ref('')
const levelFilter = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const loading = ref(false)
const showCreateDialog = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  industry: '',
  province: '',
  city: '',
  lifecycle: '',
  level: '',
  remark: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
}

function lifecycleTagType(lc: string) {
  const map: Record<string, string> = {
    potential: 'info', intent: 'warning', deal: 'success', active: 'primary', vip: 'danger', churned: 'info',
  }
  return map[lc] || 'info'
}

function lifecycleText(lc: string) {
  const map: Record<string, string> = {
    potential: '潜在', intent: '意向', deal: '成交', active: '活跃', vip: 'VIP', churned: '流失',
  }
  return map[lc] || lc || '-'
}

function levelTagType(level: string) {
  const map: Record<string, string> = { A: 'success', B: 'primary', C: 'warning', D: 'info' }
  return map[level] || 'info'
}

function countdownText(cd: { days?: number; expired?: boolean } | null | undefined): string {
  if (!cd) return '-'
  if (cd.expired && cd.days != null) return `已过期${Math.abs(cd.days)}天`
  if (cd.days != null && cd.days >= 0) return `还剩${cd.days}天`
  return '-'
}

function countdownClass(cd: { days?: number; expired?: boolean } | null | undefined): string {
  if (!cd) return ''
  if (cd.expired) return 'countdown-expired'
  return 'countdown-ok'
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await customerApi.page({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      lifecycle: lifecycleFilter.value || undefined,
      level: levelFilter.value || undefined,
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
  form.name = ''
  form.industry = ''
  form.province = ''
  form.city = ''
  form.lifecycle = ''
  form.level = ''
  form.remark = ''
  formRef.value?.resetFields()
}

async function handleDelete(id: number) {
  try {
    await customerApi.delete(id)
    loadData()
  } catch (e) {}
}

async function handleCreate() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await customerApi.create(form)
    showCreateDialog.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.customer-list { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.countdown-ok { color: #67c23a; font-weight: 500; }
.countdown-expired { color: #f56c6c; font-weight: 500; }
</style>
