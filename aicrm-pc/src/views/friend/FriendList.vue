<template>
  <div class="page">
    <div class="page-header">
      <h2>好友管理</h2>
      <el-button type="primary" @click="addVisible = true">添加好友</el-button>
    </div>

    <div class="stat-row">
      <div class="stat-card">
        <div class="value">{{ statData.totalCount ?? 0 }}</div>
        <div class="label">总好友数</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ statData.platformCount ?? 0 }}</div>
        <div class="label">平台好友</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ statData.wecomCount ?? 0 }}</div>
        <div class="label">企微好友</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ statData.dualCount ?? 0 }}</div>
        <div class="label">双渠道好友</div>
      </div>
    </div>

    <div class="filter-bar">
      <el-input v-model="search.keyword" placeholder="搜索好友" clearable style="width: 200px" @keyup.enter="load" />
      <el-select v-model="search.friendType" placeholder="好友类型" clearable style="width: 130px">
        <el-option label="全部" value="" />
        <el-option label="平台好友" value="platform" />
        <el-option label="企微好友" value="wecom" />
        <el-option label="双渠道" value="dual" />
      </el-select>
      <el-select v-model="search.source" placeholder="来源" clearable style="width: 130px">
        <el-option label="全部" value="" />
        <el-option label="活动扫码" value="activity_scan" />
        <el-option label="企微同步" value="wecom_sync" />
        <el-option label="手动添加" value="manual" />
        <el-option label="线索导入" value="lead_import" />
        <el-option label="名片扫描" value="card_scan" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
    </div>

    <div class="card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="name" label="好友名称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="company" label="公司" min-width="120" />
        <el-table-column prop="position" label="职位" width="100" />
        <el-table-column prop="friendType" label="好友类型" width="100">
          <template #default="{ row }">
            <el-tag :type="friendTypeTagMap[row.friendType] || 'info'" :class="{ 'tag-dual': row.friendType === 'dual' }" size="small">
              {{ friendTypeTextMap[row.friendType] || row.friendType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ sourceTextMap[row.source] || row.source || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="linkedCustomer" label="已关联客户" width="110">
          <template #default="{ row }">
            <el-tag :type="row.linkedCustomer ? 'success' : 'info'" size="small">
              {{ row.linkedCustomer ? '✓' : '✗' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="添加时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openLinkDialog(row)">关联客户</el-button>
            <el-popconfirm title="确定转为线索？" @confirm="convertToLead(row)">
              <template #reference>
                <el-button link type="primary">转线索</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确定删除？" @confirm="del(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
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
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="linkVisible" title="关联客户" width="400px">
      <el-form :model="linkForm" label-width="80px">
        <el-form-item label="客户ID" required>
          <el-input v-model="linkForm.customerId" placeholder="请输入客户ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="linkVisible = false">取消</el-button>
        <el-button type="primary" :loading="linkSubmitting" @click="submitLink">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addVisible" title="添加好友" width="500px" destroy-on-close @close="resetAddForm">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="90px">
        <el-form-item label="好友姓名" prop="name" required>
          <el-input v-model="addForm.name" placeholder="请输入好友姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" required>
          <el-input v-model="addForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="公司" prop="company" required>
          <el-input v-model="addForm.company" placeholder="请输入公司" />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="addForm.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="好友类型" prop="friendType">
          <el-select v-model="addForm.friendType" placeholder="请选择" style="width: 100%">
            <el-option label="平台好友" value="platform" />
            <el-option label="企微好友" value="wecom" />
            <el-option label="双渠道" value="dual" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-select v-model="addForm.source" placeholder="请选择" style="width: 100%">
            <el-option label="活动扫码" value="activity_scan" />
            <el-option label="企微同步" value="wecom_sync" />
            <el-option label="手动添加" value="manual" />
            <el-option label="线索导入" value="lead_import" />
            <el-option label="名片扫描" value="card_scan" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="addSubmitting" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { friendApi } from '@/api'

const loading = ref(false)
const list = ref<any[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statData = ref<Record<string, number>>({})
const linkVisible = ref(false)
const linkSubmitting = ref(false)
const addVisible = ref(false)
const addSubmitting = ref(false)
const addFormRef = ref<FormInstance>()
const currentLinkFriend = ref<any>(null)

const search = reactive({ keyword: '', friendType: '', source: '' })
const friendTypeTagMap: Record<string, string> = { platform: 'primary', wecom: 'success', dual: '' }
const friendTypeTextMap: Record<string, string> = { platform: '平台好友', wecom: '企微好友', dual: '双渠道' }
const sourceTextMap: Record<string, string> = {
  activity_scan: '活动扫码',
  wecom_sync: '企微同步',
  manual: '手动添加',
  lead_import: '线索导入',
  card_scan: '名片扫描',
}

const linkForm = reactive({ customerId: '' })
const addForm = reactive({
  name: '',
  phone: '',
  company: '',
  position: '',
  friendType: 'platform',
  source: 'manual',
  remark: '',
})
const addRules: FormRules = {
  name: [{ required: true, message: '请输入好友姓名' }],
  phone: [{ required: true, message: '请输入手机号' }],
  company: [{ required: true, message: '请输入公司' }],
}

async function loadStat() {
  try {
    const res = (await friendApi.statistics()) as any
    statData.value = res || {}
  } catch (e) {
    // ignore
  }
}

async function load() {
  loading.value = true
  try {
    const res = (await friendApi.page({
      page: page.value,
      pageSize: pageSize.value,
      keyword: search.keyword || undefined,
      friendType: search.friendType || undefined,
      source: search.source || undefined,
    })) as any
    list.value = res?.records ?? res?.list ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

function openLinkDialog(row: any) {
  currentLinkFriend.value = row
  linkForm.customerId = ''
  linkVisible.value = true
}

async function submitLink() {
  if (!linkForm.customerId?.trim()) {
    ElMessage.warning('请输入客户ID')
    return
  }
  if (!currentLinkFriend.value) return
  linkSubmitting.value = true
  try {
    await friendApi.linkCustomer(currentLinkFriend.value.id, { customerId: linkForm.customerId })
    ElMessage.success('关联成功')
    linkVisible.value = false
    load()
    loadStat()
  } finally {
    linkSubmitting.value = false
  }
}

async function convertToLead(row: any) {
  try {
    await friendApi.convertToLead(row.id)
    ElMessage.success('已转为线索')
    load()
    loadStat()
  } catch (e) {
    // error handled by interceptor
  }
}

async function del(row: any) {
  try {
    await friendApi.delete(row.id)
    ElMessage.success('删除成功')
    load()
    loadStat()
  } catch (e) {
    // error handled by interceptor
  }
}

function resetAddForm() {
  addForm.name = ''
  addForm.phone = ''
  addForm.company = ''
  addForm.position = ''
  addForm.friendType = 'platform'
  addForm.source = 'manual'
  addForm.remark = ''
}

async function submitAdd() {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    addSubmitting.value = true
    try {
      await friendApi.create(addForm)
      ElMessage.success('添加成功')
      addVisible.value = false
      load()
      loadStat()
    } finally {
      addSubmitting.value = false
    }
  })
}

onMounted(() => {
  loadStat()
  load()
})
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.tag-dual { --el-tag-bg-color: #f3e8ff; --el-tag-border-color: #c4b5fd; --el-tag-text-color: #6d28d9; }
</style>
