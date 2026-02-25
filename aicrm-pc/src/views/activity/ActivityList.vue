<template>
  <div class="page">
    <div class="page-header">
      <h2>活动管理</h2>
      <el-button type="primary" @click="createVisible = true">新建活动</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="search.keyword" placeholder="搜索活动" clearable style="width: 200px" @keyup.enter="load" />
      <el-select v-model="search.category" placeholder="类别" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="需要报名" value="need_registration" />
        <el-option label="不需要报名" value="no_registration" />
      </el-select>
      <el-select v-model="search.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="草稿" value="draft" />
        <el-option label="未开始" value="not_started" />
        <el-option label="报名中" value="registration" />
        <el-option label="进行中" value="ongoing" />
        <el-option label="已结束" value="ended" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
    </div>

    <div class="card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="code" label="活动编号" width="120" />
        <el-table-column prop="name" label="活动名称" min-width="160" />
        <el-table-column prop="category" label="类别" width="110">
          <template #default="{ row }">
            <el-tag :type="row.category === 'need_registration' ? 'primary' : 'success'" size="small">
              {{ row.category === 'need_registration' ? '需要报名' : '不需要报名' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column label="活动时间" width="220">
          <template #default="{ row }">
            {{ row.startTime }} ~ {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column label="参与人数" width="100">
          <template #default="{ row }">
            <span v-if="row.maxParticipants">{{ row.currentParticipants || 0 }}/{{ row.maxParticipants }}</span>
            <span v-else>{{ row.currentParticipants || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status] || 'info'" size="small">{{ statusTextMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">查看</el-button>
            <el-button v-if="row.status === 'draft'" link type="primary" @click="publish(row)">发布</el-button>
            <el-button v-if="row.status !== 'ended'" link type="danger" @click="cancel(row)">取消</el-button>
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

    <el-dialog v-model="createVisible" title="新建活动" width="600px" destroy-on-close @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="活动类别" prop="category" required>
          <el-radio-group v-model="form.category">
            <el-radio value="need_registration">需要报名</el-radio>
            <el-radio value="no_registration">不需要报名</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="活动名称" prop="name" required>
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type" required>
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option label="线下展会" value="线下展会" />
            <el-option label="线上推广" value="线上推广" />
            <el-option label="产品发布" value="产品发布" />
            <el-option label="客户沙龙" value="客户沙龙" />
            <el-option label="培训会议" value="培训会议" />
            <el-option label="品牌宣传" value="品牌宣传" />
            <el-option label="优惠活动" value="优惠活动" />
            <el-option label="内容分发" value="内容分发" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动时间" required>
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
        </el-form-item>
        <template v-if="form.category === 'need_registration'">
          <el-form-item label="报名时间范围" prop="registrationTimeRange">
            <el-date-picker
              v-model="form.registrationTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="最大参与人数" prop="maxParticipants">
            <el-input-number v-model="form.maxParticipants" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="报名审核" prop="needApproval">
            <el-switch v-model="form.needApproval" />
          </el-form-item>
          <el-form-item label="报名须知" prop="registrationNotice">
            <el-input v-model="form.registrationNotice" type="textarea" :rows="2" placeholder="请输入报名须知" />
          </el-form-item>
        </template>
        <el-form-item label="预算" prop="budget">
          <el-input-number v-model="form.budget" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="企业微信绑定" prop="wecomBind">
          <el-switch v-model="form.wecomBind" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { activityApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const createVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const search = reactive({ keyword: '', category: '', status: '' })
const statusTagMap: Record<string, string> = {
  draft: 'info',
  not_started: 'primary',
  registration: 'success',
  ongoing: 'warning',
  ended: 'info',
  cancelled: 'danger',
}
const statusTextMap: Record<string, string> = {
  draft: '草稿',
  not_started: '未开始',
  registration: '报名中',
  ongoing: '进行中',
  ended: '已结束',
  cancelled: '已取消',
}

const form = reactive({
  category: 'need_registration' as string,
  name: '',
  type: '',
  dateRange: null as [string, string] | null,
  location: '',
  description: '',
  registrationTimeRange: null as [string, string] | null,
  maxParticipants: 100,
  needApproval: false,
  registrationNotice: '',
  budget: 0,
  wecomBind: false,
})

const rules: FormRules = {
  category: [{ required: true, message: '请选择活动类别' }],
  name: [{ required: true, message: '请输入活动名称' }],
  type: [{ required: true, message: '请选择活动类型' }],
}

async function load() {
  loading.value = true
  try {
    const res = (await activityApi.page({
      page: page.value,
      pageSize: pageSize.value,
      keyword: search.keyword || undefined,
      category: search.category || undefined,
      status: search.status || undefined,
    })) as any
    list.value = res?.records ?? res?.list ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

async function publish(row: any) {
  try {
    await activityApi.publish(row.id)
    ElMessage.success('发布成功')
    load()
  } catch (e) {
    // error handled by interceptor
  }
}

async function cancel(row: any) {
  try {
    await activityApi.cancel(row.id)
    ElMessage.success('已取消')
    load()
  } catch (e) {
    // error handled by interceptor
  }
}

function goDetail(id: number) {
  router.push(`/activities/${id}`)
}

function resetForm() {
  form.category = 'need_registration'
  form.name = ''
  form.type = ''
  form.dateRange = null
  form.location = ''
  form.description = ''
  form.registrationTimeRange = null
  form.maxParticipants = 100
  form.needApproval = false
  form.registrationNotice = ''
  form.budget = 0
  form.wecomBind = false
}

async function submitCreate() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!form.dateRange?.length) {
      ElMessage.warning('请选择活动时间')
      return
    }
    submitting.value = true
    try {
      const payload: any = {
        category: form.category,
        name: form.name,
        type: form.type,
        startTime: form.dateRange[0],
        endTime: form.dateRange[1],
        location: form.location,
        description: form.description,
        budget: form.budget,
        wecomBind: form.wecomBind,
      }
      if (form.category === 'need_registration') {
        if (form.registrationTimeRange?.length) {
          payload.registrationStartTime = form.registrationTimeRange[0]
          payload.registrationEndTime = form.registrationTimeRange[1]
        }
        payload.maxParticipants = form.maxParticipants
        payload.needApproval = form.needApproval
        payload.registrationNotice = form.registrationNotice
      }
      await activityApi.create(payload)
      ElMessage.success('创建成功')
      createVisible.value = false
      load()
    } finally {
      submitting.value = false
    }
  })
}

onMounted(load)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
